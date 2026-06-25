// Plan view — itinerary builder with destinations / dates / travelers / pace,
// day tabs, timeline of stops, and a placeholder map with AI suggestion strip.
//
// Backed by /api/itinerary (legacy contract from v7). Per the new design we
// also surface destinations + dates + pace; those persist in localStorage
// since the v7 schema is just { days: [...] }.

import { api } from './api.js';
import * as sidebar from './sidebar.js';

const PACE = ['Relaxed', 'Balanced', 'Packed'];

let state = {
  root: null,
  destinations: [],   // [{ id, name, cn }]
  date_from: '',
  date_to: '',
  travelers: 2,
  pace: 'Relaxed',
  days: [],           // [{ day_index, label, date, city, stops:[{time,name,desc,tags}] }]
  active_day: 0,
  citiesCache: [],
};

const META_KEY = 'vp.plan.meta';

export function mount({ container }) {
  state.root = container;
  container.classList.add('view-plan');
  loadMeta();
  loadCities();
  loadItinerary().then(() => {
    publishTripContext();
    render();
  });
}

function loadMeta() {
  try {
    const raw = JSON.parse(localStorage.getItem(META_KEY) || '{}');
    state.destinations = raw.destinations || [];
    state.date_from = raw.date_from || '';
    state.date_to = raw.date_to || '';
    state.travelers = raw.travelers || 2;
    state.pace = raw.pace || 'Relaxed';
  } catch (_) {}
}
function saveMeta() {
  try {
    localStorage.setItem(META_KEY, JSON.stringify({
      destinations: state.destinations,
      date_from: state.date_from,
      date_to: state.date_to,
      travelers: state.travelers,
      pace: state.pace,
    }));
  } catch (_) {}
}

async function loadCities() {
  try {
    const data = await api.get('/api/cities');
    state.citiesCache = data.cities || [];
    if (state.destinations.length === 0 && state.citiesCache.length) {
      // Seed with the first 3 demo cities so the toolbar isn't empty.
      state.destinations = state.citiesCache.slice(0, 3).map((c) => ({
        id: c.id, name: c.name, cn: c.cn,
      }));
      saveMeta();
      render();
    }
  } catch (_) {}
}

async function loadItinerary() {
  try {
    const data = await api.get('/api/itinerary');
    state.days = (data.days && data.days.length) ? data.days : seedDays();
  } catch (_) {
    state.days = seedDays();
  }
}

function seedDays() {
  return [1, 2, 3].map((i) => ({
    day_index: i,
    label: 'Day ' + i,
    date: '',
    city: state.destinations[i - 1]?.name || state.destinations[0]?.name || '',
    stops: i === 1 ? [
      { time: '08:30', name: 'Forbidden City', desc: 'Walk through the imperial palace complex.', tags: ['Landmark', '2h'] },
      { time: '11:30', name: 'Jingshan Park lunch', desc: 'Climb for a view, then a hutong cafe.', tags: ['Food', '$$'] },
    ] : [],
  }));
}

function publishTripContext() {
  const name = state.destinations.map((d) => d.name).join(' → ') || 'Untitled trip';
  const dates = (state.date_from && state.date_to)
    ? `${state.date_from} – ${state.date_to}`
    : (state.date_from || 'No dates yet');
  sidebar.setTripContext({
    name,
    dates,
    city_count: state.destinations.length || undefined,
  });
}

function render() {
  if (!state.root) return;
  state.root.innerHTML = '';
  state.root.appendChild(renderToolbar());
  state.root.appendChild(renderBody());
}

function renderToolbar() {
  const bar = document.createElement('section');
  bar.className = 'plan-toolbar';

  // Destinations
  const dest = document.createElement('div');
  dest.className = 'plan-field destinations';
  dest.innerHTML = `<div class="label">DESTINATIONS</div>`;
  const destBox = document.createElement('div');
  destBox.className = 'box';
  for (const d of state.destinations) {
    const chip = document.createElement('span');
    chip.className = 'dest-chip';
    chip.innerHTML = `${esc(d.name)} <span class="x">×</span>`;
    chip.querySelector('.x').addEventListener('click', () => {
      state.destinations = state.destinations.filter((x) => x.id !== d.id);
      saveMeta(); publishTripContext(); render();
    });
    destBox.appendChild(chip);
  }
  const add = document.createElement('button');
  add.type = 'button';
  add.className = 'add-dest';
  add.textContent = '+ add…';
  add.addEventListener('click', () => promptAddCity());
  destBox.appendChild(add);
  dest.appendChild(destBox);
  bar.appendChild(dest);

  // Dates (simple text fields)
  const dates = document.createElement('div');
  dates.className = 'plan-field dates';
  dates.innerHTML = `<div class="label">DATES</div>`;
  const datesBox = document.createElement('div');
  datesBox.className = 'box';
  datesBox.style.padding = '4px 8px';
  datesBox.innerHTML = `<input type="date" id="pf-from" style="border:none;background:transparent;font:inherit;width:50%"> – <input type="date" id="pf-to" style="border:none;background:transparent;font:inherit;width:50%">`;
  dates.appendChild(datesBox);
  bar.appendChild(dates);

  // Travelers
  const trav = document.createElement('div');
  trav.className = 'plan-field travelers';
  trav.innerHTML = `<div class="label">TRAVELERS</div>`;
  const travBox = document.createElement('div');
  travBox.className = 'box';
  travBox.style.padding = '4px 8px';
  travBox.innerHTML = `<input type="number" id="pf-trav" min="1" max="20" value="${state.travelers}" style="border:none;background:transparent;font:inherit;width:100%">`;
  trav.appendChild(travBox);
  bar.appendChild(trav);

  // Pace
  const pace = document.createElement('div');
  pace.className = 'plan-field pace';
  pace.innerHTML = `<div class="label">PACE</div>`;
  const paceBox = document.createElement('div');
  paceBox.className = 'box';
  paceBox.style.padding = '4px 8px';
  const sel = document.createElement('select');
  sel.id = 'pf-pace';
  sel.style.cssText = 'border:none;background:transparent;font:inherit;width:100%;';
  for (const p of PACE) {
    const opt = document.createElement('option');
    opt.value = p; opt.textContent = p;
    if (p === state.pace) opt.selected = true;
    sel.appendChild(opt);
  }
  paceBox.appendChild(sel);
  pace.appendChild(paceBox);
  bar.appendChild(pace);

  // Generate button
  const gen = document.createElement('button');
  gen.type = 'button';
  gen.className = 'plan-generate';
  gen.innerHTML = `<span class="spark"></span>Generate`;
  gen.addEventListener('click', generate);
  bar.appendChild(gen);

  // Wire field changes
  setTimeout(() => {
    const from = bar.querySelector('#pf-from');
    const to = bar.querySelector('#pf-to');
    const t = bar.querySelector('#pf-trav');
    const p = bar.querySelector('#pf-pace');
    if (from) { from.value = state.date_from; from.addEventListener('change', (e) => { state.date_from = e.target.value; saveMeta(); publishTripContext(); }); }
    if (to) { to.value = state.date_to; to.addEventListener('change', (e) => { state.date_to = e.target.value; saveMeta(); publishTripContext(); }); }
    if (t) t.addEventListener('change', (e) => { state.travelers = +e.target.value || 1; saveMeta(); });
    if (p) p.addEventListener('change', (e) => { state.pace = e.target.value; saveMeta(); });
  }, 0);

  return bar;
}

function promptAddCity() {
  const taken = new Set(state.destinations.map((d) => d.id));
  const choices = state.citiesCache.filter((c) => !taken.has(c.id));
  if (!choices.length) { alert('No more cities to add.'); return; }
  const lines = choices.map((c, i) => `${i + 1}. ${c.name} (${c.cn})`).join('\n');
  const pick = prompt('Pick a city by number:\n' + lines);
  const idx = parseInt(pick, 10) - 1;
  if (idx >= 0 && idx < choices.length) {
    const c = choices[idx];
    state.destinations.push({ id: c.id, name: c.name, cn: c.cn });
    saveMeta();
    publishTripContext();
    render();
  }
}

function renderBody() {
  const body = document.createElement('section');
  body.className = 'plan-body';

  // Itinerary column
  const itin = document.createElement('div');
  itin.className = 'plan-itinerary';

  const tabs = document.createElement('div');
  tabs.className = 'plan-day-tabs';
  for (let i = 0; i < state.days.length; i++) {
    const t = document.createElement('button');
    t.type = 'button';
    t.className = 'plan-day-tab' + (i === state.active_day ? ' active' : '');
    t.textContent = state.days[i].label || ('Day ' + (i + 1));
    t.addEventListener('click', () => { state.active_day = i; render(); });
    tabs.appendChild(t);
  }
  const addDay = document.createElement('button');
  addDay.type = 'button';
  addDay.className = 'plan-day-add';
  addDay.textContent = '+';
  addDay.addEventListener('click', () => {
    state.days.push({
      day_index: state.days.length + 1,
      label: 'Day ' + (state.days.length + 1),
      stops: [],
    });
    state.active_day = state.days.length - 1;
    saveItinerary(); render();
  });
  tabs.appendChild(addDay);
  const opt = document.createElement('button');
  opt.type = 'button';
  opt.className = 'plan-optimize';
  opt.textContent = '⟲ Optimize';
  opt.addEventListener('click', generate);
  tabs.appendChild(opt);
  itin.appendChild(tabs);

  const content = document.createElement('div');
  content.className = 'plan-day-content';
  const day = state.days[state.active_day] || state.days[0];
  if (day) {
    const title = document.createElement('div');
    title.className = 'plan-day-title';
    title.textContent = day.label + (day.city ? ' · ' + day.city : '');
    content.appendChild(title);
    const sub = document.createElement('div');
    sub.className = 'plan-day-sub';
    sub.textContent = [day.date, `${(day.stops || []).length} stops`].filter(Boolean).join(' · ');
    content.appendChild(sub);

    for (const stop of (day.stops || [])) {
      content.appendChild(renderStop(stop));
    }
    const add = document.createElement('button');
    add.type = 'button';
    add.className = 'plan-stop-add';
    add.innerHTML = `<span class="plus">+</span>Add a stop`;
    add.addEventListener('click', () => {
      const name = prompt('What stop? (name)');
      if (!name) return;
      const time = prompt('What time? (e.g. 14:00)', '14:00') || '14:00';
      day.stops = day.stops || [];
      day.stops.push({ time, name, desc: '', tags: [] });
      saveItinerary(); render();
    });
    content.appendChild(add);
  }
  itin.appendChild(content);
  body.appendChild(itin);

  // Map placeholder
  const map = document.createElement('div');
  map.className = 'plan-map';
  map.innerHTML = `
    <span class="map-tag">map · ${esc(day?.label || 'day')}</span>
  `;
  const pins = (day?.stops || []).slice(0, 4);
  pins.forEach((s, i) => {
    const pin = document.createElement('div');
    pin.className = 'pin' + (i >= 2 ? ' muted' : '');
    pin.style.top = (90 + i * 70) + 'px';
    pin.style.left = (100 + (i % 2) * 120) + 'px';
    pin.textContent = i + 1;
    map.appendChild(pin);
  });
  const suggest = document.createElement('div');
  suggest.className = 'plan-suggest';
  suggest.innerHTML = `
    <div class="panda-mini"></div>
    <div style="flex:1">
      <h4>Panda suggests</h4>
      <p>Save 30 minutes by visiting Jingshan Park before the Forbidden City — same metro stop, less crowded mornings.</p>
      <div class="acts">
        <button class="add" type="button">Add</button>
        <button class="why" type="button">Why?</button>
      </div>
    </div>
  `;
  map.appendChild(suggest);
  body.appendChild(map);

  return body;
}

function renderStop(stop) {
  const wrap = document.createElement('div');
  wrap.className = 'plan-stop';
  wrap.innerHTML = `
    <div class="time-col">
      <span class="time">${esc(stop.time || '—')}</span>
      <div class="line"></div>
    </div>
    <div class="stop-card">
      <div class="stop-thumb"></div>
      <div class="stop-meta">
        <div class="stop-name">${esc(stop.name || 'Untitled stop')}</div>
        <div class="stop-desc">${esc(stop.desc || '')}</div>
        ${stop.tags && stop.tags.length ? `<div class="tags">${stop.tags.map((t) => `<span class="tag">${esc(t)}</span>`).join('')}</div>` : ''}
      </div>
    </div>
  `;
  return wrap;
}

async function generate() {
  // Calls /api/chat with a prompt that asks DeepSeek to draft an itinerary.
  // We don't actually parse the response into days right now (would need
  // structured output) — we just toast that the request was sent.
  alert('Generate is a stub for now. Use Ask to brainstorm; manually add stops here. (We\'ll wire DeepSeek → structured itinerary in a later step.)');
}

async function saveItinerary() {
  try { await api.put('/api/itinerary', { days: state.days }); } catch (_) {}
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
