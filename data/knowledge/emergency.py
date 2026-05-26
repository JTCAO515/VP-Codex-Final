"""Emergency assistance info for foreign travelers in China"""

EMERGENCY = {
    "phone": {
        "police": {"number": "110", "description": "Police (报警) — theft, assault, robbery"},
        "fire": {"number": "119", "description": "Fire Department (火警)"},
        "ambulance": {"number": "120", "description": "Ambulance / Medical Emergency (急救)"},
        "traffic": {"number": "122", "description": "Traffic Accident (交通事故)"},
        "directory": {"number": "114", "description": "Directory Assistance (查号台)"},
    },
    "common_emergencies": {
        "lost_passport": {
            "title": "护照丢失 (Lost Passport)",
            "steps": [
                "1. 立即去最近的派出所报案，拿到《护照遗失报案回执》",
                "2. 去你国家的大使馆/领事馆申请旅行证 (Emergency Travel Document)",
                "3. 去出入境管理局办理签证补办手续",
                "4. 如果时间紧急，旅行证可以代替护照用于离境"
            ],
            "tip": "建议随身带护照复印件或存在手机里，补办时有用"
        },
        "medical_emergency": {
            "title": "医疗急救 (Medical Emergency)",
            "steps": [
                "1. 拨打120叫救护车 (免费急救电话)",
                "2. 告诉接线员你的位置和症状（可以说简单英文）",
                "3. 大的三甲医院都有国际部/涉外门诊",
                "4. 带护照去急诊挂号，先治疗后付费",
                "5. 联系你的旅行保险公司"
            ],
            "tip": "北京协和医院国际部、上海华山医院、广州中山一院都有英文服务"
        },
        "arrested": {
            "title": "被拘留/被捕 (Arrested/Detained)",
            "steps": [
                "1. 你有权联系你的大使馆 — 要求通知使馆",
                "2. 不要签署你不明白的中文文件",
                "3. 要求翻译 — 法律规定必须提供翻译",
                "4. 联系你的旅行保险和法律援助"
            ],
            "tip": "中国签证规定：所有外国人在中国必须遵守中国法律，不知道法律不是借口"
        },
        "natural_disaster": {
            "title": "自然灾害 (Natural Disaster)",
            "steps": [
                "1. 听从当地政府和景区工作人员的指示",
                "2. 关注中国地震台网或当地天气预警",
                "3. 联系你的大使馆登记你所在位置",
                "4. 保持手机电量，使用离线地图"
            ],
            "tip": "日本/台湾游客来大陆最常遇到台风/地震，提前关注天气预报"
        }
    },
    "embassies": {
        "us": {
            "country": "美国 (United States)",
            "phone": "010-8531-3000",
            "emergency": "010-8531-4000 (after hours)",
            "address": "北京朝阳区安家楼路55号",
            "website": "https://china.usembassy-china.org.cn",
            "cities": ["北京", "上海", "广州", "成都", "沈阳", "武汉"]
        },
        "uk": {
            "country": "英国 (United Kingdom)",
            "phone": "010-8529-6600",
            "emergency": "010-8529-6600 (24h)",
            "address": "北京朝阳区建国门外光华路11号",
            "website": "https://www.gov.uk/world/china",
            "cities": ["北京", "上海", "广州", "重庆", "武汉"]
        },
        "au": {
            "country": "澳大利亚 (Australia)",
            "phone": "010-5140-4111",
            "emergency": "010-5140-4248",
            "address": "北京朝阳区亮马河南路14号塔园外交办公楼",
            "website": "https://china.embassy.gov.au",
            "cities": ["北京", "上海", "广州", "成都"]
        },
        "ca": {
            "country": "加拿大 (Canada)",
            "phone": "010-5139-4000",
            "emergency": "010-5139-4000",
            "address": "北京朝阳区亮马桥路10号",
            "website": "https://www.international.gc.ca/china",
            "cities": ["北京", "上海", "广州", "重庆"]
        },
        "sg": {
            "country": "新加坡 (Singapore)",
            "phone": "010-6532-1115",
            "emergency": "010-6532-1115",
            "address": "北京朝阳区亮马桥路42号",
            "website": "https://www.mfa.gov.sg/beijing",
            "cities": ["北京", "上海", "广州", "厦门", "成都"]
        },
        "de": {
            "country": "德国 (Germany)",
            "phone": "010-8532-9000",
            "emergency": "010-8532-9000",
            "address": "北京朝阳区亮马桥路52号",
            "website": "https://china.diplo.de",
            "cities": ["北京", "上海", "广州", "成都"]
        },
        "fr": {
            "country": "法国 (France)",
            "phone": "010-8531-2000",
            "emergency": "010-8531-2000",
            "address": "北京朝阳区亮马桥路47号",
            "website": "https://cn.ambafrance.org",
            "cities": ["北京", "上海", "广州", "成都"]
        },
        "jp": {
            "country": "日本 (Japan)",
            "phone": "010-6532-2361",
            "emergency": "010-6532-2361",
            "address": "北京朝阳区亮马桥路1号",
            "website": "https://www.cn.emb-japan.go.jp",
            "cities": ["北京", "上海", "广州", "青岛", "重庆", "沈阳"]
        },
        "kr": {
            "country": "韩国 (South Korea)",
            "phone": "010-8531-0700",
            "emergency": "010-8531-0700",
            "address": "北京朝阳区亮马桥路26号",
            "website": "https://overseas.mofa.go.kr/cn-zh",
            "cities": ["北京", "上海", "广州", "青岛", "成都", "西安"]
        },
    },
}

# Helper phrase: "Call 110" in Chinese for emergency cards
def format_emergency_phone_numbers() -> str:
    lines = ["## 紧急电话 (Emergency Numbers in China)", ""]
    for key, info in EMERGENCY["phone"].items():
        lines.append(f"- {info['number']}: {info['description']}")
    lines.append("")
    lines.append("所有电话都免费拨打。接线员可能只说中文，但会说'speak English please'通常能找到英文接线员。")
    return "\n".join(lines)


def get_embassy(country_code: str) -> dict | None:
    """Get embassy info by country code (us, uk, au, etc.)"""
    return EMERGENCY["embassies"].get(country_code.lower())


def format_embassy_summary() -> str:
    """Compact embassy list for system prompt"""
    lines = ["## 主要国家驻华大使馆/领事馆"]
    for code, emb in EMERGENCY["embassies"].items():
        cities = "、".join(emb["cities"])
        lines.append(f"- {emb['country']}: {emb['phone']} ({cities})")
    return "\n".join(lines)
