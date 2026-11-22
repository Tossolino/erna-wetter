# Erna-Wetter – Regeln für Änderungen

Eine Quelle, zwei Ziele:
- **Tablet** (Galaxy Tab A, Chrome): https://tossolino.github.io/erna-wetter/
- **Windows-Widget** (Edge-App-Fenster): dieselbe Seite mit `?widget=1`
- **Handy** (Hochformat): dieselbe Seite, Layout per `@media (orientation: portrait)`, Klasse `.phone` (kein Vollbild)
- **Android-App + Widgets 5×2/5×3** (`android/`): die App zeigt die Seite im WebView; die Widgets zeichnen
  `?kachel=5x2|5x3` unsichtbar in ein Bild. Inhalt/Aussehen der Widgets daher immer in `src/template.html` ändern.

Regeln:
1. Jede Änderung wird in `src/template.html` gemacht und gilt damit immer für Tablet UND Widget.
   Unterschiede nur über die CSS-Klasse `.widget` bzw. die JS-Variable `WIDGET` – nie zwei getrennte Versionen.
2. Jede Veröffentlichung bekommt eine neue Versionsnummer in `src/VERSION` (Semantic Versioning:
   PATCH = Fehlerbehebung, MINOR = neue Funktion, MAJOR = großer Umbau) und einen Eintrag in `src/CHANGELOG.md`.
3. Bauen mit `python3 src/build.py`, danach committen und auf `main` pushen. GitHub Pages veröffentlicht automatisch;
   Tablet und Widget laden die neue Version innerhalb von 3 Stunden selbst (`version.json`).
4. Vor dem Push testen: `index.html?demo=trocken|regen|schnee`, `&modus=geburtstag|schlaf|...`, `&zeit=HHMM`, einmal mit `&widget=1`
   in Fenstergröße ca. 720×450, einmal in 1280×800 (Tablet) und einmal als Handy hochkant (ca. 390×844).
5. Änderungen an `src/windows/` (Einrichtungsskript) aktualisieren sich NICHT automatisch auf dem PC –
   nur nötig, wenn sich das Starten des Widgets ändert. Dann den Nutzer bitten, das Paket neu einzurichten.
6. Nutzer-Texte auf Deutsch.
7. Android-App (`android/`) nur ändern, wenn sich die App-Hülle selbst ändert. Dann `android/VERSION` erhöhen
   (eigene Versionsnummer); GitHub Actions baut die APK und veröffentlicht sie als Release. Ab App 1.2.0
   aktualisiert sich die App selbst (`Updater.kt`, prüft `app-version.json` beim Öffnen und alle 30 Minuten).
   Zwei Varianten (productFlavors): `github` (APK, Debug-Schlüssel, mit Updater) und `play` (AAB für Google Play,
   Upload-Schlüssel aus den GitHub-Secrets ERNA_UPLOAD_KEYSTORE/ERNA_UPLOAD_PASSWORD, KEIN Selbst-Update –
   Play-Richtlinie). Play-Unterlagen und Anleitung: `docs/play-store/`. Build-Ergebnis mit `gh run list` / `gh run view --log-failed` prüfen.
8. Kacheln testen: `index.html?kachel=5x2&demo=trocken` in ca. 360×150 und `kachel=5x3` in ca. 360×230,
   kein Überlauf, `window.ERNA_READY` muss true werden.
