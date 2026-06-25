// Cities — browse curated cities. Search + filters + sort. Hero featured card + grid.

import { api } from './api.js';

let state = {
  root: null,
  cities: [],
  query: '',
  featured: null,
};

export function mount({ container }) {
  state.root = container;
  container.classList.add('view-cities');
  load();
}

async function load() {
  try {
    const data = await api.get('/api/cities');
    state.cities = data.cities || [];
    state.featured = state.cities.find((c) => c.id === 'chengdu') || state.cities[0];
  } catch (_) {
    state.cities = [];
  }
  render();
}

function render() {
  if (!state.root) return;
  const filtered = state.query
    ? state.cities.filter((c) =>
        c.name.toLowerCase().includes(state.query.toLowerCase()) ||
        (c.cn || '').includes(state.query) ||
        (c.tagline || '').toLowerCase().includes(state.query.toLowerCase())
      )
    : state.cities;

  state.root.innerHTML = `
    <section class="cities-toolbar">
      <div class="cities-search">
        <span class="glyph"></span>
        <input type="search" placeholder="Search a city…" value="${esc(state.query)}">
      </div>
      <button class="cities-filter" type="button">Region ▾</button>
      <button class="cities-filter" type="button">Interest ▾</button>
      <button class="cities-filter" type="button">Season ▾</button>
      <button class="cities-sort" type="button">Sort: Popular ▾</button>
    </section>
    <section class="cities-body">
      ${state.featured ? `
        <div class="cities-featured" data-id="${esc(state.featured.id)}">
          <span class="img-tag">image · city hero</span>
          <div class="meta">
            <span class="badge">FEATURED</span>
            <div class="name">${esc(state.featured.name)}</div>
            <div class="tags">
              ${(state.featured.tagline || '').split(/,\s*|\s+·\s+/).slice(0, 3).map((t) => `<span class="tag">${esc(t)}</span>`).join('')}
            </div>
          </div>
        </div>
      ` : ''}
      <div class="cities-section-head">
        <div class="title">Popular cities</div>
        <div class="count">${filtered.length} cities</div>
      </div>
      <div class="cities-grid"></div>
    </section>
  `;

  const search = state.root.querySelector('.cities-search input');
  search.addEventListener('input', (e) => {
    state.query = e.target.value;
    render();
  });

  const grid = state.root.querySelector('.cities-grid');
  for (const c of filtered) {
    const card = document.createElement('button');
    card.type = 'button';
    card.className = 'city-card';
    const tags = (c.tagline || '').split(/,\s*|\s+·\s+/).slice(0, 2);
    card.innerHTML = `
      <div class="img">
        <span class="name">${esc(c.name)}</span>
      </div>
      <div class="body">
        <div class="row">
          <span class="heading">${esc(c.cn || c.name)}</span>
          <span class="days">${esc(c.best_months || '')}</span>
        </div>
        <div class="desc">${esc(c.tagline || '')}</div>
        <div class="tags">${tags.map((t) => `<span class="tag">${esc(t)}</span>`).join('')}</div>
      </div>
    `;
    card.addEventListener('click', () => {
      // For now, just toast the city. Future: open detail view or jump to Plan with city added.
      alert(`${c.name} (${c.cn}) — full detail view coming soon.\n\nFor now you can add it to a Plan from the Plan tab.`);
    });
    grid.appendChild(card);
  }
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
