// City picker — searchable multi-select sheet, used by Plan's "+ add".

import { api } from '../api.js';
import { openSheet, closeSheet, sheetHeader } from './sheet.js';

let cache = null;

async function loadCities() {
  if (cache) return cache;
  try {
    const data = await api.get('/api/cities');
    cache = data.cities || [];
  } catch (_) {
    cache = [];
  }
  return cache;
}

/**
 * Opens a picker sheet. `selectedIds` seeds the initial selection.
 * Resolves with the final selected city list when the user confirms,
 * or null if they cancel/close without confirming.
 */
export function pickCities(selectedIds = []) {
  return new Promise((resolve) => {
    let resolved = false;
    const selected = new Map();

    const content = document.createElement('div');
    content.className = 'sheet-content';
    content.appendChild(sheetHeader('Add destinations'));

    const search = document.createElement('div');
    search.className = 'sheet-search';
    search.innerHTML = `<input type="search" placeholder="Search cities…">`;
    content.appendChild(search);

    const list = document.createElement('div');
    list.className = 'picker-list';
    content.appendChild(list);

    const actions = document.createElement('div');
    actions.className = 'sheet-footer-actions';
    actions.innerHTML = `
      <button class="btn-outline" type="button" data-act="cancel">Cancel</button>
      <button class="btn-primary" type="button" data-act="confirm">Add selected</button>
    `;
    content.appendChild(actions);

    actions.querySelector('[data-act="cancel"]').addEventListener('click', () => closeSheet());
    actions.querySelector('[data-act="confirm"]').addEventListener('click', () => {
      resolved = true;
      resolve(Array.from(selected.values()));
      closeSheet();
    });

    openSheet(content, {
      onClose: () => { if (!resolved) resolve(null); },
    });

    loadCities().then((cities) => {
      for (const id of selectedIds) {
        const c = cities.find((x) => x.id === id);
        if (c) selected.set(id, { id: c.id, name: c.name, cn: c.cn });
      }
      renderList(cities, '');
    });

    const input = search.querySelector('input');
    input.addEventListener('input', () => {
      loadCities().then((cities) => renderList(cities, input.value));
    });
    input.focus();

    function renderList(cities, query) {
      const q = query.trim().toLowerCase();
      const filtered = q
        ? cities.filter((c) => c.name.toLowerCase().includes(q) || (c.cn || '').includes(q))
        : cities;
      list.innerHTML = '';
      for (const c of filtered) {
        const row = document.createElement('button');
        row.type = 'button';
        row.className = 'picker-row' + (selected.has(c.id) ? ' selected' : '');
        row.innerHTML = `
          <span><span class="name">${esc(c.name)}</span><span class="cn">${esc(c.cn || '')}</span></span>
          <span class="check">✓</span>
        `;
        row.addEventListener('click', () => {
          if (selected.has(c.id)) selected.delete(c.id);
          else selected.set(c.id, { id: c.id, name: c.name, cn: c.cn });
          row.classList.toggle('selected');
        });
        list.appendChild(row);
      }
      if (!filtered.length) {
        list.innerHTML = `<div class="empty-msg" style="padding:20px;text-align:center;color:var(--ink-soft)">No cities match.</div>`;
      }
    }
  });
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
