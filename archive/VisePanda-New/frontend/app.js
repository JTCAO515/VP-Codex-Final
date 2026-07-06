// VisePanda shared frontend logic — simplified auth flow
// Supabase config is pre-fetched by inline script in HTML before this module loads

const MAX_GUEST_TRIPS = 3;

// ── Guest helpers ────────────────────────────────────────────

export function getGuestId() {
  const k = "cta_guest_id";
  let v = localStorage.getItem(k);
  if (!v) { v = crypto.randomUUID(); localStorage.setItem(k, v); }
  return v;
}

export function newTripId() { return "t_" + crypto.randomUUID(); }

export function getGuestTrips() {
  try { return JSON.parse(localStorage.getItem("cta_guest_trips") || "[]"); }
  catch { return []; }
}

export function saveGuestTripMeta(meta) {
  const arr = getGuestTrips();
  const next = [meta, ...arr.filter(x => x.trip_id !== meta.trip_id)].slice(0, MAX_GUEST_TRIPS);
  localStorage.setItem("cta_guest_trips", JSON.stringify(next));
}

export function getGuestMessages(tripId) {
  try { return JSON.parse(localStorage.getItem("cta_guest_messages_" + tripId) || "[]"); }
  catch { return []; }
}

export function appendGuestMessage(tripId, msg) {
  const arr = getGuestMessages(tripId);
  arr.push({ ...msg, ts: Date.now() });
  localStorage.setItem("cta_guest_messages_" + tripId, JSON.stringify(arr.slice(-400)));
}

// ── Supabase ──────────────────────────────────────────────────

let _supabase = null;
let _initAttempted = false;

export async function getSupabase() {
  if (_supabase) return _supabase;
  if (_initAttempted) return null;

  // Wait for config pre-fetch to complete
  if (window.__CONFIG_READY__) await window.__CONFIG_READY__;

  const cfg = window.__SUPABASE_CONFIG__;
  if (!cfg || !cfg.supabase_url || !cfg.supabase_anon_key) {
    _initAttempted = true;
    return null;
  }

  try {
    const { createClient } = await import("https://esm.sh/@supabase/supabase-js@2");
    _supabase = createClient(cfg.supabase_url, cfg.supabase_anon_key);
    return _supabase;
  } catch (e) {
    console.error("Supabase init failed:", e);
    _initAttempted = true;
    return null;
  }
}

export async function getSession() {
  const sb = await getSupabase();
  if (!sb) return null;
  try {
    const { data } = await sb.auth.getSession();
    return data.session || null;
  } catch { return null; }
}

export async function signInWithGoogle() {
  const sb = await getSupabase();
  if (!sb) {
    alert("Supabase not configured. Please add SUPABASE_URL and SUPABASE_ANON_KEY to Vercel environment variables.");
    return;
  }
  const redirectTo = window.location.origin + "/auth/callback";
  await sb.auth.signInWithOAuth({ provider: "google", options: { redirectTo } });
}

export async function signOut() {
  const sb = await getSupabase();
  if (!sb) return;
  await sb.auth.signOut();
}

// ── API ───────────────────────────────────────────────────────

export function apiUrl(path) {
  if (window.__API_BASE__) return window.__API_BASE__ + path;
  return "/api" + path;
}

export async function apiFetch(path, { method = "GET", headers = {}, body } = {}) {
  const session = await getSession();
  if (session?.access_token) {
    headers["Authorization"] = "Bearer " + session.access_token;
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
  return new URL(window.location.href).searchParams.get(name);
}

// ── Auth UI ───────────────────────────────────────────────────

export function setTopRightAuthUI({ containerId = "authArea" } = {}) {
  const el = document.getElementById(containerId);
  if (!el) return;

  (async () => {
    const sb = await getSupabase();
    const session = await getSession();

    if (!sb || !session) {
      el.innerHTML = '<button class="ghost" id="btnSignIn" data-i18n="app.sign_in">Sign in</button>';
      const btn = document.getElementById("btnSignIn");
      if (btn) btn.onclick = signInWithGoogle;
      return;
    }

    el.innerHTML = '<span class="badge">Signed in</span><button class="ghost" id="btnSignOut" data-i18n="chat.sign_out">Sign out</button>';
    const btn = document.getElementById("btnSignOut");
    if (btn) btn.onclick = async () => { await signOut(); location.reload(); };
  })();
}

export function requireLoginPrompt() {
  alert("This feature requires login. Please sign in with Google first.");
}

export function fmtTime(iso) {
  if (!iso) return "";
  try { return new Date(iso).toLocaleString(); }
  catch { return iso; }
}
