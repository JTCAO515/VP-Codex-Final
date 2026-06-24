// Favorites — localStorage shadow when unauthed, /api/favorites when authed.
import { api } from './api.js';

const KEY = 'vp.favorites';

function readLocal() {
  try { return JSON.parse(localStorage.getItem(KEY) || '[]'); }
  catch (_) { return []; }
}
function writeLocal(arr) { try { localStorage.setItem(KEY, JSON.stringify(arr)); } catch (_) {} }

export async function list(kind) {
  if (window.vp.user) {
    try {
      const data = await api.get('/api/favorites' + (kind ? `?kind=${kind}` : ''));
      return data.items || [];
    } catch (_) {}
  }
  const local = readLocal();
  return kind ? local.filter((f) => f.kind === kind) : local;
}

export async function add(kind, ref_id, payload) {
  if (window.vp.user) {
    try {
      const data = await api.post('/api/favorites', { kind, ref_id, payload });
      return data.id;
    } catch (_) {}
  }
  const local = readLocal();
  if (!local.find((f) => f.kind === kind && f.ref_id === ref_id)) {
    local.push({ id: 'local-' + Date.now(), kind, ref_id, payload });
    writeLocal(local);
  }
  return null;
}

export async function remove(id, kind, ref_id) {
  if (window.vp.user && id && !String(id).startsWith('local-')) {
    try { await api.del('/api/favorites/' + id); return; } catch (_) {}
  }
  writeLocal(readLocal().filter((f) =>
    !(f.id === id || (f.kind === kind && f.ref_id === ref_id))
  ));
}

export async function has(kind, ref_id) {
  const items = await list(kind);
  return items.some((f) => f.ref_id === ref_id);
}
