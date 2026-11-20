"""Schneidet Ernas Fotos fürs Display zu und gleicht Belichtung/Farbe leicht an."""
import sys, numpy as np
from PIL import Image, ImageFilter
SRC = sys.argv[1]   # Ordner mit den Originalfotos
OUT = sys.argv[2]
# Datei, Zuschnitt (x0,y0,x1,y1), Name
JOBS = [
    ('1.jpg', (0, 250, 1126, 2000), 'normal'),
    ('3.jpg', (0, 380, 1126, 2000), 'geburtstag'),
    ('4.jpg', (560, 0, 1300, 1126), 'schnee'),
    ('5.jpg', (0, 420, 1126, 2000), 'regen'),
    ('6.jpg', (500, 60, 1240, 1126), 'schlaf'),
]
T_MEAN, T_STD, T_SAT = 112, 60, 68   # gemeinsames Ziel
AMOUNT = 0.4                          # nur leicht angleichen (40 %)
H = 1200

def grade(im):
    a = np.asarray(im, float)
    L = a @ [0.299, 0.587, 0.114]
    m, s = L.mean(), L.std()
    # Helligkeit/Kontrast teilweise zum Ziel
    gain = 1 + AMOUNT * (T_STD / s - 1)
    shift = AMOUNT * (T_MEAN - m)
    a = (a - m) * gain + m + shift
    # Weißabgleich: Farbstich teilweise neutralisieren
    ch = a.reshape(-1, 3).mean(0)
    a += 0.3 * (ch.mean() - ch)
    a = np.clip(a, 0, 255)
    # Sättigung teilweise zum Ziel
    hsv = np.asarray(Image.fromarray(a.astype(np.uint8)).convert('HSV'), float)
    sat = hsv[..., 1].mean()
    f = 1 + AMOUNT * (T_SAT / sat - 1)
    grey = (a @ [0.299, 0.587, 0.114])[..., None]
    a = grey + (a - grey) * f
    # gemeinsamer Look: leicht warm, sanfte S-Kurve, weiche Vignette
    a[..., 0] += 4; a[..., 2] -= 4
    x = np.clip(a / 255, 0, 1)
    x = x + 0.12 * x * (1 - x) * (2 * x - 1) * -1 * -1   # sanfte S-Kurve
    h, w = x.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    r = np.sqrt(((xx - w / 2) / (w / 2)) ** 2 + ((yy - h / 2) / (h / 2)) ** 2)
    x *= (1 - 0.22 * np.clip(r - 0.55, 0, 1) ** 1.5)[..., None]
    return Image.fromarray((np.clip(x, 0, 1) * 255).astype(np.uint8))

for fn, box, name in JOBS:
    im = Image.open(f'{SRC}/{fn}').convert('RGB').crop(box)
    im = im.resize((round(im.width * H / im.height), H), Image.LANCZOS)
    im = grade(im).filter(ImageFilter.UnsharpMask(radius=1.2, percent=40, threshold=2))
    im.save(f'{OUT}/{name}.jpg', quality=78, optimize=True, progressive=True)
    a = np.asarray(im.convert('L'), float)
    print(name, im.size, 'L %.0f std %.0f' % (a.mean(), a.std()))
