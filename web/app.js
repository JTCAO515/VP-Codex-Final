/* ═══════════════════════════════════════════════════════════
   VisePanda v3.0.1 — Frontend Application
   ═══════════════════════════════════════════════════════════ */

const VP = (function(){
  'use strict';

  // ── State ──
  const state = {
    currentView: 'home',
    messages: [],
    isStreaming: false,
    theme: document.documentElement.getAttribute('data-theme') || 'dark',
  };

  // ── DOM refs ──
  const $ = (s) => document.querySelector(s);
  const $$ = (s) => document.querySelectorAll(s);

  // ── Navigation ──
  function navigate(view) {
    state.currentView = view;

    // Update nav buttons
    $$('.nav-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.view === view);
    });

    // Show/hide views
    $$('.view').forEach(v => v.classList.remove('active'));
    const target = document.getElementById(`view-${view}`);
    if (target) target.classList.add('active');

    // Load data on demand
    if (view === 'cities') loadCities();
    if (view === 'tools') loadTools();
    if (view === 'home') loadHomeCities();

    // Update URL hash
    window.location.hash = view;
  }

  // ── Focus chat on a city ──
  function focusChat(city) {
    const input = document.getElementById('chat-input');
    if (input) {
      input.value = `Plan a trip to ${city}`;
      input.style.height = 'auto';
      toggleSendButton(true);
      input.focus();
    }
  }

  // ── Theme ──
  function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('vp_theme', next);
    state.theme = next;
    const btn = document.querySelector('.theme-toggle');
    if (btn) btn.textContent = next === 'dark' ? '🌙' : '☀️';
  }

  // ── API helpers ──
  async function apiGet(path) {
    try {
      const r = await fetch(path);
      if (!r.ok) throw new Error(`HTTP ${r.status}`);
      return await r.json();
    } catch (e) {
      console.error(`API GET ${path}:`, e);
      return null;
    }
  }

  // ── City emoji helper ──
  function getCityEmoji(name) {
    const map = {
      beijing:'🏯', shanghai:'🌃', chengdu:'🐼', guangzhou:'🥟',
      shenzhen:'🌆', hangzhou:'🌊', xi_an:'🏛️', guilin:'🏞️',
      chongqing:'🌉', kunming:'🌸', suzhou:'🏯', nanjing:'🏛️',
      lhasa:'🏔️', hong_kong:'🌃', macau:'🎰',
    };
    return map[name.toLowerCase().replace(/ /g,'_')] || '🏙️';
  }

  // ── City tag extractor ──
  function getCityTags(name, info) {
    const tags = [];
    const vibe = (info.vibe || '').toLowerCase();
    if (vibe.includes('food') || vibe.includes('cuisine')) tags.push('🍜 Foodie');
    else if (name === 'chengdu' || name === 'guangzhou') tags.push('🍜 Foodie');
    if (vibe.includes('nature') || vibe.includes('mountain') || vibe.includes('scenery') || vibe === 'nature') tags.push('🏞️ Nature');
    if (vibe.includes('history') || vibe.includes('ancient') || vibe.includes('culture')) tags.push('🏛️ History');
    if (vibe.includes('modern') || vibe.includes('city')) tags.push('🌃 Urban');
    if (vibe.includes('nightlife') || vibe.includes('vibrant')) tags.push('🌙 Nightlife');
    if (vibe.includes('relax') || vibe.includes('chill')) tags.push('🧘 Relax');
    if (!tags.length) tags.push('📍 Destination');
    return tags.slice(0, 2);
  }

  // ── Create city card element ──
  function createCityCard(name, info) {
    const card = document.createElement('div');
    card.className = 'city-card';
    card.onclick = () => { navigate('chat'); focusChat(name); };

    const emoji = getCityEmoji(name);
    const hasImg = info.image ? true : false;
    if (hasImg) card.classList.add('has-img');
    const tags = getCityTags(name, info);

    let imgHtml = '';
    if (hasImg) {
      imgHtml = `<img class="city-bg-img" src="${info.image}" alt="${name}" loading="lazy" onerror="this.parentElement.classList.remove('has-img');this.remove()">`;
    }

    card.innerHTML = imgHtml + `
      <div class="city-card-top">
        <span class="city-emoji">${emoji}</span>
      </div>
      <div class="city-card-bottom">
        <div class="city-name">${name}</div>
        <div class="city-sub">${info.name_cn || ''}</div>
        <div class="city-meta">${info.best_season || ''} · ${info.days || ''}</div>
        ${info.vibe ? `<div class="city-vibe">${info.vibe}</div>` : ''}
        ${tags.length ? `<div class="city-tags">${tags.map(t => `<span class="city-tag">${t}</span>`).join('')}</div>` : ''}
      </div>
    `;

    return card;
  }

  // ── Load Home Cities ──
  async function loadHomeCities() {
    const grid = document.getElementById('city-grid');
    if (!grid) return;
    const data = await apiGet('/api/cities');
    if (!data || !data.cities) return;

    grid.innerHTML = '';
    const entries = Object.entries(data.cities).slice(0, 8);
    entries.forEach(([name, info]) => {
      grid.appendChild(createCityCard(name, info));
    });
  }

  // ── Load Cities (all) ──
  async function loadCities() {
    const grid = document.getElementById('cities-grid');
    if (!grid) return;
    const data = await apiGet('/api/cities');
    if (!data || !data.cities) return;

    grid.innerHTML = '';
    Object.entries(data.cities).forEach(([name, info]) => {
      grid.appendChild(createCityCard(name, info));
    });
  }

  // ── Load Tools ──
  async function loadTools() {
    const grid = document.getElementById('tools-grid');
    if (!grid) return;
    const data = await apiGet('/api/tools');
    if (!data || !data.tools) return;

    grid.innerHTML = '';
    const emojis = {packing:'🧳', pricing:'💰', visa:'🛂', phrases:'💬', emergency:'🆘'};
    Object.entries(data.tools).forEach(([name, desc]) => {
      const card = document.createElement('div');
      card.className = 'tool-card';
      card.innerHTML = `
        <div style="font-size:24px;margin-bottom:8px">${emojis[name] || '🧰'}</div>
        <div style="font-size:14px;font-weight:600;margin-bottom:2px;text-transform:capitalize">${name}</div>
        <div style="font-size:12px;color:var(--text-muted)">${desc}</div>
      `;
      grid.appendChild(card);
    });
  }

  // ── Chat ──
  let abortController = null;

  // City list for auto-detection in user messages
  const CITY_NAMES = [
    'beijing','shanghai','chengdu','xian','guilin','yunnan','hangzhou',
    'guangzhou','shenzhen','chongqing','changsha','nanjing','suzhou',
    'harbin','zhangjiajie','tibet','sanya','dunhuang','luoyang','wuhan',
    'xiamen','qingdao','dali','lijiang','huangshan','jiuzhaigou','lanzhou',
    'kunming','hohhot','guiyang','fuzhou','macau','hong kong','taipei',
  ];

  function detectCity(text) {
    const lower = text.toLowerCase();
    for (const city of CITY_NAMES) {
      if (lower.includes(city)) return city;
    }
    return '';
  }

  const SUGGESTIONS = [
    '3 days in Beijing 🏯',
    'Shanghai food tour 🥟',
    'Chengdu panda trip 🐼',
    'Guilin nature escape 🏞️',
    'Xi\'an history guide 🏛️',
    'Budget tips for China 💰',
  ];

  function renderSuggestions() {
    const bar = document.getElementById('chat-suggestions');
    if (!bar) return;
    bar.innerHTML = '';
    SUGGESTIONS.forEach(s => {
      const chip = document.createElement('button');
      chip.className = 'chat-chip';
      chip.textContent = s;
      chip.onclick = () => {
        const input = document.getElementById('chat-input');
        if (input) {
          input.value = s.replace(/ 🏯| 🥟| 🐼| 🏞️| 🏛️| 💰/g, '');
          input.style.height = 'auto';
          toggleSendButton(true);
          input.focus();
        }
      };
      bar.appendChild(chip);
    });
  }

  function addMessage(text, role) {
    const container = document.getElementById('chat-messages');
    if (!container) return null;

    const msg = document.createElement('div');
    msg.className = `msg msg-${role}`;
    msg.innerHTML = `
      <div class="msg-avatar">${role === 'bot' ? '🐼' : '👤'}</div>
      <div class="msg-body">
        <div class="msg-sender">${role === 'bot' ? 'VisePanda' : 'You'}</div>
        <div class="msg-text">${text.replace(/\n/g, '<br>')}</div>
      </div>
    `;
    container.appendChild(msg);
    container.scrollTop = container.scrollHeight;
    return msg;
  }

  function addTyping() {
    const container = document.getElementById('chat-messages');
    if (!container) return null;

    const msg = document.createElement('div');
    msg.className = 'msg msg-bot';
    msg.id = 'typing-msg';
    msg.innerHTML = `
      <div class="msg-avatar">🐼</div>
      <div class="msg-body">
        <div class="msg-sender">VisePanda</div>
        <div class="msg-text typing-dots"><span>.</span><span>.</span><span>.</span></div>
      </div>
    `;
    container.appendChild(msg);
    container.scrollTop = container.scrollHeight;
    return msg;
  }

  function updateTyping(el, content) {
    if (!el) return;
    const textDiv = el.querySelector('.msg-text');
    if (textDiv && content) textDiv.innerHTML = content.replace(/\n/g, '<br>') + '<span class="cursor-blink">▌</span>';
  }

  function removeMessage(el) {
    if (el && el.parentNode) el.parentNode.removeChild(el);
  }

  function toggleSendButton(enabled) {
    const btn = document.getElementById('chat-send');
    if (btn) btn.disabled = !enabled;
  }

  function autoResize(el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
    toggleSendButton(el.value.trim().length > 0);
  }

  async function sendMessage() {
    const input = document.getElementById('chat-input');
    const text = input.value.trim();
    if (!text || state.isStreaming) return;

    input.value = '';
    input.style.height = 'auto';
    toggleSendButton(false);

    // Add user message
    addMessage(text, 'user');
    state.messages.push({role: 'user', content: text});

    // Show typing indicator
    const typingId = addTyping();

    // Stream response
    state.isStreaming = true;
    abortController = new AbortController();

    try {
      const resp = await fetch('/api/chat', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          messages: state.messages.slice(-10),
          city: detectCity(text),
        }),
        signal: abortController.signal,
      });

      if (!resp.ok) {
        removeMessage(typingId);
        addMessage('Sorry, I couldn\'t process that. Please try again.', 'bot');
        return;
      }

      const reader = resp.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let botContent = '';

      while (true) {
        const {done, value} = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, {stream: true});
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          if (!line.startsWith('data: ')) continue;
          const data = line.slice(6).trim();
          if (!data) continue;

          try {
            const parsed = JSON.parse(data);
            if (parsed.token) {
              botContent += parsed.token;
              updateTyping(typingId, botContent);
            } else if (parsed.error) {
              removeMessage(typingId);
              addMessage('Error: ' + parsed.error, 'bot');
              return;
            } else if (parsed.done) {
              // Done
            }
          } catch (e) {
            // Incomplete JSON chunk, skip
          }
        }
      }

      // Finalize
      removeMessage(typingId);
      if (botContent) {
        addMessage(botContent, 'bot');
        state.messages.push({role: 'assistant', content: botContent});
      }
    } catch (e) {
      if (e.name === 'AbortError') return;
      removeMessage(typingId);
      addMessage('Connection error. Please try again.', 'bot');
    } finally {
      state.isStreaming = false;
      abortController = null;
    }
  }

  function stopStreaming() {
    if (abortController) {
      abortController.abort();
      abortController = null;
    }
  }

  // ── Init ──
  function init() {
    // Theme toggle icon
    const themeBtn = document.querySelector('.theme-toggle');
    if (themeBtn) themeBtn.textContent = state.theme === 'dark' ? '🌙' : '☀️';

    // Hash-based nav
    const hash = window.location.hash.slice(1);
    if (hash && ['home','chat','cities','tools'].includes(hash)) {
      navigate(hash);
    }

    // Chat input events
    const chatInput = document.getElementById('chat-input');
    const chatSend = document.getElementById('chat-send');
    if (chatInput) {
      chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          sendMessage();
        }
      });
    }

    // Render chat suggestions
    renderSuggestions();
  }

  // ── Expose public API ──
  return {
    navigate,
    toggleTheme,
    focusChat,
    sendMessage,
    autoResize,
    stopStreaming,
    init,
  };
})();

// ── Auto-init ──
document.addEventListener('DOMContentLoaded', () => VP.init());
