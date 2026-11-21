# Änderungsprotokoll – Erna-Wetter (Tablet-Anzeige)

Versionsnummern nach [Semantic Versioning](https://semver.org/lang/de/):
MAJOR.MINOR.PATCH – MAJOR = grundlegender Umbau, MINOR = neue Funktion, PATCH = Fehlerbehebung.

## 1.7.0 – 2026-11-21
- Neu: Logo „Erna-App“ mittig zwischen Uhrzeit und Temperatur (Tablet und Widget)

## 1.6.1 – 2026-11-21
- Gassi-Video läuft nur noch während der Gassi-Zeitfenster (9–11, 13–15, 19–21 Uhr), sonst das Foto
- Test-Parameter ?zeit=HHMM für die Beispieldaten

## 1.6.0 – 2026-11-21
- Neu: Statt des Fotos auf dem Baumstumpf läuft jetzt ein Video von Erna beim Gassigehen als nahtlose Endlosschleife (Tablet und Widget)
- Geburtstag, Schlaf, Schnee und Regen zeigen weiterhin ihre Fotos

## 1.5.0 – 2026-11-20
- Neu: Widget für Windows (Edge-Fenster mit derselben Anzeige, kompakte Schriftgrößen), Paket Erna-Widget-Windows.zip
- Tablet und Widget nutzen dieselbe Quelle und werden beide automatisch aktualisiert

## 1.4.2 – 2026-11-20
- Keine Installation als App mehr (die App stürzte auf dem Galaxy Tab A ab); Startbildschirm-Symbol öffnet die Anzeige in Chrome, Vollbild per Tipp

## 1.4.1 – 2026-11-20
- Feste Querformat-Vorgabe aus der App-Beschreibung entfernt (mögliche Absturzursache der installierten App auf Android 8)

## 1.4.0 – 2026-11-20
- Online-Version auf GitHub Pages: https://tossolino.github.io/erna-wetter/
- Updates kommen automatisch aufs Tablet (Prüfung alle 3 Stunden)

## 1.3.0 – 2026-11-20
- Versionsnummer in der Anzeige (oben rechts)
- Update-Prüfung: sucht alle 3 Stunden nach einer neuen Version (wenn eine Update-Adresse eingetragen ist)
- Installierbar als App auf dem Startbildschirm (Vollbild, Querformat), offline-fähig

## 1.2.0 – 2026-11-20
- Vollbild per Tipp auf den Bildschirm (ohne Kiosk-App)

## 1.1.0 – 2026-11-20
- Ernas Bild wechselt automatisch: Geburtstag (10. Januar), Schlafenszeit (22–9:30 Uhr), Schnee, Regen, normal
- Fotos in Belichtung und Farbe aneinander angeglichen
- Schneefall wird mit abgefragt

## 1.0.0 – 2026-11-20
- Erste Version: Frostwarnung, Gassi-Vorschläge für Erna, Regen Stunde für Stunde, rote Balken ab 90 %
