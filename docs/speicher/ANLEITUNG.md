# Erna-Speicher einrichten – Familien-PIN statt Token

Danach speichert ihr Einstellungen nur noch mit einer **Familien-PIN**. Den GitHub-Token
braucht dann kein Handy mehr – er liegt einmal sicher beim „Erna-Speicher“ (kostenlos bei Cloudflare).

Dauer: ca. 10–15 Minuten, am besten am PC.

---

## Schritt 1 – GitHub-Token für den Speicher anlegen

1. https://github.com/settings/personal-access-tokens/new öffnen.
2. **Token name:** `Erna Speicher`
3. **Expiration:** im Auswahlmenü **„No expiration“** wählen (kein eigenes Datum eintippen).
4. **Repository access:** „Only select repositories“ → **erna-wetter**.
5. **Permissions → Repository permissions → Contents:** **Read and write**.
6. Unten **Generate token** → bestätigen → den Schlüssel (`github_pat_…`) **kopieren** und
   kurz in einem Textdokument zwischenspeichern (wird in Schritt 4 gebraucht, danach löschen).

## Schritt 2 – Cloudflare-Konto anlegen (kostenlos)

1. https://dash.cloudflare.com/sign-up öffnen, E-Mail und Passwort eingeben, Konto anlegen.
2. Die Bestätigungs-E-Mail von Cloudflare öffnen und den Link anklicken.
3. Fragen nach einer Website / Domain einfach überspringen – die braucht ihr nicht.

## Schritt 3 – Den Erna-Speicher anlegen

1. Im Cloudflare-Dashboard links **„Compute (Workers)“** bzw. **„Workers & Pages“** öffnen.
2. **„Create“** (bzw. „Anwendung erstellen“) → **„Create Worker“** / **„Start with Hello World“**.
3. Name: **`erna-speicher`** → **Deploy**.
4. Danach **„Edit code“** (bzw. „Code bearbeiten“) klicken. Links steht eine Datei `worker.js`
   mit Beispielcode.
5. In einem zweiten Browser-Tab das Programm öffnen:
   https://github.com/Tossolino/erna-wetter/blob/main/docs/speicher/erna-speicher.js
   → oben rechts auf das Symbol **„Copy raw file“** (zwei Quadrate) klicken.
6. Zurück bei Cloudflare: im Editor **alles markieren** (Strg+A), **löschen** und das kopierte
   Programm **einfügen** (Strg+V).
7. Oben rechts **„Deploy“** klicken.

## Schritt 4 – Token und PIN hinterlegen

1. Zurück zur Übersicht des Workers `erna-speicher` → Reiter **„Settings“** (Einstellungen)
   → **„Variables and Secrets“** (Variablen und Geheimnisse) → **„Add“**.
2. Erster Eintrag:
   - Type: **Secret**
   - Variable name: `GITHUB_TOKEN`
   - Value: den GitHub-Token aus Schritt 1 einfügen
3. **„Add“** für einen zweiten Eintrag:
   - Type: **Secret**
   - Variable name: `ERNA_PIN`
   - Value: eure **Familien-PIN** – mindestens 6 Ziffern, nicht 123456 oder ein Geburtsdatum.
4. **„Deploy“** bzw. **„Save and deploy“** klicken.
5. Das zwischengespeicherte Textdokument mit dem Token löschen.

## Schritt 5 – Adresse an Claude geben

1. In der Übersicht des Workers steht oben eine Adresse wie
   `https://erna-speicher.DEIN-NAME.workers.dev`.
2. Zum Test im Browser öffnen: Es erscheint `{"fehler":"PIN falsch."}` – **genau richtig**, der
   Speicher läuft.
3. Die Adresse an Claude schicken. Claude trägt sie ein; danach fragt die Einstellungsseite
   nur noch nach der PIN.

---

## Danach

- **Speichern:** ⚙ → Änderungen machen → unten die **Familien-PIN** eingeben → Speichern.
  „PIN auf diesem Gerät merken“ ist voreingestellt.
- **Den alten Speicher-Schlüssel** (Token „Erna Einstellungen“) braucht ihr nicht mehr:
  https://github.com/settings/personal-access-tokens → „Erna Einstellungen“ → **Delete**.
- **PIN ändern:** Cloudflare → Worker `erna-speicher` → Settings → Variables and Secrets →
  `ERNA_PIN` bearbeiten → Deploy. Danach auf den Geräten die neue PIN eingeben.
- **Videos:** max. 25 MB pro Video.
