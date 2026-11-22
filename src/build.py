"""Baut die Anzeige ins Hauptverzeichnis des Repositorys (wird von GitHub Pages ausgeliefert).
Aufruf:  python3 src/build.py
Vorher:  Versionsnummer in src/VERSION erhöhen und Eintrag in src/CHANGELOG.md ergänzen."""
import base64, json, pathlib, shutil, re, datetime, zipfile
d = pathlib.Path(__file__).parent
out = d.parent
version = (d/'VERSION').read_text().strip()
assert re.fullmatch(r'\d+\.\d+\.\d+', version), 'VERSION muss MAJOR.MINOR.PATCH sein'
update_url = (d/'UPDATE_URL').read_text().strip() if (d/'UPDATE_URL').exists() else ''
if update_url and not update_url.endswith('/'):
    update_url += '/'
cl = (d/'CHANGELOG.md').read_text(encoding='utf-8')
m = re.search(r'## ' + re.escape(version) + r'.*?\n- (.+)', cl)
assert m, f'Im CHANGELOG fehlt ein Eintrag "## {version}"'
html = (d/'template.html').read_text(encoding='utf-8')
html = html.replace('__APP_VERSION__', version).replace('__UPDATE_URL__', update_url)
for name in ['normal', 'regen', 'schnee', 'schlaf', 'geburtstag']:
    html = html.replace(f'__IMG_{name}__', base64.b64encode((d/'img'/f'{name}.jpg').read_bytes()).decode())
html = html.replace('__LOGO__', base64.b64encode((d/'img'/'logo.webp').read_bytes()).decode())
assert '__' + 'IMG_' not in html and '__LOGO__' not in html and '__APP_VERSION__' not in html
(out/'index.html').write_text(html, encoding='utf-8')
(out/'erna-wetter.html').write_text(html, encoding='utf-8')
(out/'version.json').write_text(json.dumps({'version': version, 'date': datetime.date.today().isoformat(),
                                            'notes': m.group(1)}, ensure_ascii=False, indent=2), encoding='utf-8')
for f in ['sw.js', 'manifest.webmanifest', 'icon-192.png', 'icon-512.png', 'CHANGELOG.md']:
    shutil.copy(d/f, out/f)
shutil.copy(d/'video'/'gassi.mp4', out/'gassi.mp4')
shutil.copy(d/'datenschutz.html', out/'datenschutz.html')
# Versionsnummer der Android-App (für den Update-Hinweis in der App)
app_v = (out/'android'/'VERSION').read_text().strip()
(out/'app-version.json').write_text(json.dumps({'version': app_v,
    'apk': 'https://github.com/Tossolino/erna-wetter/releases/latest/download/erna-app.apk'}, indent=2), encoding='utf-8')
# Windows-Widget-Paket
with zipfile.ZipFile(out/'Erna-Widget-Windows.zip', 'w', zipfile.ZIP_DEFLATED) as z:
    for f in sorted((d/'windows').iterdir()):
        z.write(f, 'Erna-Widget/' + f.name)
print('Version', version, '| Update-Adresse:', update_url or '(keine)')
