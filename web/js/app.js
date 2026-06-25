// VisePanda v8 — left sidebar + main router (Ask / Plan / Cities / Tools / Trips).

import { api } from './api.js';
import * as sidebar from './sidebar.js';
import * as ask from './ask.js';
import * as plan from './plan.js';
import * as cities from './cities.js';
import * as tools from './tools.js';
import * as trips from './trips.js';
import * as auth from './auth.js';

window.vp = window.vp || {};
window.vp.features = {
  has_deepseek: false,
  has_voice: false,
  has_supabase: false,
  has_email: false,
  has_google: false,
};
window.vp.user = null;

const VIEWS = ['ask', 'plan', 'cities', 'tools', 'trips'];

async function boot() {
  try {
    const data = await api.get('/api/config/public');
    window.vp.features = { ...window.vp.features, ...data };
  } catch (_) {}
  try {
    const data = await api.get('/api/auth/profile');
    if (data && data.user) window.vp.user = data.user;
  } catch (_) {}

  sidebar.mount({
    container: document.getElementById('sidebar'),
    onNav: handleNav,
  });

  // Initial route — Ask landing.
  setView('ask');

  window.addEventListener('vp:auth-required', () => auth.openSignIn());

  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').catch(() => {});
  }
}

function handleNav(key, opts = {}) {
  if (key === 'account') {
    auth.openAccount();
    return;
  }
  if (VIEWS.includes(key)) setView(key, opts);
}

function setView(name, opts = {}) {
  sidebar.setActive(name);
  const main = document.getElementById('main');
  main.innerHTML = '';
  main.className = 'main';
  if (name === 'ask') {
    ask.mount({
      container: main,
      sessionId: opts.session_id || null,
      freshChat: !!opts.fresh,
      onAddToTrip: (note) => {
        alert('Added to trip: ' + note.slice(0, 80) + (note.length > 80 ? '…' : ''));
      },
    });
  } else if (name === 'plan') {
    plan.mount({ container: main });
  } else if (name === 'cities') {
    cities.mount({ container: main });
  } else if (name === 'tools') {
    tools.mount({
      container: main,
      onAsk: () => setView('ask', { fresh: true }),
    });
  } else if (name === 'trips') {
    trips.mount({
      container: main,
      onPlanNew: () => setView('plan'),
    });
  }
}

boot();
