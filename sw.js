// Service Worker: Anzeige auch ohne Internet starten (zuletzt geladene Version).
// Immer zuerst aus dem Netz laden (damit Updates sofort ankommen), sonst aus dem Speicher.
var CACHE = 'erna-wetter';
self.addEventListener('install', function () { self.skipWaiting(); });
self.addEventListener('activate', function (e) { e.waitUntil(self.clients.claim()); });
self.addEventListener('fetch', function (e) {
  var url = new URL(e.request.url);
  if (e.request.method !== 'GET' || url.origin !== location.origin) return; // Wetterdaten nicht cachen
  if (e.request.headers.has('range') || /\.mp4$/.test(url.pathname)) return;   // Video: Browser lädt selbst (in Teilen)
  e.respondWith(
    fetch(e.request).then(function (res) {
      var copy = res.clone();
      caches.open(CACHE).then(function (c) { c.put(e.request, copy); });
      return res;
    }).catch(function () {
      return caches.match(e.request, { ignoreSearch: true });
    })
  );
});
