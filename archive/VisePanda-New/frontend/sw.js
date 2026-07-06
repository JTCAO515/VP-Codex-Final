// v3.8 Service Worker — offline support + caching
const CACHE = "visepanda-v1";

self.addEventListener("install", (e) => {
  e.waitUntil(
    caches.open(CACHE).then((cache) =>
      cache.addAll([
        "/",
        "/chat",
        "/dashboard",
        "/app.js",
        "/chat.js",
        "/i18n.js",
        "/itinerary-planner.js",
        "/frontend/index.html",
        "/frontend/chat.html",
        "/frontend/dashboard.html",
        "/frontend/404.html",
      ])
    )
  );
});

self.addEventListener("fetch", (e) => {
  // Skip API calls - let them go to network
  if (e.request.url.includes("/api/")) return;

  e.respondWith(
    caches.match(e.request).then(
      (cached) =>
        cached ||
        fetch(e.request).then((resp) => {
          if (resp.ok && resp.type === "basic") {
            const clone = resp.clone();
            caches.open(CACHE).then((cache) => cache.put(e.request, clone));
          }
          return resp;
        })
    )
  );
});
