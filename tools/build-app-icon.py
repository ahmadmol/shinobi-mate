"""Convert the official RGBA artwork to Windows ICO and Java PNG resources."""
import argparse
from pathlib import Path
from PIL import Image

SIZES = (16, 32, 48, 64, 128, 256)
root = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("source", type=Path)
args = parser.parse_args()
image = Image.open(args.source).convert("RGBA")
# Pad non-square artwork without cropping or stretching it; retain alpha.
side = max(image.size)
canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
canvas.paste(image, ((side - image.width) // 2, (side - image.height) // 2))
resources = root / "src/main/resources/icons"
resources.mkdir(parents=True, exist_ok=True)
for size in SIZES:
    canvas.resize((size, size), Image.Resampling.LANCZOS).save(
        resources / f"ShinobiMate-{size}.png")
icon = root / "conf/ShinobiMate.ico"
canvas.save(icon, format="ICO", sizes=[(s, s) for s in SIZES])
for relative in ("conf/icon.ico", "img/icon.ico"):
    (root / relative).write_bytes(icon.read_bytes())
canvas.resize((256, 256), Image.Resampling.LANCZOS).save(root / "img/icon.png")
with Image.open(icon) as check:
    assert check.ico.sizes() == {(s, s) for s in SIZES}
    for size in SIZES:
        assert check.ico.getimage((size, size)).mode == "RGBA"
print(f"Created {icon} with sizes {SIZES}; source alpha range: {image.getchannel('A').getextrema()}")
