const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const html = fs.readFileSync(path.join(__dirname, '..', 'index.html'), 'utf8');

test('chat 主消息容器 id 唯一', () => {
  const matches = html.match(/id="chat-messages"/g) || [];
  assert.equal(matches.length, 1);
});

test('chat 历史查看器使用独立容器 id', () => {
  const matches = html.match(/id="chat-viewer-messages"/g) || [];
  assert.equal(matches.length, 1);
});

test('home page includes atlas sections', () => {
  assert.match(html, /id="hero-actions"/);
  assert.match(html, /class="hero-metrics"/);
  assert.match(html, /class="editorial-lead"/);
  assert.match(html, /id="trust-layer"/);
  assert.match(html, /id="editorial-city-rail"/);
  assert.match(html, /id="planner-entry"/);
});

test('home 首屏包含竖屏压缩所需结构钩子', () => {
  assert.match(html, /class="hero-content hero-content-portrait"/);
  assert.match(html, /class="hero-note-card hero-note-card-compact"/);
  assert.match(html, /class="planner-entry-card planner-entry-card-compact"/);
});

test('chat 视图包含 atlas action rail', () => {
  assert.match(html, /id="chat-action-rail"/);
});

test('chat 视图包含输入区与 action rail 的移动端安全区结构', () => {
  assert.match(html, /class="chat-container chat-mobile-shell"/);
  assert.match(html, /id="chat-action-rail" class="chat-action-rail chat-action-rail-mobile"/);
  assert.match(html, /class="chat-input-bar chat-input-bar-safe"/);
});

test('trips 视图包含 recent \/ saved 分组容器', () => {
  assert.match(html, /class="trips-atlas-note"/);
  assert.match(html, /id="trips-recent"/);
  assert.match(html, /id="trips-saved"/);
});

test('trips 与 tools 视图包含单手浏览的移动端壳层', () => {
  assert.match(html, /class="section trips-atlas trips-atlas-mobile"/);
  assert.match(html, /class="section tools-section tools-section-mobile"/);
});

test('cities 视图包含 editorial filter rail', () => {
  assert.match(html, /id="cities-filter-rail"/);
  assert.match(html, /data-filter="all"/);
  assert.match(html, /data-filter="history"/);
  assert.match(html, /data-filter="food"/);
});

test('cities filter rail 声明横向滑动结构', () => {
  assert.match(html, /id="cities-filter-rail" class="cities-filter-rail" aria-label="City filters" data-scrollable="true"/);
});

test('visible version is updated to v5.0.5', () => {
  assert.match(html, /v5\.0\.5/);
});

test('admin 页面包含 atlas 风格 hero 概览头', () => {
  const adminHtml = fs.readFileSync(path.join(__dirname, '..', 'admin.html'), 'utf8');
  assert.match(adminHtml, /class="admin-hero"/);
  assert.match(adminHtml, /class="admin-kicker"/);
});
