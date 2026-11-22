/**
 * Erna-Speicher – kleiner „Türsteher“ für Cloudflare Workers.
 *
 * Die Einstellungsseite der Erna-App schickt Änderungen hierher, zusammen mit der Familien-PIN.
 * Nur wenn die PIN stimmt, schreibt dieses Programm die Änderung in das GitHub-Repository.
 * Der GitHub-Token liegt ausschließlich hier (als geheimer Wert) und nie auf einem Handy.
 *
 * Geheime Werte (Cloudflare → Worker → Einstellungen → Variablen und Geheimnisse):
 *   GITHUB_TOKEN  – GitHub-Token: nur Repository erna-wetter, Contents: Read and write, ohne Ablaufdatum
 *   ERNA_PIN      – Familien-PIN (mindestens 6 Zeichen)
 */

const REPO = 'Tossolino/erna-wetter';
const ERLAUBTE_HERKUNFT = ['https://tossolino.github.io'];
const MAX_DATEI = 25 * 1024 * 1024;   // 25 MB pro Foto/Video
const MEDIEN_PFAD = /^media\/[a-z]+-\d{10,16}(-standbild)?\.(jpg|mp4)$/;

export default {
  async fetch(request, env) {
    const origin = request.headers.get('Origin') || '';
    const cors = {
      'Access-Control-Allow-Origin': ERLAUBTE_HERKUNFT.includes(origin) ? origin : ERLAUBTE_HERKUNFT[0],
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, X-Erna-Pin',
      'Access-Control-Max-Age': '86400',
      'Vary': 'Origin'
    };
    const antwort = (status, daten) =>
      new Response(JSON.stringify(daten), { status, headers: { ...cors, 'Content-Type': 'application/json; charset=utf-8' } });

    if (request.method === 'OPTIONS') return new Response(null, { status: 204, headers: cors });
    if (!env.GITHUB_TOKEN || !env.ERNA_PIN) return antwort(500, { fehler: 'Speicher ist noch nicht eingerichtet (GITHUB_TOKEN/ERNA_PIN fehlen).' });

    // PIN prüfen (mit kleiner Pause bei falscher PIN gegen Durchprobieren)
    const pin = request.headers.get('X-Erna-Pin') || '';
    if (!gleich(pin, String(env.ERNA_PIN))) {
      await new Promise(r => setTimeout(r, 1500));
      return antwort(401, { fehler: 'PIN falsch.' });
    }

    const url = new URL(request.url);
    try {
      // PIN testen
      if (request.method === 'GET' && url.pathname === '/pruefen') return antwort(200, { ok: true });

      // Foto/Video hochladen: Rohdaten im Body, Ziel in ?pfad=media/...
      if (request.method === 'POST' && url.pathname === '/datei') {
        const pfad = url.searchParams.get('pfad') || '';
        if (!MEDIEN_PFAD.test(pfad)) return antwort(400, { fehler: 'Ungültiger Dateiname.' });
        const daten = await request.arrayBuffer();
        if (daten.byteLength === 0) return antwort(400, { fehler: 'Leere Datei.' });
        if (daten.byteLength > MAX_DATEI) return antwort(413, { fehler: 'Datei zu groß (max. 25 MB).' });
        await github(env, 'PUT', pfad, { message: 'Medium ' + pfad, content: base64(daten), branch: 'main' });
        return antwort(200, { ok: true, pfad });
      }

      // Einstellungen speichern: { config: {...}, loeschen: ['media/...'] }
      if (request.method === 'POST' && url.pathname === '/einstellungen') {
        const body = await request.json();
        const cfg = pruefeConfig(body && body.config);
        if (!cfg) return antwort(400, { fehler: 'Einstellungen unvollständig oder fehlerhaft.' });
        const alt = await github(env, 'GET', 'config.json?ref=main');
        const text = JSON.stringify(cfg, null, 2) + '\n';
        await github(env, 'PUT', 'config.json', {
          message: 'Einstellungen geändert', content: base64(new TextEncoder().encode(text)),
          sha: alt ? alt.sha : undefined, branch: 'main'
        });
        // nicht mehr benutzte Medien aufräumen
        for (const p of (Array.isArray(body.loeschen) ? body.loeschen : []).slice(0, 20)) {
          if (!MEDIEN_PFAD.test(p) || text.includes(p)) continue;
          const f = await github(env, 'GET', p + '?ref=main').catch(() => null);
          if (f && f.sha) await github(env, 'DELETE', p, { message: 'Medium entfernt: ' + p, sha: f.sha, branch: 'main' }).catch(() => {});
        }
        return antwort(200, { ok: true });
      }

      return antwort(404, { fehler: 'Unbekannte Anfrage.' });
    } catch (e) {
      return antwort(502, { fehler: 'GitHub-Fehler: ' + e.message });
    }
  }
};

/** Anfrage an die GitHub-API (Inhalte des Repositorys) */
async function github(env, method, pfad, body) {
  const r = await fetch('https://api.github.com/repos/' + REPO + '/contents/' + pfad, {
    method,
    headers: {
      'Authorization': 'Bearer ' + env.GITHUB_TOKEN,
      'Accept': 'application/vnd.github+json',
      'User-Agent': 'erna-speicher',
      ...(body ? { 'Content-Type': 'application/json' } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  });
  if (method === 'GET' && r.status === 404) return null;
  if (!r.ok) {
    const t = await r.text();
    if (r.status === 401) throw new Error('GitHub-Token ungültig (im Worker neu hinterlegen).');
    if (r.status === 403) throw new Error('GitHub-Token ohne Schreibrechte.');
    throw new Error(r.status + ' ' + t.slice(0, 200));
  }
  return r.status === 204 ? {} : r.json();
}

/** Nur bekannte Felder mit sinnvollen Werten übernehmen */
function pruefeConfig(c) {
  if (!c || typeof c !== 'object') return null;
  const zeit = /^\d{2}:\d{2}$/;
  const hund = String(c.hund || '').trim().slice(0, 30);
  if (!hund || !/^\d{4}-\d{2}-\d{2}$/.test(c.geburtstag || '')) return null;
  if (!c.schlaf || !zeit.test(c.schlaf.von) || !zeit.test(c.schlaf.bis)) return null;
  const min = Math.round(Number(c.gassiMinuten));
  if (!(min >= 5 && min <= 240)) return null;
  if (!Array.isArray(c.gassiZeiten) || !c.gassiZeiten.length || c.gassiZeiten.length > 12) return null;
  const zeiten = c.gassiZeiten.map(z => ({ von: String(z.von), bis: String(z.bis) }));
  if (zeiten.some(z => !zeit.test(z.von) || !zeit.test(z.bis))) return null;
  const medien = {};
  for (const k of ['normal', 'gassi', 'regen', 'schnee', 'schlaf', 'geburtstag']) {
    const m = c.medien && c.medien[k];
    if (!m) continue;
    if (!['image', 'video'].includes(m.type) || !MEDIEN_PFAD.test(m.src)) return null;
    if (m.poster && !MEDIEN_PFAD.test(m.poster)) return null;
    medien[k] = { type: m.type, src: m.src, ...(m.poster ? { poster: m.poster } : {}) };
  }
  return { hund, geburtstag: c.geburtstag, schlaf: { von: c.schlaf.von, bis: c.schlaf.bis }, gassiMinuten: min, gassiZeiten: zeiten, medien };
}

/** Base64 in Stücken (auch für große Videos) */
function base64(daten) {
  const bytes = daten instanceof Uint8Array ? daten : new Uint8Array(daten);
  const STUECK = 3 * 32768;   // Vielfaches von 3, damit die Teile direkt aneinanderpassen
  let aus = '';
  for (let i = 0; i < bytes.length; i += STUECK) {
    const teil = bytes.subarray(i, i + STUECK);
    let s = '';
    for (let j = 0; j < teil.length; j += 8192) s += String.fromCharCode.apply(null, teil.subarray(j, j + 8192));
    aus += btoa(s);
  }
  return aus;
}

/** Vergleich in konstanter Zeit */
function gleich(a, b) {
  if (a.length !== b.length) return false;
  let d = 0;
  for (let i = 0; i < a.length; i++) d |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return d === 0;
}
