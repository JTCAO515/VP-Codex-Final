// Trips view — upcoming / past / drafts toggle + grid of trip cards
// + "Start a new trip" card. Backed by /api/trips.
//
// Creating a trip opens a sheet (name + start date + day count stepper)
// instead of a prompt(). Opening an existing trip navigates to Plan bound
// to that trip's id.

import { api } from './api.js';
import { openSheet, closeSheet, sheetHeader } from './components/sheet.js';

let state = {
  root: null,
  trips: [],
  bucket: 'upcoming',
  onOpenTrip: null,
};

export function mount({ container, onOpenTrip }) {
  state.root = container;
  state.onOpenTrip = onOpenTrip;
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
  if (state.bucket === 'drafts') return state.trips.filter((t) => t.status === 'draft');
  if (state.bucket === 'past')   return state.trips.filter((t) => t.status === 'past');
  return state.trips.filter((t) => t.status !== 'past' && t.status !== 'draft');
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
  state.root.querySelector('.trips-new').addEventListener('click', openCreateSheet);

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
        <span class="badge">${esc(`${(t.cities || []).length ? (t.cities || []).length + ' cities' : 'New trip'} · ${t.day_count || 0}d`)}</span>
      </div>
      <div class="body">
        <div class="name">${esc(t.name)}</div>
        <div class="route">${esc(t.start_date || 'No dates yet')}${cities ? ' · ' + esc(cities) : ''}</div>
        <div class="status">${esc(statusLabel(t))} · ${t.progress || 0}%</div>
        <div class="progress-track"><div class="progress-bar ${t.progress >= 100 ? 'done' : ''}" style="width:${Math.max(0, Math.min(100, t.progress || 0))}%"></div></div>
        <div class="footer">
          <span></span>
          <span class="open-link">Open →</span>
        </div>
      </div>
    `;
    card.addEventListener('click', () => {
      if (state.onOpenTrip) state.onOpenTrip(t.id);
    });
    grid.appendChild(card);
  }

  if (state.bucket !== 'past') {
    const add = document.createElement('button');
    add.type = 'button';
    add.className = 'trip-new-card';
    add.innerHTML = `
      <span class="plus">+</span>
      <span class="title">Start a new trip</span>
      <span class="sub"><span class="spark"></span>or generate with Panda</span>
    `;
    add.addEventListener('click', openCreateSheet);
    grid.appendChild(add);
  }
}

function statusLabel(t) {
  if ((t.progress || 0) >= 100) return 'Ready';
  if (t.status === 'draft') return 'Draft';
  if (t.status === 'past') return 'Past';
  return 'Planning';
}

function openCreateSheet() {
  if (!window.vp.user) {
    window.dispatchEvent(new CustomEvent('vp:auth-required'));
    return;
  }
  let dayCount = 3;
  const content = document.createElement('div');
  content.className = 'sheet-content';
  content.appendChild(sheetHeader('Start a new trip'));
  content.innerHTML += `
    <form id="create-trip-form">
      <label style="display:flex;flex-direction:column;gap:4px;font-size:var(--text-base);color:var(--ink-5);margin-bottom:14px">
        Trip name
        <input type="text" name="name" placeholder="My China trip" required
               style="background:var(--sidebar-bg);border:1px solid var(--line-1);border-radius:8px;padding:10px 12px;font:inherit;color:var(--ink-1)">
      </label>
      <label style="display:flex;flex-direction:column;gap:4px;font-size:var(--text-base);color:var(--ink-5);margin-bottom:6px">
        Start date (optional)
        <input type="date" name="start_date"
               style="background:var(--sidebar-bg);border:1px solid var(--line-1);border-radius:8px;padding:10px 12px;font:inherit;color:var(--ink-1)">
      </label>
      <div class="label" style="font-weight:600;font-size:10.5px;color:var(--ink-soft);letter-spacing:.05em;margin-top:14px">DURATION</div>
      <div class="day-count-stepper">
        <button type="button" data-d="-1">−</button>
        <span class="count" id="day-count-display">3</span>
        <button type="button" data-d="1">+</button>
      </div>
      <div class="sheet-footer-actions">
        <button class="btn-outline" type="button" data-act="cancel">Cancel</button>
        <button class="btn-primary" type="submit">Create trip</button>
      </div>
    </form>
  `;
  openSheet(content);

  const display = content.querySelector('#day-count-display');
  content.querySelectorAll('.day-count-stepper button').forEach((b) => {
    b.addEventListener('click', () => {
      dayCount = Math.max(1, Math.min(30, dayCount + (+b.dataset.d)));
      display.textContent = dayCount;
    });
  });
  content.querySelector('[data-act="cancel"]').addEventListener('click', () => closeSheet());
  content.querySelector('#create-trip-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const name = String(fd.get('name') || '').trim() || 'Untitled trip';
    const start_date = String(fd.get('start_date') || '');
    try {
      const data = await api.post('/api/trips', {
        name, start_date, day_count: dayCount, cities: [], status: 'planning', progress: 0,
      });
      closeSheet();
      state.trips.unshift(data.trip);
      render();
      if (state.onOpenTrip) state.onOpenTrip(data.trip.id);
    } catch (_) {
      alert('Could not create trip.');
    }
  });
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
