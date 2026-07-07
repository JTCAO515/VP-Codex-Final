const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const app = fs.readFileSync(path.resolve(__dirname, "..", "app.js"), "utf8");
const html = fs.readFileSync(path.resolve(__dirname, "..", "index.html"), "utf8");
const css = fs.readFileSync(path.resolve(__dirname, "..", "app.css"), "utf8");

test("chat reads server-sent event tokens from a streaming response", () => {
  assert.match(app, /response\.body\.getReader\(\)/);
  assert.match(app, /line\.startsWith\("data:"\)/);
  assert.match(app, /payload\.token/);
});

test("chat inserts traveler text with textContent", () => {
  assert.match(app, /target\.textContent \+= payload\.token/);
  assert.match(app, /\.message__body", node\)\.textContent = text/);
});

test("chat protects streaming JSON parsing and sends auth header when available", () => {
  assert.match(app, /try\s*{\s*payload = JSON\.parse/s);
  assert.match(app, /headers\.Authorization = `Bearer \$\{state\.token\}`/);
  assert.match(app, /fetchWithTimeout\("\/api\/chat"/);
});

test("chat waits long enough for backend remote-model fallback", () => {
  assert.match(app, /fetchWithTimeout\("\/api\/chat"[\s\S]*45000\)/);
});

test("chat startup checks llm health and surfaces fallback status", () => {
  assert.match(app, /function loadLlmHealth/);
  assert.match(app, /\/api\/health/);
  assert.match(app, /llm-status/);
});

test("chat renders a live itinerary beside the conversation", () => {
  assert.match(html, /class="chat-layout"/);
  assert.match(html, /id="itineraryLive"/);
  assert.match(html, /id="itineraryTimeline"/);
  assert.match(html, /id="shareItinerary"/);
  assert.match(html, /id="downloadItinerary"/);
  assert.match(css, /\.chat-layout\s*{[\s\S]*?grid-template-columns:/);
  assert.match(css, /\.itinerary-live\s*{[\s\S]*?order: 1/s);
  assert.match(css, /\.chat-layout \.chat-shell\s*{[\s\S]*?order: 2/s);
  assert.match(css, /\.itinerary-live/);
  assert.match(css, /\.itinerary-timeline::before/);
  assert.match(app, /function renderLiveItinerary/);
  assert.match(app, /renderLiveItinerary\(message, "planning"\)/);
});
