// Generic overlay sheet — used by city picker, city detail, trip creation,
// translate panel. Creates its DOM on demand (no static markup needed) and
// tears down on close so multiple call sites never collide.
//
// Distinct from auth.js's #view-auth sheet (which stays mounted for the
// account menu). This one is App-wide single-instance: opening a new sheet
// while one is open replaces it.

let activeRoot = null;
let activeBackdrop = null;
let onCloseCb = null;

export function openSheet(contentEl, { onClose, wide = false } = {}) {
  closeSheet();

  const backdrop = document.createElement('div');
  backdrop.className = 'sheet-backdrop';
  backdrop.addEventListener('click', closeSheet);

  const root = document.createElement('div');
  root.className = 'sheet generic-sheet' + (wide ? ' wide' : '');
  root.setAttribute('role', 'dialog');
  root.appendChild(contentEl);

  document.body.appendChild(backdrop);
  document.body.appendChild(root);

  activeRoot = root;
  activeBackdrop = backdrop;
  onCloseCb = onClose || null;

  requestAnimationFrame(() => {
    backdrop.classList.add('open');
    root.classList.add('open');
  });

  const escHandler = (e) => { if (e.key === 'Escape') closeSheet(); };
  document.addEventListener('keydown', escHandler);
  root._escHandler = escHandler;

  return root;
}

export function closeSheet() {
  if (!activeRoot) return;
  const root = activeRoot;
  const backdrop = activeBackdrop;
  const cb = onCloseCb;
  activeRoot = null;
  activeBackdrop = null;
  onCloseCb = null;

  root.classList.remove('open');
  backdrop.classList.remove('open');
  if (root._escHandler) document.removeEventListener('keydown', root._escHandler);
  setTimeout(() => {
    root.remove();
    backdrop.remove();
  }, 200);
  if (cb) cb();
}

export function sheetHeader(title, { chop } = {}) {
  const bar = document.createElement('div');
  bar.className = 'sheet-bar';
  bar.innerHTML = `<h2>${esc(title)}</h2><button class="sheet-close" type="button" aria-label="Close">✕</button>`;
  bar.querySelector('.sheet-close').addEventListener('click', closeSheet);
  return bar;
}

function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  }[c]));
}
