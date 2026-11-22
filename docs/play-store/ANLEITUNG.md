# Erna im Google Play Store – Schritt für Schritt

Ziel: Die App „Erna“ über den Play Store an die Familie verteilen (**interner Test**, bis 100 Personen).
Updates laufen dann automatisch über den Play Store. Eine öffentliche Veröffentlichung ist nicht nötig.

---

## Teil A – Entwicklerkonto anlegen (einmalig, ca. 15 Minuten + einige Tage Prüfung)

1. Am PC https://play.google.com/console/signup öffnen und mit dem Google-Konto anmelden,
   das zur App gehören soll.
2. Kontotyp: **„Für mich selbst“** (privates Konto).
3. Angaben ausfüllen: Name des Entwicklers (wird im Play Store angezeigt), Kontakt-E-Mail, Telefonnummer.
4. Registrierungsgebühr **25 US-Dollar** bezahlen (einmalig).
5. **Identität bestätigen**: Google fragt nach einem Ausweis (Personalausweis oder Reisepass).
   Die Prüfung dauert meist 1–3 Tage.
6. **Android-Gerät bestätigen**: Auf dem Handy die App **„Google Play Console“** aus dem Play Store
   installieren und mit demselben Konto anmelden.

> Geschafft, wenn die Play Console „Konto bestätigt“ zeigt.

---

## Teil B – Upload-Schlüssel bei GitHub hinterlegen (einmalig, am PC)

Damit GitHub das Paket für den Play Store bauen kann. Die Anleitung steht in der Datei
**`GEHEIM-Play-Upload-Schluessel.txt`** (separat geschickt, liegt nicht im Repository).
Danach Bescheid geben – dann liegt das Paket `erna-play.aab` unter
https://github.com/Tossolino/erna-wetter/releases/latest bereit.

---

## Teil C – App in der Play Console anlegen

„Alle Apps“ → **„App erstellen“**:

| Feld | Eingabe |
|---|---|
| App-Name | `Erna` |
| Standardsprache | Deutsch – de-DE |
| App oder Spiel | App |
| Kostenlos oder kostenpflichtig | Kostenlos |
| Erklärungen | beide Häkchen setzen |

---

## Teil D – App-Inhalte (Menü „Richtlinie“ → „App-Inhalte“)

| Punkt | Antwort |
|---|---|
| Datenschutzerklärung | `https://tossolino.github.io/erna-wetter/datenschutz.html` |
| App-Zugriff | Alle Funktionen ohne besondere Zugangsdaten verfügbar |
| Werbung | Nein, enthält keine Werbung |
| Einstufung der Inhalte | Fragebogen: Kategorie **„Alle anderen App-Typen“**, alle Fragen **Nein** |
| Zielgruppe | **18 und älter** |
| Nachrichten-App | Nein |
| COVID-19-App | Nein |
| Datensicherheit | siehe unten |
| Behörden-App | Nein |
| Finanzfunktionen | Keine |
| Gesundheit | Keine Gesundheitsfunktionen |

**Datensicherheit:**
- Erhebt oder teilt die App Nutzerdaten? → **Nein**
- (Falls gefragt) Werden Daten bei der Übertragung verschlüsselt? → **Ja** (HTTPS)

---

## Teil E – Store-Eintrag (Menü „Wachsen“ → „Store-Präsenz“ → „Haupteintrag“)

**App-Name:** `Erna`

**Kurzbeschreibung** (max. 80 Zeichen):
```
Frostwarnung, Regen nach Stunden und die beste Gassi-Zeit für Erna.
```

**Vollständige Beschreibung:**
```
Erna zeigt auf einen Blick, wann der beste Moment für den Spaziergang mit dem Hund ist.

• Gassi-Vorschläge: Für die Zeitfenster 9–11, 13–15 und 19–21 Uhr berechnet Erna die
  trockenste halbe Stunde – auf die Viertelstunde genau.
• Frostwarnung für die nächsten 24 Stunden, inklusive Bodenfrost und Glätte-Hinweis.
• Regen heute Stunde für Stunde, rot markiert, wenn es so gut wie sicher regnet.
• Erna im Bild: je nach Wetter, Tageszeit und Anlass – im Regencape, im Schnee,
  schlafend oder mit Partyhut am Geburtstag.
• Widgets für den Startbildschirm in 5×2 und 5×3, Transparenz einstellbar.

Wetterdaten: Open-Meteo (Deutscher Wetterdienst). Ohne Anmeldung, ohne Werbung, ohne Tracking.
```

**Grafiken** (alle im Ordner `docs/play-store/` des Repositorys):

| Feld | Datei |
|---|---|
| App-Symbol (512 × 512) | `app-symbol-512.png` |
| Titelgrafik (1024 × 500) | `titelgrafik-1024x500.png` |
| Smartphone-Screenshots | `handy-1.png` … `handy-5-widgets.png` |
| Tablet-Screenshots (optional) | `tablet-1.png` |

**Kategorie** (unter „Store-Einstellungen“): Wetter · Kontakt-E-Mail: deine Entwickler-Adresse.

---

## Teil F – Interner Test: App hochladen und an die Familie verteilen

1. Menü **„Testen“ → „Interner Test“** → Reiter **„Tester“** → **„E-Mail-Liste erstellen“**:
   Name „Familie“, die Google-Konto-Adressen (Gmail) aller Handys eintragen → Speichern.
2. Reiter **„Releases“** → **„Neuen Release erstellen“**.
3. Bei der ersten Veröffentlichung: **„Play App-Signatur“** → **„Weiter“ / akzeptieren**
   (Google verwaltet den endgültigen Signaturschlüssel).
4. **App-Bundle hochladen**: `erna-play.aab` von
   https://github.com/Tossolino/erna-wetter/releases/latest herunterladen und hier hochladen.
5. Release-Name wird automatisch gesetzt. Versionshinweise z. B.: `Erste Version im Play Store.`
6. **„Weiter“ → „Speichern und veröffentlichen“**.
7. Reiter „Tester“ → **Link „Über das Web teilnehmen“** kopieren und an die Familie schicken.

**Auf jedem Handy:**
1. Den Test-Link öffnen → **„Tester werden“**.
2. Danach über den Link zum Play Store → **Installieren**.

---

## Teil G – Umstieg von der heruntergeladenen App

Die Play-Version ist anders signiert. Deshalb einmalig:
1. Bisherige App „Erna“ **deinstallieren** (Symbol lange drücken → Deinstallieren).
2. Erna aus dem Play Store installieren (Teil F).
3. Widgets neu hinzufügen und ggf. Transparenz neu einstellen.

---

## Teil H – Spätere Updates

- **Inhalte, Aussehen, Kacheln** (fast alle Änderungen): kommen wie bisher automatisch, ohne Play Store.
- **Neue App-Version** (selten): GitHub baut `erna-play.aab`. Hochladen wie in Teil F, Schritt 2–6.
  Die Handys aktualisieren sich danach automatisch über den Play Store.
  (Auf Wunsch lässt sich auch das Hochladen automatisieren – dafür ist ein Dienstkonto in der
  Play Console nötig.)
