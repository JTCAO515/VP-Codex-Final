// itinerary-planner.js — v3.2 Drag-and-drop day planner
// Zero dependencies except Leaflet CDN (loaded lazily)

let _plannerState = { days: [] };
let _plannerTripId = "";
let _saveTimer = null;
let _mapInstance = null;
let _activeDay = 0;
let _mapVisible = false;

const DAY_NAMES = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const TYPE_COLORS = {
  sightseeing: "#7dd3fc",
  food: "#fbbf24",
  transport: "#a78bfa",
  hotel: "#34d399",
  activity: "#f472b6",
  other: "#94a3b8",
};

function uid() {
  return "act_" + Math.random().toString(36).slice(2, 10);
}

function escapeHtml(s) {
  return (s || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function fmtTime(isoTime) {
  if (!isoTime) return "";
  const [h, m] = isoTime.split(":");
  return `${h}:${m}`;
}

// ── Persistence ──────────────────────────────────────────────

async function autoSave() {
  if (!_plannerTripId) return;
  const session = await (await import("./app.js")).getSession();
  if (!session?.access_token) return;

  try {
    await (
      await import("./app.js")
    ).apiFetch(`/trips/${encodeURIComponent(_plannerTripId)}/itinerary`, {
      method: "PUT",
      body: { days: _plannerState.days },
    });
  } catch (_) {
    // Silently fail autosave
  }
}

function debouncedSave() {
  clearTimeout(_saveTimer);
  _saveTimer = setTimeout(autoSave, 1000);
}

// ── Map ──────────────────────────────────────────────────────

async function initMap(containerId) {
  if (_mapInstance) return _mapInstance;
  await new Promise((res) => {
    if (window.L) return res();
    const link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = "https://unpkg.com/leaflet@1.9/dist/leaflet.css";
    document.head.appendChild(link);
    const script = document.createElement("script");
    script.src = "https://unpkg.com/leaflet@1.9/dist/leaflet.js";
    script.onload = res;
    document.head.appendChild(script);
  });

  const el = document.getElementById(containerId);
  if (!el) return null;
  _mapInstance = L.map(el).setView([35.86, 104.19], 4);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 18,
    attribution: "&copy; OpenStreetMap",
  }).addTo(_mapInstance);
  return _mapInstance;
}

function centerMapOn(lat, lng, name) {
  if (!_mapInstance) return;
  _mapInstance.setView([lat, lng], 13);
  _mapInstance.eachLayer((layer) => {
    if (layer._isMarker) _mapInstance.removeLayer(layer);
  });
  const marker = L.marker([lat, lng]).addTo(_mapInstance);
  marker._isMarker = true;
  marker.bindPopup(escapeHtml(name)).openPopup();
}

// ── Rendering ────────────────────────────────────────────────

function renderActivityCard(act, dayIndex, actIndex) {
  const color = TYPE_COLORS[act.type] || TYPE_COLORS.other;
  return `
    <div class="ap-act" draggable="true"
         data-day="${dayIndex}" data-act="${actIndex}"
         ondragstart="window._plannerDragStart(event)"
         ondragend="window._plannerDragEnd(event)">
      <div class="ap-act-head">
        <span class="ap-act-grip">⋮⋮</span>
        <input class="ap-act-time" type="time" value="${escapeHtml(act.time || "")}"
               onchange="window._plannerUpdateAct(${dayIndex},${actIndex},'time',this.value)">
        <span class="ap-act-tag" style="background:${color}22;color:${color}">${escapeHtml(act.type || "other")}</span>
        <button class="ap-act-del" onclick="window._plannerDelAct(${dayIndex},${actIndex})">×</button>
      </div>
      <input class="ap-act-title" value="${escapeHtml(act.title)}"
             onchange="window._plannerUpdateAct(${dayIndex},${actIndex},'title',this.value)"
             placeholder="Activity name">
      <div class="ap-act-row">
        <input class="ap-act-dur" type="number" min="5" step="5" value="${act.duration_min || 60}"
               onchange="window._plannerUpdateAct(${dayIndex},${actIndex},'duration_min',parseInt(this.value)||60)"
               title="Duration (min)"> min
        ${act.location ? `
        <button class="ap-act-loc" onclick="window._plannerFocusLoc(${dayIndex},${actIndex})" title="Show on map">
          📍 ${escapeHtml(act.location.name || "Map")}
        </button>` : `
        <button class="ap-act-loc-empty" onclick="window._plannerAddLoc(${dayIndex},${actIndex})">+ Location</button>`}
      </div>
      <input class="ap-act-note" value="${escapeHtml(act.notes || "")}"
             onchange="window._plannerUpdateAct(${dayIndex},${actIndex},'notes',this.value)"
             placeholder="Notes...">
    </div>`;
}

function renderDay(day, index, total) {
  const acts = (day.activities || [])
    .map((a, i) => renderActivityCard(a, index, i))
    .join("");

  const totalMin = (day.activities || []).reduce(
    (sum, a) => sum + (a.duration_min || 0),
    0
  );
  const hours = Math.floor(totalMin / 60);
  const mins = totalMin % 60;
  const timeStr = hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;

  return `
    <div class="ap-day"
         ondragover="window._plannerDayDragOver(event,${index})"
         ondragleave="window._plannerDayDragLeave(event)"
         ondrop="window._plannerDayDrop(event,${index})">
      <div class="ap-day-head">
        <span class="ap-day-num">Day ${day.day || index + 1}</span>
        <input class="ap-day-date" type="date" value="${escapeHtml(day.date || "")}"
               onchange="window._plannerUpdateDay(${index},'date',this.value)"
               title="Date">
        <span class="ap-day-total">${timeStr} · ${(day.activities || []).length} activities</span>
        <button class="ap-day-add" onclick="window._plannerAddAct(${index})">+ Activity</button>
        ${total > 1 ? `<button class="ap-day-del" onclick="window._plannerDelDay(${index})">Delete day</button>` : ""}
      </div>
      <div class="ap-day-acts" id="ap-day-${index}">
        ${acts || `<div class="ap-day-empty">Drop activities here or click "+ Activity"</div>`}
      </div>
    </div>`;
}

function renderPlanner(containerId, tripId, data) {
  _plannerTripId = tripId;
  _plannerState = data?.days?.length
    ? data
    : {
        days: [
          { day: 1, date: "", activities: [] },
          { day: 2, date: "", activities: [] },
          { day: 3, date: "", activities: [] },
        ],
      };

  const el = document.getElementById(containerId);
  if (!el) return;

  // Mobile day navigation
  const isMobile = window.innerWidth < 768;

  el.innerHTML = `
    <style>
      .ap-wrap { font-size:13px; }
      .ap-toolbar { display:flex; gap:8px; padding:8px 0; flex-wrap:wrap; align-items:center; }
      .ap-toolbar button { font-size:12px; padding:7px 12px; border-radius:999px; border:1px solid rgba(255,255,255,.08); background:rgba(255,255,255,.04); color:rgba(255,255,255,.86); cursor:pointer; }
      .ap-toolbar button:hover { background:rgba(255,255,255,.08); }
      .ap-toolbar button.ap-primary { background:rgba(125,211,252,.14); border-color:rgba(125,211,252,.25); }
      .ap-mobile-nav { display:none; justify-content:center; align-items:center; gap:12px; margin:8px 0; }
      @media (max-width: 767px) {
        .ap-mobile-nav { display:flex; }
        .ap-day:not(.ap-day-active) { display:none; }
      }
      .ap-map-container { margin-top:8px; border-radius:12px; overflow:hidden; border:1px solid rgba(255,255,255,.08); }
      .ap-map { height:280px; width:100%; }
      .ap-day { border:1px solid rgba(255,255,255,.06); border-radius:12px; background:rgba(255,255,255,.015); margin-bottom:10px; padding:10px; transition:border-color .15s; }
      .ap-day.ap-dragover { border-color:rgba(125,211,252,.35); background:rgba(125,211,252,.04); }
      .ap-day-head { display:flex; gap:8px; align-items:center; flex-wrap:wrap; margin-bottom:8px; }
      .ap-day-num { font-weight:650; font-size:14px; color:rgba(125,211,252,.9); }
      .ap-day-date { font-size:12px; padding:3px 6px; border-radius:6px; border:1px solid rgba(255,255,255,.08); background:rgba(255,255,255,.03); color:rgba(255,255,255,.78); }
      .ap-day-total { font-size:11px; color:rgba(255,255,255,.45); margin-left:auto; }
      .ap-day-add, .ap-day-del { font-size:11px; padding:4px 10px; border-radius:999px; border:1px solid rgba(255,255,255,.08); background:rgba(255,255,255,.03); color:rgba(255,255,255,.7); cursor:pointer; }
      .ap-day-add:hover { border-color:rgba(125,211,252,.3); color:rgba(125,211,252,.9); }
      .ap-day-del:hover { border-color:rgba(252,165,165,.3); color:rgba(252,165,165,.9); }
      .ap-day-acts { min-height:40px; }
      .ap-day-empty { color:rgba(255,255,255,.25); font-style:italic; padding:12px; text-align:center; font-size:12px; border:1px dashed rgba(255,255,255,.06); border-radius:8px; }
      .ap-act { border:1px solid rgba(255,255,255,.05); border-radius:8px; background:rgba(255,255,255,.02); padding:8px; margin:6px 0; cursor:grab; transition: all .15s; }
      .ap-act:active { cursor:grabbing; }
      .ap-act.ap-dragging { opacity:.4; }
      .ap-act:hover { border-color:rgba(255,255,255,.12); }
      .ap-act-head { display:flex; gap:6px; align-items:center; margin-bottom:4px; }
      .ap-act-grip { color:rgba(255,255,255,.2); cursor:grab; font-size:14px; letter-spacing:-2px; user-select:none; }
      .ap-act-time { font-size:12px; padding:2px 4px; border-radius:4px; border:1px solid rgba(255,255,255,.08); background:rgba(255,255,255,.03); color:rgba(255,255,255,.7); width:70px; }
      .ap-act-tag { font-size:10px; padding:2px 6px; border-radius:999px; font-weight:600; text-transform:uppercase; }
      .ap-act-del { font-size:16px; padding:0 4px; border:none; background:none; color:rgba(255,255,255,.2); cursor:pointer; margin-left:auto; }
      .ap-act-del:hover { color:rgba(252,165,165,.8); }
      .ap-act-title { width:100%; font-size:13px; padding:4px 6px; border-radius:6px; border:1px solid rgba(255,255,255,.05); background:rgba(255,255,255,.02); color:rgba(255,255,255,.86); margin:4px 0; box-sizing:border-box; }
      .ap-act-title:focus { border-color:rgba(125,211,252,.25); outline:none; }
      .ap-act-row { display:flex; gap:8px; align-items:center; margin:4px 0; font-size:11px; color:rgba(255,255,255,.5); }
      .ap-act-dur { width:50px; padding:2px 4px; border-radius:4px; border:1px solid rgba(255,255,255,.08); background:rgba(255,255,255,.03); color:rgba(255,255,255,.7); text-align:center; }
      .ap-act-loc { font-size:11px; padding:2px 8px; border-radius:999px; border:none; background:rgba(125,211,252,.12); color:rgba(125,211,252,.9); cursor:pointer; }
      .ap-act-loc:hover { background:rgba(125,211,252,.20); }
      .ap-act-loc-empty { font-size:11px; padding:2px 8px; border-radius:999px; border:1px dashed rgba(255,255,255,.12); background:none; color:rgba(255,255,255,.35); cursor:pointer; }
      .ap-act-note { width:100%; font-size:11px; padding:3px 6px; border-radius:4px; border:1px solid rgba(255,255,255,.04); background:rgba(255,255,255,.01); color:rgba(255,255,255,.5); margin-top:4px; box-sizing:border-box; }
      .ap-act-note:focus { border-color:rgba(255,255,255,.1); outline:none; color:rgba(255,255,255,.7); }
      .ap-share-toast { position:fixed; bottom:20px; left:50%; transform:translateX(-50%); background:rgba(125,211,252,.15); border:1px solid rgba(125,211,252,.3); color:rgba(255,255,255,.9); padding:8px 20px; border-radius:999px; font-size:13px; z-index:9999; animation:apFadeIn .3s; }
      @keyframes apFadeIn { from{opacity:0;transform:translateX(-50%) translateY(10px)} to{opacity:1;transform:translateX(-50%) translateY(0)} }
    </style>
    <div class="ap-wrap">
      <div class="ap-toolbar">
        <button onclick="window._plannerAddDay()">+ Add Day</button>
        <button class="ap-primary" onclick="window._plannerExportPDF()">📄 Export PDF</button>
        <button class="ap-primary" onclick="window._plannerShare()">🔗 Share</button>
        <button onclick="window._plannerToggleMap()">🗺️ Map</button>
      </div>
      <div class="ap-mobile-nav" id="ap-mobile-nav">
        <button onclick="window._plannerPrevDay()">◀ Prev</button>
        <span id="ap-mobile-label">Day 1 / ${_plannerState.days.length}</span>
        <button onclick="window._plannerNextDay()">▶ Next</button>
      </div>
      <div id="ap-days-container">
        ${_plannerState.days.map((d, i) => renderDay(d, i, _plannerState.days.length)).join("")}
      </div>
      <div class="ap-map-container" id="ap-map-wrap" style="display:none">
        <div class="ap-map" id="ap-map"></div>
      </div>
    </div>`;

  // Mobile: show first day
  if (isMobile) {
    _plannerShowDay(0);
  }
  updateMobileNav();
}

function _plannerShowDay(index) {
  _activeDay = index;
  document.querySelectorAll(".ap-day").forEach((d, i) => {
    d.classList.toggle("ap-day-active", i === index);
  });
  updateMobileNav();
}

function updateMobileNav() {
  const label = document.getElementById("ap-mobile-label");
  if (label) {
    label.textContent = `Day ${_plannerState.days[_activeDay]?.day || _activeDay + 1} / ${_plannerState.days.length}`;
  }
}

function refreshPlanner() {
  const container = document.getElementById("ap-days-container");
  if (!container) return;
  container.innerHTML = _plannerState.days
    .map((d, i) => renderDay(d, i, _plannerState.days.length))
    .join("");
  if (window.innerWidth < 768) _plannerShowDay(_activeDay);
  updateMobileNav();
}

// ── Global handlers (callable from onclick/ondrag) ───────────

let _dragDay = -1;
let _dragAct = -1;

window._plannerDragStart = function (e) {
  _dragDay = parseInt(e.target.dataset.day);
  _dragAct = parseInt(e.target.dataset.act);
  e.target.classList.add("ap-dragging");
  e.dataTransfer.effectAllowed = "move";
  e.dataTransfer.setData("text/plain", `${_dragDay}:${_dragAct}`);
};

window._plannerDragEnd = function (e) {
  e.target.classList.remove("ap-dragging");
  document.querySelectorAll(".ap-dragover").forEach((d) => d.classList.remove("ap-dragover"));
};

window._plannerDayDragOver = function (e, dayIndex) {
  e.preventDefault();
  e.currentTarget.classList.add("ap-dragover");
};

window._plannerDayDragLeave = function (e) {
  e.currentTarget.classList.remove("ap-dragover");
};

window._plannerDayDrop = function (e, targetDay) {
  e.preventDefault();
  e.currentTarget.classList.remove("ap-dragover");
  if (_dragDay < 0 || _dragAct < 0) return;

  const srcDay = _plannerState.days[_dragDay];
  if (!srcDay) return;
  const act = (srcDay.activities || [])[_dragAct];
  if (!act) return;

  // Remove from source
  srcDay.activities.splice(_dragAct, 1);

  // Add to target
  const target = _plannerState.days[targetDay];
  if (!target.activities) target.activities = [];
  target.activities.push(act);

  _dragDay = _dragAct = -1;
  refreshPlanner();
  debouncedSave();
};

window._plannerAddAct = function (dayIndex) {
  const day = _plannerState.days[dayIndex];
  if (!day) return;
  if (!day.activities) day.activities = [];
  day.activities.push({
    id: uid(),
    time: "09:00",
    title: "",
    type: "sightseeing",
    duration_min: 60,
    location: null,
    notes: "",
  });
  refreshPlanner();
  debouncedSave();
};

window._plannerDelAct = function (dayIndex, actIndex) {
  _plannerState.days[dayIndex].activities.splice(actIndex, 1);
  refreshPlanner();
  debouncedSave();
};

window._plannerUpdateAct = function (dayIndex, actIndex, key, value) {
  const act = _plannerState.days[dayIndex]?.activities?.[actIndex];
  if (!act) return;
  act[key] = value;
  debouncedSave();
};

window._plannerUpdateDay = function (dayIndex, key, value) {
  _plannerState.days[dayIndex][key] = value;
  debouncedSave();
};

window._plannerAddDay = function () {
  const nextNum = _plannerState.days.length + 1;
  _plannerState.days.push({ day: nextNum, date: "", activities: [] });
  refreshPlanner();
  debouncedSave();
};

window._plannerDelDay = function (index) {
  _plannerState.days.splice(index, 1);
  _plannerState.days.forEach((d, i) => (d.day = i + 1));
  if (_activeDay >= _plannerState.days.length) _activeDay = _plannerState.days.length - 1;
  refreshPlanner();
  debouncedSave();
};

window._plannerPrevDay = function () {
  if (_activeDay > 0) _plannerShowDay(_activeDay - 1);
};

window._plannerNextDay = function () {
  if (_activeDay < _plannerState.days.length - 1) _plannerShowDay(_activeDay + 1);
};

window._plannerFocusLoc = function (dayIndex, actIndex) {
  const act = _plannerState.days[dayIndex]?.activities?.[actIndex];
  if (!act?.location) return;
  if (!_mapVisible) window._plannerToggleMap();
  initMap("ap-map").then(() => centerMapOn(act.location.lat, act.location.lng, act.location.name));
};

window._plannerAddLoc = function (dayIndex, actIndex) {
  const name = prompt("Location name (e.g. Forbidden City):");
  if (!name) return;
  const lat = parseFloat(prompt("Latitude (e.g. 39.916):"));
  const lng = parseFloat(prompt("Longitude (e.g. 116.397):"));
  if (isNaN(lat) || isNaN(lng)) return;
  const act = _plannerState.days[dayIndex]?.activities?.[actIndex];
  if (!act) return;
  act.location = { lat, lng, name };
  refreshPlanner();
  debouncedSave();
};

window._plannerToggleMap = async function () {
  _mapVisible = !_mapVisible;
  const wrap = document.getElementById("ap-map-wrap");
  if (!wrap) return;
  wrap.style.display = _mapVisible ? "block" : "none";
  if (_mapVisible) await initMap("ap-map");
};

window._plannerExportPDF = function () {
  const w = window.open("", "_print");
  if (!w) return;
  const daysHtml = _plannerState.days
    .map((d) => {
      const acts = (d.activities || [])
        .map(
          (a) =>
            `<tr><td style="padding:6px 8px;border-bottom:1px solid #eee">${escapeHtml(a.time)}</td>` +
            `<td style="padding:6px 8px;border-bottom:1px solid #eee"><b>${escapeHtml(a.title)}</b></td>` +
            `<td style="padding:6px 8px;border-bottom:1px solid #eee">${escapeHtml(a.type)}</td>` +
            `<td style="padding:6px 8px;border-bottom:1px solid #eee">${a.duration_min || 0}min</td>` +
            `<td style="padding:6px 8px;border-bottom:1px solid #eee">${escapeHtml(a.notes || "-")}</td></tr>`
        )
        .join("");
      return `<h2>Day ${d.day}${d.date ? " — " + d.date : ""}</h2>
        <table style="width:100%;border-collapse:collapse;margin:8px 0 20px">
          <tr style="background:#f5f5f5"><th style="padding:6px 8px;text-align:left">Time</th><th style="padding:6px 8px;text-align:left">Activity</th><th style="padding:6px 8px;text-align:left">Type</th><th style="padding:6px 8px;text-align:left">Duration</th><th style="padding:6px 8px;text-align:left">Notes</th></tr>
          ${acts || `<tr><td colspan="5" style="padding:12px;text-align:center;color:#999">No activities</td></tr>`}
        </table>`;
    })
    .join("");

  w.document.write(`<!doctype html><html><head><title>Itinerary</title>
    <meta charset="utf-8"><style>body{font-family:system-ui,sans-serif;max-width:800px;margin:0 auto;padding:20px;color:#333}h1{font-size:24px}h2{font-size:18px;color:#666;margin-top:24px}@media print{body{padding:0}}</style></head>
    <body><h1>China Travel Itinerary</h1>${daysHtml}<p style="margin-top:30px;color:#999;font-size:12px">Generated by VisePanda</p></body></html>`);
  w.document.close();
  setTimeout(() => w.print(), 300);
};

window._plannerShare = async function () {
  let token = "";
  try {
    // Load itinerary from backend to get share token
    const r = await (
      await import("./app.js")
    ).apiFetch(`/trips/${encodeURIComponent(_plannerTripId)}/itinerary`);
    if (r.ok) {
      const data = await r.json();
      token = data.share_token || "";
    }
  } catch (_) {}

  const url = token
    ? `${window.location.origin}/shared/${token}`
    : `${window.location.origin}/chat?trip=${_plannerTripId}`;

  try {
    await navigator.clipboard.writeText(url);
  } catch (_) {
    // Fallback
    const input = document.createElement("input");
    input.value = url;
    document.body.appendChild(input);
    input.select();
    document.execCommand("copy");
    document.body.removeChild(input);
  }

  const toast = document.createElement("div");
  toast.className = "ap-share-toast";
  toast.textContent = "Link copied! " + url.slice(0, 40) + "…";
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
};

// ── Exports ──────────────────────────────────────────────────

export { renderPlanner };

export function getPlannerData() {
  return _plannerState;
}

export function setPlannerData(data, tripId) {
  _plannerTripId = tripId;
  _plannerState = data || { days: [] };
  refreshPlanner();
}
