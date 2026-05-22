import {
  apiFetch,
  appendGuestMessage,
  getGuestId,
  getGuestMessages,
  getGuestTrips,
  getSession,
  newTripId,
  qs,
  saveGuestTripMeta,
  setTopRightAuthUI,
} from "./app.js";

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
  top.innerHTML = `<button class="pill" id="btnNew">+ New</button>`;
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
  setTopRightAuthUI({ containerId: "authArea" });

  const tripId = qs("trip") || newTripId();
  const initial = qs("q");

  // ensure url has trip
  if (!qs("trip")) {
    const u = new URL(location.href);
    u.searchParams.set("trip", tripId);
    location.replace(u.toString());
    return;
  }

  await loadTripsAndMaybeMessages(tripId);

  const form = $("msgForm");
  form.onsubmit = async (e) => {
    e.preventDefault();
    const input = $("msgInput");
    const t = input.value.trim();
    if (!t) return;
    input.value = "";
    await sendMessage(tripId, t);
  };

  if (initial) {
    const u = new URL(location.href);
    u.searchParams.delete("q");
    history.replaceState({}, "", u.toString());
    await sendMessage(tripId, initial);
  }
}

main();
