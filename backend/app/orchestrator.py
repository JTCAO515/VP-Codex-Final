from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Literal, Tuple

from app.llm_provider import build_provider
from app.prompts import chat_orchestrate_system_prompt, slot_extraction_system_prompt, slot_extraction_user_prompt

"""
此文件实现“少问问题、多交流”的追问策略（MVP可运行版）：
- 每轮最多 1 个追问
- 只问“关键缺口”（影响排程/下单），其余通过建议 chips 引导（可选）
- 追问统一以 2~4 个选项呈现，降低填写成本

同时提供 LLM 化入口（LLM_ENABLED=1 时可启用），未启用则回退规则抽取。
"""


INTENT = Literal["plan", "book_hotel", "rfp"]


@dataclass
class OrchestrateResult:
    """v2.4: Full orchestration result from a single LLM call."""
    intent: str  # plan | book_hotel | rfp
    reply: str
    slots: dict
    confidence: dict
    ask: dict | None = None
    itinerary: dict | None = None
    actions: list[dict] | None = None
    clear: list | None = None
    remove: dict | None = None


def detect_intent(text: str) -> INTENT:
    t = text.lower()
    if any(k in t for k in ["hotel", "room", "book hotel", "订酒店", "酒店", "订房", "住哪"]):
        return "book_hotel"
    if any(k in t for k in ["guide", "tour", "ticket", "rfp", "导游", "旅行社", "包车", "门票", "定制"]):
        return "rfp"
    return "plan"


def merge_slot_state(old: dict, new: dict) -> dict:
    """
    把新抽取结果合并到旧状态（只覆盖非空字段），确保“多轮对话逐步补齐”。
    """
    old = dict(old or {})

    # 1) 先处理 clear/remove 操作（允许“否定/改口”）
    def clear_path(state: dict, path: str) -> None:
        parts = path.split(".")
        if len(parts) == 1:
            key = parts[0]
            if key in ["cities", "interests"]:
                state[key] = []
            elif key in ["party", "constraints"]:
                state[key] = {}
            else:
                state[key] = None
            return
        cur = state
        for p in parts[:-1]:
            if not isinstance(cur.get(p), dict):
                cur[p] = {}
            cur = cur[p]
        cur[parts[-1]] = None

    clear_list = []
    if isinstance(new, dict) and isinstance(new.get("_clear"), list):
        clear_list = [str(x) for x in new.get("_clear") if x]
    for p in clear_list:
        clear_path(old, p)

    remove_map = new.get("_remove") if isinstance(new, dict) else None
    if isinstance(remove_map, dict):
        for k, items in remove_map.items():
            if not items:
                continue
            if k in ["cities", "interests"] and isinstance(old.get(k), list):
                old[k] = [x for x in old[k] if x not in items]
            elif k.startswith("constraints.") and isinstance(old.get("constraints"), dict):
                _, sub = k.split(".", 1)
                cur = old["constraints"]
                if isinstance(cur.get(sub), list):
                    cur[sub] = [x for x in cur[sub] if x not in items]

        # 同时对 new 中的同名列表字段做过滤，避免“先删后又覆盖回去”
        if isinstance(new.get("cities"), list) and isinstance(remove_map.get("cities"), list):
            new["cities"] = [x for x in new["cities"] if x not in remove_map["cities"]]
        if isinstance(new.get("interests"), list) and isinstance(remove_map.get("interests"), list):
            new["interests"] = [x for x in new["interests"] if x not in remove_map["interests"]]
    # 2) 再合并新值（仅非空覆盖）
    for k, v in (new or {}).items():
        if k in ["_clear", "_remove"]:
            continue
        if v is None:
            continue
        if isinstance(v, list) and not v:
            continue
        if isinstance(v, str) and not v.strip():
            continue
        old[k] = v
    return old



def extract_slots_rule(text: str) -> dict:
    """
    规则抽取（用于无 LLM 时的最小可用）。
    """
    out: dict = {
        "cities": [],
        "days": None,
        "pace": None,  # slow|medium|fast
        "budget_level": None,  # low|mid|high
        "interests": [],  # food|history|nature|shopping|nightlife|family
        "party": {},  # {size, composition, kids}
        "language": None,  # en/zh/...
        "constraints": {},  # dietary/walking/must_see/must_avoid/arrival/departure
    }

    # days
    m = re.search(r"(\d+)\s*day", text, re.I)
    if m:
        out["days"] = int(m.group(1))
    m2 = re.search(r"(\d+)\s*天", text)
    if m2:
        out["days"] = int(m2.group(1))

    # cities (very small seed)
    city_map = {
        "beijing": "Beijing",
        "北京": "Beijing",
        "shanghai": "Shanghai",
        "上海": "Shanghai",
        "guangzhou": "Guangzhou",
        "广州": "Guangzhou",
        "chengdu": "Chengdu",
        "成都": "Chengdu",
        "shenzhen": "Shenzhen",
        "深圳": "Shenzhen",
        "xian": "Xi'an",
        "西安": "Xi'an",
        "hangzhou": "Hangzhou",
        "杭州": "Hangzhou",
    }
    for k, v in city_map.items():
        if k.lower() in text.lower():
            out["cities"].append(v)
    out["cities"] = list(dict.fromkeys(out["cities"]))

    # 否定/改口：移除某些城市（最小可用）
    remove_cities = []
    for k, v in city_map.items():
        kl = k.lower()
        if f"not {kl}" in text.lower() or f"don't {kl}" in text.lower() or f"不去{k}" in text or f"不要{k}" in text:
            remove_cities.append(v)
    if remove_cities:
        out["_remove"] = {"cities": list(dict.fromkeys(remove_cities))}

    # pace
    if any(k in text.lower() for k in ["slow", "relax", "easy", "慢", "轻松"]):
        out["pace"] = "slow"
    elif any(k in text.lower() for k in ["fast", "packed", "紧凑", "赶"]):
        out["pace"] = "fast"

    # interests
    if any(k in text.lower() for k in ["food", "eat", "美食", "吃"]):
        out["interests"].append("food")
    if any(k in text.lower() for k in ["history", "museum", "历史", "博物馆"]):
        out["interests"].append("history")
    if any(k in text.lower() for k in ["nature", "mountain", "lake", "自然", "徒步"]):
        out["interests"].append("nature")
    if any(k in text.lower() for k in ["shopping", "mall", "购物"]):
        out["interests"].append("shopping")

    # 否定兴趣（最小可用）
    remove_interests = []
    if any(k in text.lower() for k in ["no shopping", "not shopping", "don't shopping", "不购物", "不想购物"]):
        remove_interests.append("shopping")
    if remove_interests:
        out["_remove"] = dict(out.get("_remove") or {})
        out["_remove"]["interests"] = list(dict.fromkeys(remove_interests))

    # language (very rough)
    if any("\u4e00" <= ch <= "\u9fff" for ch in text):
        out["language"] = "zh"
    else:
        out["language"] = "en"

    # party (very rough)
    lower = text.lower()
    if any(k in lower for k in ["couple", "honeymoon", "we are two", "2 of us", "情侣", "两个人"]):
        out["party"] = {"size": 2, "composition": "couple"}
    elif any(k in lower for k in ["family", "kids", "child", "children", "亲子", "带娃", "小孩"]):
        out["party"] = {"composition": "family", "kids": True}
    elif any(k in lower for k in ["solo", "by myself", "alone", "我一个人"]):
        out["party"] = {"size": 1, "composition": "solo"}

    msize = re.search(r"(\d+)\s*(people|persons|adults|travellers|travelers)", lower)
    if msize:
        out["party"] = dict(out["party"] or {})
        out["party"]["size"] = int(msize.group(1))

    # constraints - dietary
    dietary = {}
    if any(k in lower for k in ["allergy", "peanut", "nuts"]):
        dietary.setdefault("allergies", []).append("nuts")
    if any(k in lower for k in ["no spicy", "not spicy", "can't eat spicy", "不吃辣", "不要辣"]):
        dietary["no_spicy"] = True
    if any(k in lower for k in ["halal", "muslim", "清真"]):
        dietary["halal"] = True
    if dietary:
        out["constraints"] = dict(out["constraints"] or {})
        out["constraints"]["dietary"] = dietary

    # constraints - walking level
    if any(k in lower for k in ["walk a lot", "hiking", "long walk", "徒步", "暴走"]):
        out["constraints"] = dict(out["constraints"] or {})
        out["constraints"]["walking_level"] = "high"
    if any(k in lower for k in ["easy walk", "not too much walking", "少走", "体力一般"]):
        out["constraints"] = dict(out["constraints"] or {})
        out["constraints"]["walking_level"] = "low"

    # constraints - must_see / must_avoid (minimal POI dictionary)
    poi_map = {
        "forbidden city": "Forbidden City",
        "palace museum": "Forbidden City",
        "故宫": "Forbidden City",
        "great wall": "Great Wall",
        "长城": "Great Wall",
        "terracotta": "Terracotta Army",
        "兵马俑": "Terracotta Army",
        "bund": "The Bund",
        "外滩": "The Bund",
    }
    must_see = []
    must_avoid = []
    tl = text.lower()
    for k, v in poi_map.items():
        if k.lower() in tl:
            # if negated -> avoid or remove
            if any(
                n in tl
                for n in [
                    f"not {k.lower()}",
                    f"no {k.lower()}",
                    f"don't {k.lower()}",
                ]
            ) or (f"不去{k}" in text) or (f"不要{k}" in text):
                must_avoid.append(v)
            else:
                must_see.append(v)

    # heuristics: "must see" / "必去" boosts must_see; "avoid" / "不去" boosts must_avoid
    if must_see or must_avoid:
        out["constraints"] = dict(out["constraints"] or {})
        if must_see:
            out["constraints"]["must_see"] = list(dict.fromkeys(must_see))
        if must_avoid:
            out["constraints"]["must_avoid"] = list(dict.fromkeys(must_avoid))

        # if user explicitly says "not X", also remove from existing must_see
        remove_must_see = [x for x in must_avoid if x in ["Forbidden City", "Great Wall", "Terracotta Army", "The Bund"]]
        if remove_must_see:
            out["_remove"] = dict(out.get("_remove") or {})
            out["_remove"]["constraints.must_see"] = list(dict.fromkeys(remove_must_see))

    return out


def normalize_llm_slots(slots: dict) -> dict:
    """
    兜底规范化：避免 LLM 输出类型漂移导致下游崩溃。
    """
    slots = dict(slots or {})
    if not isinstance(slots.get("cities"), list):
        slots["cities"] = []
    if slots.get("days") is not None:
        try:
            slots["days"] = int(slots["days"])
        except Exception:
            slots["days"] = None
    if not isinstance(slots.get("interests"), list):
        slots["interests"] = []
    if not isinstance(slots.get("party"), dict):
        slots["party"] = {}
    if not isinstance(slots.get("language"), str) or not slots.get("language"):
        slots["language"] = None
    if not isinstance(slots.get("constraints"), dict):
        slots["constraints"] = {}
    if "_clear" in slots and not isinstance(slots.get("_clear"), list):
        slots["_clear"] = []
    if "_remove" in slots and not isinstance(slots.get("_remove"), dict):
        slots["_remove"] = {}
    return slots


def normalize_llm_confidence(conf: dict) -> dict:
    conf = dict(conf or {})
    keys = ["cities", "days", "pace", "budget_level", "interests", "party", "language", "constraints"]
    out = {}
    for k in keys:
        v = conf.get(k, 0.0)
        try:
            v = float(v)
        except Exception:
            v = 0.0
        if v < 0:
            v = 0.0
        if v > 1:
            v = 1.0
        out[k] = v
    return out


def extract_slots(text: str, prior_slot_state: dict | None) -> Tuple[dict, dict, dict | None]:
    """
    返回 (slots, confidence, ask)
    - 若启用 LLM：优先使用 LLM JSON 抽取（带置信度与 ask）
    - 否则：回退规则抽取（置信度用启发式 1/0）
    """
    provider = build_provider()
    if provider is not None:
        system = slot_extraction_system_prompt()
        user = slot_extraction_user_prompt(text, prior_slot_state)
        try:
            raw = provider.chat_json_sync(system, user)  # type: ignore[attr-defined]
            slots = normalize_llm_slots(raw.get("slots", {}))
            conf = normalize_llm_confidence(raw.get("confidence", {}))
            ask = raw.get("ask")
            if ask is not None and (not isinstance(ask, dict) or len(ask.get("options", [])) < 2):
                ask = None
            # 否定/改口操作：把 clear/remove 填到 slots 的特殊字段中，交给 merge_slot_state 处理
            if isinstance(raw.get("clear"), list):
                slots["_clear"] = raw.get("clear")
            if isinstance(raw.get("remove"), dict):
                slots["_remove"] = raw.get("remove")
            return slots, conf, ask
        except Exception:
            # 任意失败直接回退规则版
            pass

    slots = extract_slots_rule(text)
    conf = {
        "cities": 1.0 if slots.get("cities") else 0.0,
        "days": 1.0 if slots.get("days") else 0.0,
        "pace": 1.0 if slots.get("pace") else 0.0,
        "budget_level": 1.0 if slots.get("budget_level") else 0.0,
        "interests": 1.0 if slots.get("interests") else 0.0,
        "party": 1.0 if slots.get("party") else 0.0,
        "language": 1.0 if slots.get("language") else 0.0,
        "constraints": 1.0 if slots.get("constraints") else 0.0,
    }
    return slots, conf, None


def build_question(slot_key: str, language: str | None) -> dict:
    """
    生成“少问问题、多选项”的标准 ask action。
    slot_key 是我们本轮要补齐的关键字段。
    """
    lang = (language or "en").lower()
    zh = lang.startswith("zh")

    if slot_key == "cities":
        return {
            "type": "ask",
            "slot_key": "cities",
            "question": "你想去哪个城市？" if zh else "Which city are you visiting?",
            "options": ["北京", "上海", "成都", "其他"] if zh else ["Beijing", "Shanghai", "Chengdu", "Other"],
        }
    if slot_key == "days":
        return {
            "type": "ask",
            "slot_key": "days",
            "question": "你计划玩几天？" if zh else "How many days is your trip?",
            "options": ["3", "5", "7", "其他"] if zh else ["3", "5", "7", "Other"],
        }
    if slot_key == "pace":
        return {
            "type": "ask",
            "slot_key": "pace",
            "question": "你更偏好旅行节奏？" if zh else "What pace do you prefer?",
            "options": ["轻松", "适中", "紧凑", "不确定"] if zh else ["Slow", "Medium", "Fast", "Not sure"],
        }
    if slot_key == "budget_level":
        return {
            "type": "ask",
            "slot_key": "budget_level",
            "question": "预算档位大概？" if zh else "What budget level?",
            "options": ["经济", "中等", "高端", "不确定"] if zh else ["Low", "Mid", "High", "Not sure"],
        }
    if slot_key == "interests":
        return {
            "type": "ask",
            "slot_key": "interests",
            "question": "这次更想侧重什么？" if zh else "What do you want to focus on?",
            "options": ["美食", "历史", "自然", "混合"] if zh else ["Food", "History", "Nature", "Mixed"],
        }
    if slot_key == "party":
        # “人数+关系”组合（2~4选项），降低追问次数
        return {
            "type": "ask",
            "slot_key": "party",
            "question": "同行人情况更接近哪种？" if zh else "Who are you traveling with?",
            "options": (
                ["独自（1人）", "情侣/夫妻（2人）", "亲子/家庭（带娃）", "朋友/同事（3人+）"]
                if zh
                else ["Solo (1)", "Couple (2)", "Family (kids)", "Friends/Group (3+)"]
            ),
        }
    return {"type": "ask", "slot_key": slot_key, "question": "Please clarify", "options": ["OK"]}


def decide_next_action(slot_state: dict, intent: INTENT) -> dict:
    """
    关键策略：
    - plan 只要求 cities + days，缺一个就问一个
    - book_hotel：除 cities/days 外，需要 check_in/check_out/adults（当前MVP未做日期抽取，先不强制）
    # - rfp：需要 city + party（人数/关系高影响）；language 默认可从输入推断
    """
    # 每轮最多问 1 个：按照优先级选择最关键的缺口
    lang = slot_state.get("language")

    def missing(key: str) -> bool:
        v = slot_state.get(key)
        if v is None:
            return True
        if isinstance(v, list) and len(v) == 0:
            return True
        if isinstance(v, dict) and len(v) == 0:
            return True
        if isinstance(v, str) and not v.strip():
            return True
        return False

    # 避免重复追问：记录上一次问的 slot_key
    last_asked = slot_state.get("_last_asked")

    # 置信度（如果没有则当作 1.0/0.0）
    conf = dict(slot_state.get("_confidence") or {})
    c_cities = float(conf.get("cities", 1.0 if slot_state.get("cities") else 0.0))
    c_days = float(conf.get("days", 1.0 if slot_state.get("days") else 0.0))
    c_party = float(conf.get("party", 1.0 if slot_state.get("party") else 0.0))

    # 关键缺口阈值：低于该值就倾向追问
    THRESH_KEY = 0.6

    if intent == "plan":
        if (missing("cities") or c_cities < THRESH_KEY) and last_asked != "cities":
            q = build_question("cities", lang)
            q["_set_last_asked"] = "cities"
            return q
        if (missing("days") or c_days < THRESH_KEY) and last_asked != "days":
            q = build_question("days", lang)
            q["_set_last_asked"] = "days"
            return q
        # 非关键字段不追问：先产出行程；同时给一个“可选细化”的轻量建议（每轮最多一个）
        post_actions: list[dict] = []
        if missing("pace"):
            post_actions.append({"type": "suggest_refine", "slot_key": "pace", "suggestion": build_question("pace", lang)})
        elif missing("interests"):
            post_actions.append(
                {"type": "suggest_refine", "slot_key": "interests", "suggestion": build_question("interests", lang)}
            )
        elif missing("budget_level"):
            post_actions.append(
                {"type": "suggest_refine", "slot_key": "budget_level", "suggestion": build_question("budget_level", lang)}
            )
        return {"type": "generate_itinerary_v1", "post_actions": post_actions}

    if intent == "rfp":
        if missing("cities") and last_asked != "cities":
            q = build_question("cities", lang)
            q["_set_last_asked"] = "cities"
            return q
        # 高影响：party（人数/关系）缺失或低置信度时优先追问
        if (missing("party") or c_party < THRESH_KEY) and last_asked != "party":
            q = build_question("party", lang)
            q["_set_last_asked"] = "party"
            return q
        # language 可缺省为输入推断值（rule里会给）
        return {"type": "ready_for_rfp"}

    if intent == "book_hotel":
        if missing("cities"):
            return build_question("cities", lang)
        return {"type": "ready_for_hotel_search"}

    return {"type": "generate_itinerary_v1"}


def generate_itinerary_v1(cities: list[str], days: int) -> dict:
    city = cities[0]
    return {
        "version": "v1",
        "days": [
            {"day": i + 1, "city": city, "theme": "Highlights", "time_blocks": []}
            for i in range(days)
        ],
    }


def _rule_reply(action: dict, slots: dict, text: str) -> str:
    """Generate a rule-based reply string for the fallback path."""
    if action.get("type") == "ask":
        opts = " / ".join(action.get("options", []))
        return f"{action['question']} ({opts})"
    if action.get("type") == "generate_itinerary_v1":
        city = slots.get("cities", ["Unknown"])[0]
        days = slots.get("days", "?")
        return f"Got it — generating a {days}-day itinerary for {city}."
    if action.get("type") == "ready_for_hotel_search":
        city = slots.get("cities", ["Unknown"])[0]
        return f"OK, let's find hotels in {city}. Please confirm dates and guests."
    if action.get("type") == "ready_for_rfp":
        city = slots.get("cities", ["Unknown"])[0]
        return f"OK, I can start a custom service RFP for {city}. What do you need: guide / car / tickets?"
    return f"ACK: {text}"


def orchestrate(text: str, prior_slot_state: dict | None, use_llm: bool = True) -> OrchestrateResult:
    """
    v2.4: Single LLM call for full orchestration — intent detection, slot extraction,
    natural reply generation, and itinerary creation when slots are sufficient.

    Falls back to rule-based path when LLM is unavailable, disabled, or fails.
    """
    provider = build_provider() if use_llm else None

    if provider is not None:
        try:
            system = chat_orchestrate_system_prompt()
            prior = prior_slot_state or {}
            prior_clean = {k: v for k, v in prior.items() if not k.startswith("_")}
            user = f"用户发言：\n{text}\n\n已知的历史信息：\n{prior_clean}\n\n请按JSON格式输出完整结果。"
            raw = provider.chat_json_sync(system, user)

            intent = raw.get("intent", "plan")
            reply = raw.get("reply", f"Got it! Let me help you plan your trip.")
            slots = normalize_llm_slots(raw.get("slots", {}))
            conf = normalize_llm_confidence(raw.get("confidence", {}))
            ask = raw.get("ask")
            itinerary = raw.get("itinerary")
            clear_list = raw.get("clear")
            remove_map = raw.get("remove")

            # Validate ask
            if ask is not None and (not isinstance(ask, dict) or len(ask.get("options", [])) < 2):
                ask = None

            return OrchestrateResult(
                intent=intent,
                reply=reply,
                slots=slots,
                confidence=conf,
                ask=ask if isinstance(ask, dict) else None,
                itinerary=itinerary if isinstance(itinerary, dict) else None,
                clear=clear_list if isinstance(clear_list, list) else None,
                remove=remove_map if isinstance(remove_map, dict) else None,
            )
        except Exception:
            pass  # fall through to rule-based

    # Rule-based fallback
    slots, confidence, llm_ask = extract_slots(text, prior_slot_state)
    intent = detect_intent(text)
    action = llm_ask or decide_next_action(slots, intent)
    reply = _rule_reply(action, slots, text)

    itinerary = None
    if action.get("type") == "generate_itinerary_v1":
        itinerary = generate_itinerary_v1(slots.get("cities", []), slots.get("days", 1))

    return OrchestrateResult(
        intent=intent,
        reply=reply,
        slots=slots,
        confidence=confidence,
        ask=action if action.get("type") == "ask" else None,
        itinerary=itinerary,
        actions=[action],
    )
