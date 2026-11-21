# Erna-Wetter – Regeln für Änderungen

Eine Quelle, zwei Ziele:
- **Tablet** (Galaxy Tab A, Chrome): https://tossolino.github.io/erna-wetter/
- **Windows-Widget** (Edge-App-Fenster): dieselbe Seite mit `?widget=1`

Regeln:
1. Jede Änderung wird in `src/template.html` gemacht und gilt damit immer für Tablet UND Widget.
   Unterschiede nur über die CSS-Klasse `.widget` bzw. die JS-Variable `WIDGET` – nie zwei getrennte Versionen.
2. Jede Veröffentlichung bekommt eine neue Versionsnummer in `src/VERSION` (Semantic Versioning:
   PATCH = Fehlerbehebung, MINOR = neue Funktion, MAJOR = großer Umbau) und einen Eintrag in `src/CHANGELOG.md`.
3. Bauen mit `python3 src/build.py`, danach committen und auf `main` pushen. GitHub Pages veröffentlicht automatisch;
   Tablet und Widget laden die neue Version innerhalb von 3 Stunden selbst (`version.json`).
4. Vor dem Push testen: `index.html?demo=trocken|regen|schnee`, `&modus=geburtstag|schlaf|...`, `&zeit=HHMM`, einmal mit `&widget=1`
   in Fenstergröße ca. 720×450 und einmal in 1280×800 (Tablet).
5. Änderungen an `src/windows/` (Einrichtungsskript) aktualisieren sich NICHT automatisch auf dem PC –
   nur nötig, wenn sich das Starten des Widgets ändert. Dann den Nutzer bitten, das Paket neu einzurichten.
6. Nutzer-Texte auf Deutsch.
