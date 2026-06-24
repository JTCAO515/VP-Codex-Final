// Itinerary — localStorage when unauthed, /api/itinerary when authed.
import { api } from './api.js';

const KEY = 'vp.itinerary';
const DEFAULT_DAYS = [
  { day_index: 1, label: 'Day 1', slots: { morning: '', afternoon: '', evening: '' } },
  { day_index: 2, label: 'Day 2', slots: { morning: '', afternoon: '', evening: '' } },
  { day_index: 3, label: 'Day 3', slots: { morning: '', afternoon: '', evening: '' } },
];

export async function get() {
  if (window.vp.user) {
    try {
      const data = await api.get('/api/itinerary');
      if (Array.isArray(data.days) && data.days.length) return data.days;
    } catch (_) {}
  }
  try {
    const raw = localStorage.getItem(KEY);
    if (raw) {
      const days = JSON.parse(raw);
      if (Array.isArray(days) && days.length) return days;
    }
  } catch (_) {}
  return JSON.parse(JSON.stringify(DEFAULT_DAYS));
}

export async function save(days) {
  try { localStorage.setItem(KEY, JSON.stringify(days)); } catch (_) {}
  if (window.vp.user) {
    try { await api.put('/api/itinerary', { days }); return true; } catch (_) {}
  }
  return false;
}
