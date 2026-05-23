// 共享前端逻辑（静态站点）
// - Supabase Auth（Google 登录）
// - guest_id / 本地会话与消息缓存
// - 统一 /api 请求封装（自动加 Bearer）

const MAX_GUEST_TRIPS = 3;

export function getGuestId() {
  const k = "cta_guest_id";
  let v = localStorage.getItem(k);
  if (!v) {
    v = crypto.randomUUID();
    localStorage.setItem(k, v);
  }
  return v;
}

export function newTripId() {
  return "t_" + crypto.randomUUID();
}

export function getGuestTrips() {
  try {
    return JSON.parse(localStorage.getItem("cta_guest_trips") || "[]");
  } catch {
    return [];
  }
}

export function saveGuestTripMeta(meta) {
  const arr = getGuestTrips();
  const next = [meta, ...arr.filter((x) => x.trip_id !== meta.trip_id)].slice(0, MAX_GUEST_TRIPS);
  localStorage.setItem("cta_guest_trips", JSON.stringify(next));
}

export function getGuestMessages(tripId) {
  try {
    return JSON.parse(localStorage.getItem(`cta_guest_messages_${tripId}`) || "[]");
  } catch {
    return [];
  }
}

export function appendGuestMessage(tripId, msg) {
  const arr = getGuestMessages(tripId);
  arr.push({ ...msg, ts: Date.now() });
  const capped = arr.slice(-400);
  localStorage.setItem(`cta_guest_messages_${tripId}`, JSON.stringify(capped));
}

async function fetchPublicConfig() {
  // Dev: try backend directly first, then fall back to relative (production Vercel rewrite)
  const urls = [
    window.__API_BASE__ ? `${window.__API_BASE__}/public-config` : null,
    "/api/public-config"
  ].filter(Boolean);
  for (const url of urls) {
    try {
      const r = await fetch(url);
      if (r.ok) return await r.json();
    } catch (_) {}
  }
  return { supabase_url: "", supabase_anon_key: "" };
}

let _supabase = null;
let _supabaseCfg = null;

export async function getSupabase() {
  if (_supabase) return _supabase;
  _supabaseCfg = await fetchPublicConfig();
  if (!_supabaseCfg.supabase_url || !_supabaseCfg.supabase_anon_key) {
    // 未配置 Supabase：继续以游客模式运行
    return null;
  }
  const { createClient } = await import("https://esm.sh/@supabase/supabase-js@2");
  _supabase = createClient(_supabaseCfg.supabase_url, _supabaseCfg.supabase_anon_key);
  return _supabase;
}

export async function getSession() {
  const sb = await getSupabase();
  if (!sb) return null;
  const { data } = await sb.auth.getSession();
  return data.session || null;
}

export async function signInWithGoogle() {
  const sb = await getSupabase();
  if (!sb) {
    alert("Supabase 未配置：请先在 Vercel 环境变量里设置 SUPABASE_URL / SUPABASE_ANON_KEY。");
    return;
  }
  const redirectTo = `${window.location.origin}/auth/callback`;
  await sb.auth.signInWithOAuth({ provider: "google", options: { redirectTo } });
}

export async function signOut() {
  const sb = await getSupabase();
  if (!sb) return;
  await sb.auth.signOut();
}

export function apiUrl(path) {
  // Dev mode: direct backend URL (no /api rewrite)
  if (window.__API_BASE__) return `${window.__API_BASE__}${path}`;
  // Production: Vercel rewrites /api/* to backend
  return `/api${path}`;
}

export async function apiFetch(path, { method = "GET", headers = {}, body } = {}) {
  const session = await getSession();
  if (session?.access_token) {
    headers["Authorization"] = `Bearer ${session.access_token}`;
  }
  if (body && !(body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }
  const res = await fetch(apiUrl(path), {
    method,
    headers,
    body: body ? (body instanceof FormData ? body : JSON.stringify(body)) : undefined,
  });
  return res;
}

export function qs(name) {
  const u = new URL(window.location.href);
  return u.searchParams.get(name);
}

export function setTopRightAuthUI({ containerId = "authArea" } = {}) {
  const el = document.getElementById(containerId);
  if (!el) return;

  const render = async () => {
    const sb = await getSupabase();
    const session = await getSession();
    if (!sb || !session) {
      el.innerHTML = `<button class="ghost" id="btnSignIn">Sign in</button>`;
      document.getElementById("btnSignIn").onclick = signInWithGoogle;
      return;
    }
    el.innerHTML = `<span class="badge">Signed in</span><button class="ghost" id="btnSignOut">Sign out</button>`;
    document.getElementById("btnSignOut").onclick = async () => {
      await signOut();
      location.reload();
    };
  };

  render();
}

export function requireLoginPrompt() {
  alert("该功能需要登录（Google）。请先点击右上角 Sign in with Google。");
}

export function fmtTime(iso) {
  if (!iso) return "";
  try {
    const d = new Date(iso);
    return d.toLocaleString();
  } catch {
    return iso;
  }
}
