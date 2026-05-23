import {
  apiFetch,
  appendGuestMessage,
  getGuestId,
  getGuestMessages,
  getGuestTrips,
  getSession,
  requireLoginPrompt,
  fmtTime,
  newTripId,
  qs,
  saveGuestTripMeta,
  setTopRightAuthUI,
} from "./app.js";
import { initI18n, renderLangSwitcher, t } from "./i18n.js";

const $ = (id) => document.getElementById(id);

function escapeHtml(s) {
  return (s || "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;", "`": "&#96;" }[c]));
}

// ── Markdown ─────────────────────────────────────────────────

function simpleMarkdown(text) {
  let html = escapeHtml(text);
  html = html.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");
  html = html.replace(/\*(.+?)\*/g, "<em>$1</em>");
  html = html.replace(/`([^`]+)`/g, "<code>$1</code>");
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');
  html = html.replace(/\n\n/g, "</p><p>");
  html = html.replace(/\n/g, "<br>");
  return "<p>" + html + "</p>";
}

// ── Chat History ─────────────────────────────────────────────

let _historyCache = [];

function pushHistory(role, content) {
  _historyCache.push({ role, content });
  if (_historyCache.length > 100) _historyCache.shift();
}

function getHistory() {
  return _historyCache;
}

// ── Quick Replies ────────────────────────────────────────────

function renderQuickReplies(suggestions, inputId) {
  const existing = document.getElementById("ap-quick-replies");
  if (existing) existing.remove();
  if (!suggestions || !suggestions.length) return;

  const wrap = document.createElement("div");
  wrap.id = "ap-quick-replies";
  wrap.style.cssText = "display:flex;gap:6px;flex-wrap:wrap;padding:6px 0;";

  suggestions.forEach((text) => {
    const chip = document.createElement("button");
    chip.textContent = text;
    chip.style.cssText =
      "font-size:11px;padding:5px 10px;border-radius:999px;border:1px solid rgba(125,211,252,.2);background:rgba(125,211,252,.06);color:rgba(255,255,255,.8);cursor:pointer;white-space:nowrap;";
    chip.onmouseenter = () => { chip.style.background = "rgba(125,211,252,.14)"; };
    chip.onmouseleave = () => { chip.style.background = "rgba(125,211,252,.06)"; };
    chip.onclick = () => {
      const input = document.getElementById(inputId);
      if (input) {
        input.value = text;
        input.form.dispatchEvent(new Event("submit", { cancelable: true }));
      }
    };
    wrap.appendChild(chip);
  });

  const thread = $("thread");
  thread.appendChild(wrap);
  thread.scrollTop = thread.scrollHeight;
}

// ── Render ───────────────────────────────────────────────────

function renderMessage(role, content, isMarkdown = false) {
  const wrap = document.createElement("div");
  wrap.className = `msg ${role}`;
  const inner = isMarkdown ? simpleMarkdown(content) : escapeHtml(content);
  wrap.innerHTML = `<div class="bubble">${inner}</div>`;
  $("thread").appendChild(wrap);
  $("thread").scrollTop = $("thread").scrollHeight;
  return wrap;
}

function renderStreamingBubble(role) {
  const wrap = document.createElement("div");
  wrap.className = `msg ${role}`;
  wrap.innerHTML = `<div class="bubble" id="streaming-bubble"><span class="cursor-blink">▊</span></div>`;
  $("thread").appendChild(wrap);
  $("thread").scrollTop = $("thread").scrollHeight;
  return wrap.querySelector("#streaming-bubble");
}

// ── SSE Streaming ────────────────────────────────────────────

async function sendMessageStreaming(tripId, text) {
  renderMessage("user", text);
  pushHistory("user", text);
  if (!(await getSession())?.access_token) {
    appendGuestMessage(tripId, { role: "user", content: text });
  }

  const bubble = renderStreamingBubble("bot");
  let fullText = "";
  let suggestions = [];

  // Remove existing quick replies
  const oldQR = document.getElementById("ap-quick-replies");
  if (oldQR) oldQR.remove();

  try {
    const session = await getSession();
    const guestId = getGuestId();
    const url = session?.access_token
      ? `${window.__API_BASE__}/chat/stream` || "/api/chat/stream"
      : (window.__API_BASE__ ? `${window.__API_BASE__}/chat/stream` : "/api/chat/stream");

    const resp = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        trip_id: tripId,
        text,
        guest_id: session?.access_token ? undefined : guestId,
        history: getHistory().slice(0, -1),
      }),
    });

    const reader = resp.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      const lines = buffer.split("\n");
      buffer = lines.pop() || "";

      for (const line of lines) {
        if (!line.startsWith("data: ")) continue;
        const data = line.slice(6);
        if (data === "[DONE]") continue;
        try {
          const parsed = JSON.parse(data);
          if (parsed.token) {
            fullText += parsed.token;
            // Extract suggestions
            const sugMatch = fullText.split("---SUGGESTIONS---");
            if (sugMatch.length > 1) {
              suggestions = sugMatch[1]
                .split("\n")
                .filter((l) => l.startsWith("- "))
                .map((l) => l.slice(2).trim());
            }
            const displayText = sugMatch[0] || fullText;
            bubble.innerHTML = simpleMarkdown(displayText);
          }
          if (parsed.error) {
            bubble.innerHTML = `<span style="color:#fca5a5">Error: ${escapeHtml(parsed.error)}</span>`;
          }
        } catch (_) {}
      }
      $("thread").scrollTop = $("thread").scrollHeight;
    }
  } catch (e) {
    bubble.innerHTML = `<span style="color:#fca5a5">Network error: ${escapeHtml(e.message)}</span>`;
  }

  if (!fullText) fullText = "(no response)";
  pushHistory("assistant", fullText);

  // Save to guest storage
  if (!(await getSession())?.access_token) {
    appendGuestMessage(tripId, { role: "assistant", content: fullText });
    saveGuestTripMeta({
      trip_id: tripId,
      title: `Trip ${tripId.slice(0, 6)}`,
      updated_at: new Date().toISOString(),
    });
  }

  // Show quick replies
  if (suggestions.length > 0) {
    renderQuickReplies(suggestions, "msgInput");
  }
}

// Fallback: non-streaming send (kept for compat)
async function sendMessage(tripId, text) {
  return sendMessageStreaming(tripId, text);
}

// ── Sidebar ──────────────────────────────────────────────────

function renderTripsList(trips, activeId) {
  const el = $("sidebar");
  el.innerHTML = "";
  const top = document.createElement("div");
  top.className = "sideTop";
  top.innerHTML = `<button class="pill" id="btnNew" data-i18n="chat.new_trip">+ New Trip</button>`;
  el.appendChild(top);
  top.querySelector("#btnNew").onclick = () => {
    const id = newTripId();
    const u = new URL(location.href);
    u.searchParams.set("trip", id);
    u.searchParams.delete("q");
    location.href = u.toString();
  };

  trips.forEach((t) => {
    const item = document.createElement("div");
    item.className = "tripItem" + (t.id === activeId ? " active" : "");
    item.innerHTML = `<div class="tripTitle">${escapeHtml(t.title || "Untitled")}</div><div class="tripMeta">${escapeHtml(
      t.updated_at || ""
    )}</div>`;
    item.onclick = () => {
      const u = new URL(location.href);
      u.searchParams.set("trip", t.id);
      u.searchParams.delete("q");
      location.href = u.toString();
    };
    el.appendChild(item);
  });
}

function setActiveTab(tab) {
  document.querySelectorAll(".tab").forEach((b) => b.classList.toggle("active", b.dataset.tab === tab));
}

function setPanel(html) {
  $("panel").innerHTML = html;
}

// ── Details Panel ────────────────────────────────────────────

async function loadTripDetail(tripId) {
  const r = await apiFetch(`/trips/${encodeURIComponent(tripId)}`);
  if (!r.ok) return null;
  const body = await r.json();
  return body.trip || null;
}

async function loadHotelBookings(tripId) {
  const r = await apiFetch(`/trips/${encodeURIComponent(tripId)}/hotel-bookings`);
  if (!r.ok) return [];
  return (await r.json()).bookings || [];
}

async function loadRfps(tripId) {
  const r = await apiFetch(`/trips/${encodeURIComponent(tripId)}/rfps`);
  if (!r.ok) return [];
  return (await r.json()).rfps || [];
}

async function loadOrders(tripId) {
  const r = await apiFetch(`/trips/${encodeURIComponent(tripId)}/service-orders`);
  if (!r.ok) return [];
  return (await r.json()).orders || [];
}

async function renderDetailsItinerary(tripId) {
  const session = await getSession();
  if (!session?.access_token) {
    setPanel(
      `<div class="card"><h4>Itinerary</h4><div class="kv"><div>Tip</div><div>Guest trips only save chat locally. Log in to use the full itinerary planner.</div></div></div>`
    );
    return;
  }
  const r = await apiFetch(`/trips/${encodeURIComponent(tripId)}/itinerary`);
  const data = r.ok ? await r.json() : null;
  if (!data) {
    setPanel(
      `<div class="card"><h4>Itinerary</h4><div class="kv"><div>Status</div><div>Not found / no access</div></div></div>`
    );
    return;
  }
  $("dTitle").textContent = data.title || "Trip";
  $("dMeta").textContent = `${(data.cities || []).join(", ")}  ·  ${data.start_date || "?"} — ${data.end_date || "?"}`;
  setPanel(`<div id="planner-root"></div>`);

  const mod = await import("./itinerary-planner.js");
  mod.renderPlanner("planner-root", tripId, data.itinerary || { days: [] });
}

async function renderDetailsHotel(tripId) {
  const session = await getSession();
  if (!session?.access_token) {
    requireLoginPrompt();
    setPanel(
      `<div class="card"><h4>Hotel</h4><div class="kv"><div>Tip</div><div>Log in to view and manage hotel bookings.</div></div></div>`
    );
    return;
  }
  const bookings = await loadHotelBookings(tripId);
  const rows = bookings
    .map(
      (b) => `
    <div class="card">
      <h4>Booking</h4>
      <div class="kv">
        <div>ID</div><div class="mono">${escapeHtml(b.id)}</div>
        <div>Status</div><div>${escapeHtml(b.status)}</div>
        <div>Offer</div><div class="mono">${escapeHtml(b.offer_id)}</div>
        <div>Time</div><div>${escapeHtml(fmtTime(b.created_at))}</div>
      </div>
      <div style="margin-top:10px; display:flex; gap:8px; justify-content:flex-end;">
        <button class="smallBtn" data-cancel="${escapeHtml(b.id)}">Cancel</button>
      </div>
    </div>`
    )
    .join("");
  setPanel(rows || `<div class="card"><h4>Hotel</h4><div class="kv"><div>Status</div><div>No bookings</div></div></div>`);

  document.querySelectorAll("[data-cancel]").forEach((btn) => {
    btn.onclick = async () => {
      const id = btn.getAttribute("data-cancel");
      const r = await apiFetch(`/hotel/bookings/${encodeURIComponent(id)}:cancel`, { method: "POST" });
      if (!r.ok) alert("Cancel failed");
      await renderDetailsHotel(tripId);
    };
  });
}

async function renderDetailsOrders(tripId) {
  const session = await getSession();
  if (!session?.access_token) {
    requireLoginPrompt();
    setPanel(
      `<div class="card"><h4>RFP & Orders</h4><div class="kv"><div>Tip</div><div>Log in to view quotes and service orders.</div></div></div>`
    );
    return;
  }
  const rfps = await loadRfps(tripId);
  const orders = await loadOrders(tripId);

  const rfpHtml = rfps
    .map(
      (r) => `
    <div class="card">
      <h4>RFP</h4>
      <div class="kv">
        <div>ID</div><div class="mono">${escapeHtml(r.id)}</div>
        <div>Status</div><div>${escapeHtml(r.status)}</div>
        <div>Types</div><div>${escapeHtml((r.service_types || []).join(", "))}</div>
        <div>Time</div><div>${escapeHtml(fmtTime(r.created_at))}</div>
      </div>
      <div style="margin-top:10px; display:flex; gap:8px; justify-content:flex-end;">
        <button class="smallBtn" data-rfp="${escapeHtml(r.id)}">View quotes</button>
      </div>
    </div>`
    )
    .join("");

  const orderHtml = orders
    .map(
      (o) => `
    <div class="card">
      <h4>Service Order</h4>
      <div class="kv">
        <div>ID</div><div class="mono">${escapeHtml(o.id)}</div>
        <div>Status</div><div>${escapeHtml(o.status)}</div>
        <div>Supplier</div><div class="mono">${escapeHtml(o.supplier_id)}</div>
        <div>Time</div><div>${escapeHtml(fmtTime(o.created_at))}</div>
      </div>
      <div style="margin-top:10px;">
        <div class="mono">${escapeHtml(JSON.stringify(o.fulfillment_info || {}, null, 2))}</div>
      </div>
    </div>`
    )
    .join("");

  setPanel(
    `
    <div class="card"><h4>Service Orders (${orders.length})</h4></div>
    ${orderHtml || `<div class="card"><div class="kv"><div>Status</div><div>No orders</div></div></div>`}
    <div class="card"><h4>RFPs (${rfps.length})</h4></div>
    ${rfpHtml || `<div class="card"><div class="kv"><div>Status</div><div>No RFPs</div></div></div>`}
    `
  );

  document.querySelectorAll("[data-rfp]").forEach((btn) => {
    btn.onclick = async () => {
      const id = btn.getAttribute("data-rfp");
      const r = await apiFetch(`/rfps/${encodeURIComponent(id)}`);
      const body = await r.json();
      alert(JSON.stringify(body, null, 2));
    };
  });
}

// ── Main ─────────────────────────────────────────────────────

async function loadTripsAndMaybeMessages(tripId) {
  const session = await getSession();
  if (session?.access_token) {
    const r = await apiFetch("/trips");
    const body = r.ok ? await r.json() : { trips: [] };
    const trips = body.trips || [];
    renderTripsList(trips, tripId);
    if (tripId) {
      const mr = await apiFetch(`/trips/${encodeURIComponent(tripId)}/messages`);
      if (mr.ok) {
        const mb = await mr.json();
        $("thread").innerHTML = "";
        (mb.messages || []).forEach((m) => {
          const role = m.role === "assistant" ? "bot" : "user";
          renderMessage(role, m.content);
          pushHistory(role, m.content);
        });
      }
    }
    return;
  }

  // guest mode
  const trips = (getGuestTrips() || []).map((x) => ({ id: x.trip_id, title: x.title, updated_at: x.updated_at }));
  renderTripsList(trips, tripId);
  if (tripId) {
    $("thread").innerHTML = "";
    const msgs = getGuestMessages(tripId);
    msgs.forEach((m) => {
      const role = m.role === "assistant" ? "bot" : "user";
      renderMessage(role, m.content);
      pushHistory(role, m.content);
    });
  }
}

async function main() {
  initI18n();
  setTopRightAuthUI({ containerId: "authArea" });
  renderLangSwitcher("langSwitcherArea");

  const tripId = qs("trip") || newTripId();
  const initial = qs("q");

  const btnDetails = $("btnDetails");
  if (btnDetails) {
    btnDetails.onclick = () => {
      const d = document.getElementById("details");
      if (!d) return;
      d.style.display = d.style.display === "none" ? "block" : "none";
    };
  }

  let activeTab = "itinerary";
  document.querySelectorAll(".tab").forEach((b) => {
    b.onclick = async () => {
      activeTab = b.dataset.tab;
      setActiveTab(activeTab);
      if (activeTab === "itinerary") await renderDetailsItinerary(tripId);
      if (activeTab === "hotel") await renderDetailsHotel(tripId);
      if (activeTab === "orders") await renderDetailsOrders(tripId);
    };
  });

  if (!qs("trip")) {
    const u = new URL(location.href);
    u.searchParams.set("trip", tripId);
    location.replace(u.toString());
    return;
  }

  await loadTripsAndMaybeMessages(tripId);
  await renderDetailsItinerary(tripId);

  const form = $("msgForm");
  form.onsubmit = async (e) => {
    e.preventDefault();
    const input = $("msgInput");
    const t = input.value.trim();
    if (!t) return;
    input.value = "";
    await sendMessageStreaming(tripId, t);
    if (activeTab === "itinerary") await renderDetailsItinerary(tripId);
  };

  if (initial) {
    const u = new URL(location.href);
    u.searchParams.delete("q");
    history.replaceState({}, "", u.toString());
    await sendMessageStreaming(tripId, initial);
  }
}

main();
