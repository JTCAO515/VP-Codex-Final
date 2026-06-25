// Trips view — upcoming / past / drafts toggle + grid of trip cards
// + "Start a new trip" card. Backed by /api/trips.

import { api } from './api.js';

let state = {
  root: null,
  trips: [],
  bucket: 'upcoming',
  onPlanNew: null,
};

export function mount({ container, onPlanNew }) {
  state.root = container;
  state.onPlanNew = onPlanNew;
  container.classList.add('view-trips');
  load();
}

async function load() {
  try {
    const data = await api.get('/api/trips');
    state.trips = data.trips || [];
  } catch (_) { state.trips = []; }
  render();
}

function filtered() {
  // Loose mapping: drafts = status:draft; past = status:past; else upcoming.
  if (state.bucket === 'drafts') return state.trips.filter((t) => t.status === 'draft');
  if (state.bucket === 'past')   return state.trips.filter((t) => t.status === 'past');
  return state.trips.filter((t) => t.status !== 'past' && t.status !== 'draft' || t.status === 'planning' || t.status === 'ready');
}

function render() {
  if (!state.root) return;
  state.root.innerHTML = `
    <section class="trips-toolbar">
      <div class="title">My trips</div>
      <div class="trips-segment" role="tablist">
        <button type="button" data-b="upcoming" class="${state.bucket==='upcoming'?'active':''}">Upcoming</button>
        <button type="button" data-b="past" class="${state.bucket==='past'?'active':''}">Past</button>
        <button type="button" data-b="drafts" class="${state.bucket==='drafts'?'active':''}">Drafts</button>
      </div>
      <button class="trips-new" type="button">＋ New trip</button>
    </section>
    <section class="trips-body">
      <div class="trips-grid"></div>
    </section>
  `;
  state.root.querySelectorAll('.trips-segment button').forEach((b) => {
    b.addEventListener('click', () => { state.bucket = b.dataset.b; render(); });
  });
  state.root.querySelector('.trips-new').addEventListener('click', createTrip);

  const grid = state.root.querySelector('.trips-grid');
  const items = filtered();
  if (!items.length && !window.vp.user) {
    grid.innerHTML = `<div class="trips-empty">Sign in to save and revisit trips across devices.</div>`;
  }
  for (const t of items) {
    const cities = (t.cities || []).map((c) => c.name || c).join(' → ');
    const card = document.createElement('button');
    card.type = 'button';
    card.className = 'trip-card';
    card.innerHTML = `
      <div class="img">
        <span class="badge">${esc(`${(t.cities || []).length ? (t.cities || []).length + ' cities' : 'New trip'}`)}${t.dates ? ' · ' + esc(t.dates) : ''}</span>
      </div>
      <div class="body">
        <div class="name">${esc(t.name)}</div>
        <div class="route">${esc(t.dates || '')}${cities ? ' · ' + esc(cities) : ''}</div>
        <div class="status">${esc(statusLabel(t))} · ${t.progress || 0}%</div>
        <div class="progress-track"><div class="progress-bar ${t.progress >= 100 ? 'done' : ''}" style="width:${Math.max(0, Math.min(100, t.progress || 0))}%"></div></div>
        <div class="footer">
          <span></span>
          <span class="open-link">Open →</span>
        </div>
      </div>
    `;
    card.addEventListener('click', () => openTrip(t));
    grid.appendChild(card);
  }

  // Always show "Start a new trip" tile at the end of upcoming/drafts
  if (state.bucket !== 'past') {
    const add = document.createElement('button');
    add.type = 'button';
    add.className = 'trip-new-card';
    add.innerHTML = `
      <span class="plus">+</span>
      <span class="title">Start a new trip</span>
      <span class="sub"><span class="spark"></span>or generate with Panda</span>
    `;
    add.addEventListener('click', createTrip);
    grid.appendChild(add);
  }
}

function statusLabel(t) {
  if ((t.progress || 0) >= 100) return 'Ready';
  if (t.status === 'draft') return 'Draft';
  if (t.status === 'past') return 'Past';
  return 'Planning';
}

async function createTrip() {
  if (!window.vp.user) {
    window.dispatchEvent(new CustomEvent('vp:auth-required'));
    return;
  }
  const name = prompt('Trip name?', 'My China trip');
  if (!name) return;
  try {
    const data = await api.post('/api/trips', {
      name,
      dates: '',
      cities: [],
      status: 'planning',
      progress: 0,
    });
    state.trips.unshift(data.trip);
    render();
    if (state.onPlanNew) state.onPlanNew(data.trip);
  } catch (_) {
    alert('Could not create trip.');
  }
}

function openTrip(t) {
  // For now jump to Plan with the trip context. (Plan view loads its own
  // itinerary from /api/itinerary; full per-trip itineraries are a later
  // enhancement.)
  if (state.onPlanNew) state.onPlanNew(t);
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
