# Erna-Wetter

Wetteranzeige für das Tablet am Eingang (Galaxy Tab A 10.1, Querformat):
Frostwarnung, Gassi-Vorschläge für Erna, Regen Stunde für Stunde. Ernas Bild
wechselt je nach Geburtstag, Schlafenszeit, Schnee und Regen.

**Anzeige:** https://tossolino.github.io/erna-wetter/
Wetterdaten: [Open-Meteo](https://open-meteo.com/) (Deutscher Wetterdienst).

## Auf dem Tablet
1. In Chrome https://tossolino.github.io/erna-wetter/ öffnen.
2. Menü ⋮ > „Zum Startbildschirm hinzufügen“ (Verknüpfung, keine App –
   die installierte App stürzt auf dem Galaxy Tab A ab).
3. Über das Erna-Symbol starten, einmal tippen für Vollbild.
   Querformat über „Automatisch drehen“ aus. Updates kommen automatisch.
4. Bildschirm dauerhaft an: Entwickleroptionen > „Aktiv lassen“.

## Widget für Windows
1. https://tossolino.github.io/erna-wetter/Erna-Widget-Windows.zip herunterladen und entpacken.
2. `Einrichten.cmd` doppelklicken (legt „Erna-Wetter“ auf den Desktop, optional Autostart).
Das Widget zeigt dieselbe Seite mit `?widget=1` und aktualisiert sich ebenfalls automatisch.

## Android-App mit Widgets
1. Auf dem Handy https://github.com/Tossolino/erna-wetter/releases/latest/download/erna-app.apk herunterladen.
2. Datei öffnen, Installation aus dieser Quelle erlauben, installieren.
3. Widgets: Startbildschirm lange drücken > Widgets > „Erna“ > 5×2 oder 5×3.

## Neue Version veröffentlichen
1. Änderungen in `src/template.html`.
2. Versionsnummer in `src/VERSION` erhöhen (Semantic Versioning: MAJOR.MINOR.PATCH).
3. Eintrag in `src/CHANGELOG.md` ergänzen.
4. `python3 src/build.py`, dann committen und pushen.
   Das Tablet lädt die neue Version innerhalb von 3 Stunden automatisch.

## Aufbau
- `index.html` – die Anzeige (wird gebaut, nicht direkt bearbeiten)
- `version.json` – aktuelle Versionsnummer für die Update-Prüfung
- `sw.js`, `manifest.webmanifest`, `icon-*.png` – App-Installation und Offline-Start
- `src/` – Quelltext, Bilder und Build-Skript
