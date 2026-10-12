// Replaces duplicated sit/wall/creep frames with distinct poses from the fix sheets.
const fs = require('fs');
const path = require('path');
const sharp = require('C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');

const root = path.resolve(__dirname, '..');
const assets = 'C:/Users/molha/.cursor/projects/d-programm-Shimeji-ee-2-0-1-Windows-Portable/assets';

function dist(r, g, b, kr, kg, kb) {
  const dr = r - kr, dg = g - kg, db = b - kb;
  return Math.sqrt(dr * dr + dg * dg + db * db);
}

async function loadKeyed(file) {
  const { data, info } = await sharp(file).ensureAlpha().raw().toBuffer({ resolveWithObject: true });
  const w = info.width, h = info.height;
  const corners = [[2, 2], [w - 3, 2], [2, h - 3], [w - 3, h - 3]];
  let kr = 0, kg = 0, kb = 0;
  for (const [x, y] of corners) {
    const i = (y * w + x) * 4;
    kr += data[i]; kg += data[i + 1]; kb += data[i + 2];
  }
  kr /= 4; kg /= 4; kb /= 4;
  const out = Buffer.alloc(w * h * 4);
  for (let p = 0; p < w * h; p++) {
    const i = p * 4;
    let r = data[i], g = data[i + 1], b = data[i + 2];
    const d = dist(r, g, b, kr, kg, kb);
    let a = 255;
    if (d < 78) a = 0;
    else if (d < 145) a = Math.round(((d - 78) / 67) * 255);
    if (a > 0 && a < 250) {
      const af = a / 255;
      r = Math.max(0, Math.min(255, Math.round((r - kr * (1 - af)) / af)));
      g = Math.max(0, Math.min(255, Math.round((g - kg * (1 - af)) / af)));
      b = Math.max(0, Math.min(255, Math.round((b - kb * (1 - af)) / af)));
    }
    if (g < 48 && r > g + 22 && b > g + 4 && r < 210 && b < 160) a = 0;
    out[i] = r; out[i + 1] = g; out[i + 2] = b; out[i + 3] = a;
  }
  return { data: out, width: w, height: h };
}

function cellBox(img, index) {
  const cw = Math.floor(img.width / 4);
  const ch = Math.floor(img.height / 3);
  const c = index % 4, r = Math.floor(index / 4);
  return { x0: c * cw + 2, y0: r * ch + 2, x1: (c + 1) * cw - 3, y1: (r + 1) * ch - 3 };
}

function trim(img, box) {
  let minX = box.x1, minY = box.y1, maxX = box.x0, maxY = box.y0;
  for (let y = box.y0; y <= box.y1; y++) {
    for (let x = box.x0; x <= box.x1; x++) {
      if (img.data[(y * img.width + x) * 4 + 3] > 28) {
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
      }
    }
  }
  return { minX, minY, maxX, maxY };
}

async function pngOf(img, box) {
  const w = box.maxX - box.minX + 1;
  const h = box.maxY - box.minY + 1;
  const buf = Buffer.alloc(w * h * 4);
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const s = ((box.minY + y) * img.width + (box.minX + x)) * 4;
      const d = (y * w + x) * 4;
      buf[d] = img.data[s]; buf[d + 1] = img.data[s + 1]; buf[d + 2] = img.data[s + 2]; buf[d + 3] = img.data[s + 3];
    }
  }
  return sharp(buf, { raw: { width: w, height: h, channels: 4 } }).png().toBuffer();
}

async function existingHeight(file) {
  const { data, info } = await sharp(file).ensureAlpha().raw().toBuffer({ resolveWithObject: true });
  let minY = info.height, maxY = 0;
  for (let y = 0; y < info.height; y++) {
    for (let x = 0; x < info.width; x++) {
      if (data[(y * info.width + x) * 4 + 3] > 24) {
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
      }
    }
  }
  return maxY - minY + 1;
}

async function apply(character, sheetFile, refIndex, map) {
  const img = await loadKeyed(sheetFile);
  const ref = trim(img, cellBox(img, refIndex));
  const refH = ref.maxY - ref.minY + 1;
  const target = await existingHeight(path.join(root, 'Shimeji-ee/img', character, 'Stand.png'));
  const scale = target / refH;
  const dest = path.join(root, 'Shimeji-ee/img', character);
  for (const [name, spec] of Object.entries(map)) {
    const box = trim(img, cellBox(img, spec.index));
    const bw = box.maxX - box.minX + 1;
    const bh = box.maxY - box.minY + 1;
    let w2 = Math.max(1, Math.round(bw * scale));
    let h2 = Math.max(1, Math.round(bh * scale));
    if (w2 > 122 || h2 > 126) {
      const fit = Math.min(122 / w2, 126 / h2);
      w2 = Math.round(w2 * fit);
      h2 = Math.round(h2 * fit);
    }
    const sprite = await sharp(await pngOf(img, box)).resize(w2, h2, { fit: 'fill' }).png().toBuffer();
    let left = spec.mode === 'wall' ? 2 : Math.round(64 - w2 / 2);
    let top = spec.mode === 'seat' ? 112 - h2 : 128 - h2;
    if (left < 0) left = 0;
    if (top < 0) top = 0;
    if (left + w2 > 128) left = 128 - w2;
    if (top + h2 > 128) top = 128 - h2;
    await sharp({
      create: { width: 128, height: 128, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
    }).composite([{ input: sprite, left, top }]).png().toFile(path.join(dest, name));
    console.log(character, name, 'cell', spec.index, w2 + 'x' + h2);
  }
}

(async () => {
  await apply('NarutoShippuden', path.join(assets, 'blonde-fix-sheet.jpg'), 8, {
    'Sit.png': { index: 0, mode: 'floor' },
    'SitLookUp.png': { index: 1, mode: 'floor' },
    'SitLegsUp.png': { index: 2, mode: 'seat' },
    'SitLegsDown.png': { index: 3, mode: 'floor' },
    'SitDangle1.png': { index: 4, mode: 'floor' },
    'SitDangle2.png': { index: 3, mode: 'floor' },
    'SitHeadTurn1.png': { index: 5, mode: 'floor' },
    'SitHeadTurn2.png': { index: 6, mode: 'floor' },
    'SitHeadTurn3.png': { index: 7, mode: 'floor' },
    'SitHeadTurn4.png': { index: 1, mode: 'floor' },
    'SitHeadTurn5.png': { index: 4, mode: 'floor' },
    'SitHeadTurn6.png': { index: 6, mode: 'floor' },
    'GrabWall.png': { index: 9, mode: 'wall' },
    'ClimbWallExtend.png': { index: 9, mode: 'wall' },
    'ClimbWallContract.png': { index: 10, mode: 'wall' },
    'CreepExtend.png': { index: 11, mode: 'floor' },
    'CreepContract.png': { index: 10, mode: 'floor' },
  });
  await apply('SasukeShippuden', path.join(assets, 'sasuke-identity-sheet.jpg'), 7, {
    'Sit.png': { index: 0, mode: 'floor' },
    'SitLookUp.png': { index: 1, mode: 'floor' },
    'SitLegsUp.png': { index: 2, mode: 'seat' },
    'SitLegsDown.png': { index: 3, mode: 'floor' },
    'SitDangle1.png': { index: 3, mode: 'floor' },
    'SitDangle2.png': { index: 4, mode: 'floor' },
    'SitHeadTurn1.png': { index: 4, mode: 'floor' },
    'SitHeadTurn2.png': { index: 5, mode: 'floor' },
    'SitHeadTurn3.png': { index: 0, mode: 'floor' },
    'SitHeadTurn4.png': { index: 1, mode: 'floor' },
    'SitHeadTurn5.png': { index: 5, mode: 'floor' },
    'SitHeadTurn6.png': { index: 4, mode: 'floor' },
    'GrabWall.png': { index: 6, mode: 'wall' },
    'ClimbWallExtend.png': { index: 7, mode: 'wall' },
    'ClimbWallContract.png': { index: 8, mode: 'wall' },
    'Falling.png': { index: 11, mode: 'floor' },
  });
})().catch((err) => { console.error(err); process.exit(1); });
