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
  return (s || "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function renderMessage(role, content) {
  const wrap = document.createElement("div");
  wrap.className = `msg ${role}`;
  wrap.innerHTML = `<div class="bubble">${escapeHtml(content)}</div>`;
  $("thread").appendChild(wrap);
  $("thread").scrollTop = $("thread").scrollHeight;
}

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
    item.innerHTML = `<div class="tripTitle">${escapeHtml(t.title || "Untitled")}</div><div class="tripMeta">${escapeHtml(t.updated_at || "")}</div>`;
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
    setPanel(`<div class="card"><h4>Itinerary</h4><div class="kv"><div>Tip</div><div>Guest trips only save chat locally. Log in to use the full itinerary planner.</div></div></div>`);
    return;
  }
  const r = await apiFetch(`/trips/${encodeURIComponent(tripId)}/itinerary`);
  const data = r.ok ? (await r.json()) : null;
  if (!data) {
    setPanel(`<div class="card"><h4>Itinerary</h4><div class="kv"><div>Status</div><div>Not found / no access</div></div></div>`);
    return;
  }
  $("dTitle").textContent = data.title || "Trip";
  $("dMeta").textContent = `${(data.cities || []).join(", ")}  ·  ${data.start_date || "?"} — ${data.end_date || "?"}`;
  setPanel(`<div id="planner-root"></div>`);

  // Load planner module lazily
  const mod = await import("./itinerary-planner.js");
  mod.renderPlanner("planner-root", tripId, data.itinerary || { days: [] });
}

async function renderDetailsHotel(tripId) {
  const session = await getSession();
  if (!session?.access_token) {
    requireLoginPrompt();
    setPanel(`<div class="card"><h4>酒店</h4><div class="kv"><div>提示</div><div>登录后可查看与管理酒店订单。</div></div></div>`);
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
  setPanel(rows || `<div class="card"><h4>酒店</h4><div class="kv"><div>状态</div><div>暂无订单</div></div></div>`);

  document.querySelectorAll("[data-cancel]").forEach((btn) => {
    btn.onclick = async () => {
      const id = btn.getAttribute("data-cancel");
      const r = await apiFetch(`/hotel/bookings/${encodeURIComponent(id)}:cancel`, { method: "POST" });
      if (!r.ok) alert("取消失败");
      await renderDetailsHotel(tripId);
    };
  });
}

async function renderDetailsOrders(tripId) {
  const session = await getSession();
  if (!session?.access_token) {
    requireLoginPrompt();
    setPanel(`<div class="card"><h4>RFP&订单</h4><div class="kv"><div>提示</div><div>登录后可查看询价、报价与服务订单。</div></div></div>`);
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
    <div class="card"><h4>Service Orders（${orders.length}）</h4></div>
    ${orderHtml || `<div class="card"><div class="kv"><div>状态</div><div>暂无订单</div></div></div>`}
    <div class="card"><h4>RFPs（${rfps.length}）</h4></div>
    ${rfpHtml || `<div class="card"><div class="kv"><div>状态</div><div>暂无 RFP</div></div></div>`}
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
        (mb.messages || []).forEach((m) => renderMessage(m.role === "assistant" ? "bot" : "user", m.content));
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
    msgs.forEach((m) => renderMessage(m.role === "assistant" ? "bot" : "user", m.content));
  }
}

async function sendMessage(tripId, text) {
  const session = await getSession();
  const guestId = getGuestId();

  renderMessage("user", text);
  if (!session?.access_token) {
    appendGuestMessage(tripId, { role: "user", content: text });
  }

  const payload = session?.access_token ? { trip_id: tripId, text } : { trip_id: tripId, text, guest_id: guestId };
  const r = await apiFetch("/chat/messages", { method: "POST", body: payload });
  const body = await r.json();
  const reply = body.reply || "(no reply)";
  renderMessage("bot", reply);
  if (!session?.access_token) {
    appendGuestMessage(tripId, { role: "assistant", content: reply });
    // update trip meta locally
    saveGuestTripMeta({
      trip_id: tripId,
      title: (body.trip && body.trip.title) || `Trip ${tripId.slice(0, 6)}`,
      updated_at: new Date().toISOString(),
    });
  }
}

async function main() {
  initI18n();
  setTopRightAuthUI({ containerId: "authArea" });
  renderLangSwitcher("langSwitcherArea");

  const tripId = qs("trip") || newTripId();
  const initial = qs("q");

  // mobile: toggle details
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

  // ensure url has trip
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
    await sendMessage(tripId, t);
    // refresh itinerary tab after new messages (might have new itinerary versions)
    if (activeTab === "itinerary") await renderDetailsItinerary(tripId);
  };

  if (initial) {
    const u = new URL(location.href);
    u.searchParams.delete("q");
    history.replaceState({}, "", u.toString());
    await sendMessage(tripId, initial);
  }
}

main();
