const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const html = fs.readFileSync(path.join(root, "index.html"), "utf8");
const app = fs.readFileSync(path.join(root, "app.js"), "utf8");

test("all primary navigation views have matching panels", () => {
  const navViews = [...html.matchAll(/data-view="([^"]+)"/g)].map((match) => match[1]);
  const panels = [...html.matchAll(/data-view-panel="([^"]+)"/g)].map((match) => match[1]);
  assert.deepEqual([...new Set(navViews)].sort(), [...new Set(panels)].sort());
});

test("v6.2.1 information architecture uses three core app tabs", () => {
  const navViews = [...html.matchAll(/class="nav__item[^"]*"[^>]*data-view="([^"]+)"/g)].map((match) => match[1]);
  assert.deepEqual(navViews, ["chat", "dashboard", "translate"]);
  assert.doesNotMatch(html, /id="tab-cities"/);
  assert.doesNotMatch(html, /id="tab-map"/);
  assert.doesNotMatch(html, /id="tab-tools"/);
  assert.doesNotMatch(html, /id="tab-trips"/);
  assert.match(html, /id="dashboardMap"/);
  assert.match(html, /id="dashboardHotels"/);
  assert.match(html, /id="dashboardDeals"/);
});

test("view switching loads the data-heavy panels on demand", () => {
  assert.match(app, /if \(view === "dashboard"\) loadDashboard\(\)/);
  assert.match(app, /loadDashboardCities\(\)/);
  assert.match(app, /loadDashboardMap\(\)/);
  assert.match(app, /loadDashboardHotels\(\)/);
  assert.match(app, /loadDashboardDeals\(\)/);
});

test("data-heavy panels render loading, empty, and error feedback", () => {
  assert.match(app, /function showToast/);
  assert.match(app, /function setStatus/);
  assert.match(app, /function emptyState/);
  assert.match(app, /function loadingCards/);
  assert.match(app, /catch \(error\)/);
});
