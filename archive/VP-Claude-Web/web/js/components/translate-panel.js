// Translate & scan panel — opened from the Tools card. Restyled for v8
// (terracotta accents) version of the v7 translate experience: type or
// speak, get a bilingual result, play audio, show fullscreen for a driver.

import { api } from '../api.js';
import { openSheet, closeSheet, sheetHeader } from './sheet.js';
import * as voice from '../voice.js';

export function openTranslatePanel() {
  let direction = 'en->zh';
  let current = null;
  let recording = null;

  const content = document.createElement('div');
  content.className = 'sheet-content translate-content';
  content.appendChild(sheetHeader('Translate & scan'));
  content.innerHTML += `
    <div id="tr-hero"></div>
    <div style="display:flex;align-items:center;gap:10px;margin:10px 0;font-size:var(--text-base);color:var(--ink-5)">
      <span id="tr-dir-label">English → 中文</span>
      <button type="button" id="tr-swap" style="font-size:var(--text-sm);color:var(--brand);background:var(--brand-tint);padding:4px 10px;border-radius:12px;border:none;cursor:pointer">⇄ swap</button>
    </div>
    <form id="tr-form" style="display:flex;gap:8px;align-items:end;background:var(--canvas-warm);border-radius:10px;padding:8px">
      <textarea id="tr-input" rows="1" placeholder="Type English to translate…"
        style="flex:1;border:none;background:transparent;font:inherit;padding:8px 10px;resize:none;color:var(--ink-1)"></textarea>
      <button type="button" id="tr-mic" aria-label="Record"
        style="width:40px;height:40px;border-radius:8px;background:var(--canvas);border:1px solid var(--line-1);cursor:pointer">🎤</button>
      <button type="submit" aria-label="Translate"
        style="width:40px;height:40px;border-radius:8px;background:var(--brand);color:#fff;border:none;cursor:pointer">→</button>
    </form>
  `;
  openSheet(content, { wide: true });

  const heroEl = content.querySelector('#tr-hero');
  const dirLabel = content.querySelector('#tr-dir-label');
  const form = content.querySelector('#tr-form');
  const input = content.querySelector('#tr-input');
  const micBtn = content.querySelector('#tr-mic');

  renderHero();

  content.querySelector('#tr-swap').addEventListener('click', () => {
    direction = direction === 'en->zh' ? 'zh->en' : 'en->zh';
    dirLabel.textContent = direction === 'en->zh' ? 'English → 中文' : '中文 → English';
    input.placeholder = direction === 'en->zh' ? 'Type English to translate…' : '输入中文进行翻译…';
  });

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const v = input.value.trim();
    if (!v) return;
    input.value = '';
    submit(v);
  });
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); form.requestSubmit(); }
  });

  micBtn.addEventListener('click', () => {
    if (recording) { recording.stop(); recording = null; micBtn.style.background = 'var(--canvas)'; return; }
    micBtn.style.background = 'var(--brand-tint)';
    recording = voice.record({
      onResult: ({ text }) => {
        micBtn.style.background = 'var(--canvas)';
        recording = null;
        if (text) submit(text);
      },
      onError: () => { micBtn.style.background = 'var(--canvas)'; recording = null; },
    });
    setTimeout(() => { if (recording) { recording.stop(); recording = null; micBtn.style.background = 'var(--canvas)'; } }, 8000);
  });

  function renderHero() {
    if (!current) {
      heroEl.innerHTML = `
        <div style="background:var(--canvas-warm);border-left:2px solid var(--brand);border-radius:8px;padding:14px 16px;margin-bottom:6px">
          <div style="font-size:var(--text-sm);color:var(--ink-5)">Please take me to this address.</div>
          <div style="font-family:var(--font-display);font-size:20px;color:var(--ink-1);margin-top:2px">请带我去这个地址。</div>
          <div style="font-size:var(--text-sm);color:var(--ink-faint);font-style:italic;margin-top:2px">qǐng dài wǒ qù zhè ge dì zhǐ</div>
        </div>
      `;
      return;
    }
    const t = current;
    heroEl.innerHTML = `
      <div style="border:1px solid var(--line-3);border-radius:10px;padding:16px;position:relative">
        <div style="font-size:var(--text-base);color:var(--ink-5);margin-bottom:6px">${esc(t.direction === 'en->zh' ? t.source : t.target)}</div>
        <div style="font-family:var(--font-display);font-size:24px;color:var(--ink-1);line-height:1.3">${esc(t.direction === 'en->zh' ? t.target : t.source)}</div>
        ${t.pinyin ? `<div style="font-size:var(--text-sm);color:var(--ink-faint);font-style:italic;margin-top:2px">${esc(t.pinyin)}</div>` : ''}
        ${t.note ? `<div style="margin-top:10px;padding-top:10px;border-top:1px solid var(--line-4);font-size:var(--text-sm);color:var(--ink-5)">${esc(t.note)}</div>` : ''}
        <div style="display:flex;gap:8px;margin-top:12px">
          <button type="button" id="tr-play" style="font-size:var(--text-sm);padding:7px 12px;border-radius:8px;border:1px solid var(--line-1);background:var(--canvas);cursor:pointer">▶ Play</button>
          <button type="button" id="tr-full" style="font-size:var(--text-sm);padding:7px 12px;border-radius:8px;border:none;background:var(--brand);color:#fff;cursor:pointer">⤢ Show driver</button>
        </div>
      </div>
    `;
    heroEl.querySelector('#tr-play').addEventListener('click', () => {
      voice.play(t.direction === 'en->zh' ? t.target : t.source);
    });
    heroEl.querySelector('#tr-full').addEventListener('click', () => showFullscreen(t));
  }

  async function submit(text) {
    current = { source: text, target: '…', pinyin: null, note: null, direction };
    renderHero();
    try {
      const res = await api.post('/api/translate', { text, direction });
      current = {
        source: res.source || text, target: res.target || '',
        pinyin: res.pinyin || null, note: res.note || null, direction,
      };
      renderHero();
      voice.play(direction === 'en->zh' ? current.target : current.source);
    } catch (_) {
      current = { source: text, target: 'Translation unavailable', pinyin: null, note: null, direction };
      renderHero();
    }
  }
}

function showFullscreen(t) {
  const overlay = document.createElement('div');
  overlay.style.cssText = `
    position:fixed;inset:0;background:#000;z-index:200;
    display:flex;flex-direction:column;align-items:center;justify-content:center;
    text-align:center;padding:24px;cursor:pointer;
  `;
  const zh = t.direction === 'en->zh' ? t.target : t.source;
  overlay.innerHTML = `
    <div style="color:#fff;font-family:var(--font-display);font-size:clamp(48px,16vw,140px);line-height:1.1">${esc(zh)}</div>
    <div style="color:#999;font-size:14px;margin-top:24px">Tap to exit</div>
  `;
  overlay.addEventListener('click', () => overlay.remove());
  document.body.appendChild(overlay);
  if (navigator.wakeLock && navigator.wakeLock.request) {
    navigator.wakeLock.request('screen').catch(() => {});
  }
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
