// Extracts chroma-keyed sprite sheets into 128x128 Shimeji frames.
const fs = require('fs');
const path = require('path');
const sharp = require('C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');

const root = path.resolve(__dirname, '..');
const assets = 'C:/Users/molha/.cursor/projects/d-programm-Shimeji-ee-2-0-1-Windows-Portable/assets';

const sheets = {
  GA: path.join(assets, 'whitehair-sheet.jpg'),
  GB: path.join(assets, 'whitehair-sheet-b.jpg'),
  GC: path.join(assets, 'whitehair-sheet-c.jpg'),
  GD: path.join(assets, 'whitehair-sheet-d.jpg'),
  SA: path.join(assets, 'pinkhair-sheet-a.jpg'),
  SB: path.join(assets, 'pinkhair-sheet-b.jpg'),
  SC: path.join(assets, 'pinkhair-sheet-c.jpg'),
  SD: path.join(assets, 'pinkhair-sheet-d.jpg'),
};

function frame(sheet, index, mode) {
  return { sheet, index, mode };
}

// Shared pose map. Sukuna skips the rope frame on sheet D.
const gojo = {
  'WalkLeft.png': frame('GA', 0, 'floor'),
  'WalkPass1.png': frame('GA', 1, 'floor'),
  'WalkRight.png': frame('GA', 2, 'floor'),
  'WalkPass2.png': frame('GA', 3, 'floor'),
  'Stand.png': frame('GA', 4, 'floor'),
  'Preview.png': frame('GA', 4, 'floor'),
  'Smile.png': frame('GA', 5, 'floor'),
  'Blink.png': frame('GA', 6, 'floor'),
  'MouseSurprise.png': frame('GA', 7, 'floor'),
  'HandSign.png': frame('GA', 8, 'floor'),
  'NinjaJump.png': frame('GA', 9, 'floor'),
  'Sit.png': frame('GA', 10, 'floor'),
  'DragWince.png': frame('GB', 0, 'floor'),
  'SitLookUp.png': frame('GB', 1, 'floor'),
  'SitLegsUp.png': frame('GB', 2, 'seat'),
  'Falling.png': frame('GB', 4, 'floor'),
  'BounceCompress.png': frame('GB', 5, 'floor'),
  'JumpStart.png': frame('GB', 5, 'floor'),
  'BounceExtend.png': frame('GB', 6, 'floor'),
  'ThrowWindow.png': frame('GB', 7, 'floor'),
  'CreepExtend.png': frame('GB', 8, 'floor'),
  'CreepContract.png': frame('GB', 9, 'floor'),
  'GrabCeiling.png': frame('GB', 10, 'ceiling'),
  'ClimbCeiling2.png': frame('GB', 10, 'ceiling'),
  'ClimbCeiling1.png': frame('GB', 11, 'ceiling'),
  'GrabWall.png': frame('GC', 0, 'wall'),
  'ClimbWallExtend.png': frame('GC', 1, 'wall'),
  'ClimbWallContract.png': frame('GC', 2, 'wall'),
  'StandWithWindow.png': frame('GC', 3, 'floor'),
  'PullUpEffort.png': frame('GC', 4, 'floor'),
  'PullUpHalf.png': frame('GC', 4, 'floor'),
  'hug1.png': frame('GC', 5, 'floor'),
  'hug2.png': frame('GC', 6, 'floor'),
  'DivideTwist1.png': frame('GC', 9, 'floor'),
  'DivideComplete.png': frame('GC', 10, 'floor'),
  'DivideSplit.png': frame('GC', 10, 'floor'),
  'StruggleHard1.png': frame('GC', 11, 'floor'),
  'SitHeadTurn1.png': frame('GD', 0, 'floor'),
  'SitHeadTurn2.png': frame('GD', 1, 'floor'),
  'SitHeadTurn3.png': frame('GD', 2, 'floor'),
  'SitHeadTurn4.png': frame('GD', 3, 'floor'),
  'SitHeadTurn5.png': frame('GD', 4, 'floor'),
  'SitHeadTurn6.png': frame('GD', 5, 'floor'),
  'SitLegsDown.png': frame('GD', 6, 'hang'),
  'SitDangle1.png': frame('GD', 6, 'hang'),
  'SitDangle2.png': frame('GD', 7, 'hang'),
  'Resist1.png': frame('GD', 8, 'floor'),
  'DivideTwist2.png': frame('GD', 8, 'floor'),
  'Resist2.png': frame('GD', 9, 'floor'),
  'DivideTwist3.png': frame('GD', 9, 'floor'),
  'StruggleHard2.png': frame('GD', 9, 'floor'),
  'StruggleHard3.png': frame('GD', 10, 'floor'),
  'PullUpPrepare.png': frame('GC', 1, 'floor'),
  'StruggleHard4.png': frame('GD', 11, 'floor'),
  'hug3.png': frame('GD', 11, 'floor'),
  'hug4.png': frame('GC', 5, 'floor'),
  'PullUpComplete.png': frame('GA', 4, 'floor'),
  'WalkWithWindow1.png': frame('GA', 0, 'floor'),
  'WalkWithWindow2.png': frame('GA', 2, 'floor'),
};

const sukuna = {
  'WalkLeft.png': frame('SA', 0, 'floor'),
  'WalkPass1.png': frame('SA', 1, 'floor'),
  'WalkRight.png': frame('SA', 2, 'floor'),
  'WalkPass2.png': frame('SA', 3, 'floor'),
  'Stand.png': frame('SA', 4, 'floor'),
  'Preview.png': frame('SA', 4, 'floor'),
  'Smile.png': frame('SA', 5, 'floor'),
  'Blink.png': frame('SA', 6, 'floor'),
  'MouseSurprise.png': frame('SA', 7, 'floor'),
  'HandSign.png': frame('SA', 9, 'floor'),
  'NinjaJump.png': frame('SA', 10, 'floor'),
  'Sit.png': frame('SA', 11, 'floor'),
  'DragWince.png': frame('SB', 0, 'floor'),
  'SitLookUp.png': frame('SB', 1, 'floor'),
  'SitLegsUp.png': frame('SB', 2, 'seat'),
  'Falling.png': frame('SB', 4, 'floor'),
  'BounceCompress.png': frame('SB', 5, 'floor'),
  'JumpStart.png': frame('SB', 5, 'floor'),
  'BounceExtend.png': frame('SB', 6, 'floor'),
  'ThrowWindow.png': frame('SB', 7, 'floor'),
  'CreepExtend.png': frame('SB', 8, 'floor'),
  'CreepContract.png': frame('SB', 9, 'floor'),
  'GrabCeiling.png': frame('SB', 10, 'ceiling'),
  'ClimbCeiling2.png': frame('SB', 10, 'ceiling'),
  'ClimbCeiling1.png': frame('SB', 11, 'ceiling'),
  'GrabWall.png': frame('SC', 0, 'wall'),
  'ClimbWallExtend.png': frame('SC', 1, 'wall'),
  'ClimbWallContract.png': frame('SC', 2, 'wall'),
  'StandWithWindow.png': frame('SC', 3, 'floor'),
  'PullUpEffort.png': frame('SC', 4, 'floor'),
  'PullUpHalf.png': frame('SC', 4, 'floor'),
  'hug1.png': frame('SC', 5, 'floor'),
  'hug2.png': frame('SC', 6, 'floor'),
  'DivideTwist1.png': frame('SC', 9, 'floor'),
  'DivideComplete.png': frame('SC', 10, 'floor'),
  'DivideSplit.png': frame('SC', 10, 'floor'),
  'StruggleHard1.png': frame('SC', 11, 'floor'),
  'SitHeadTurn1.png': frame('SD', 0, 'floor'),
  'SitHeadTurn2.png': frame('SD', 1, 'floor'),
  'SitHeadTurn3.png': frame('SD', 2, 'floor'),
  'SitHeadTurn4.png': frame('SD', 3, 'floor'),
  'SitHeadTurn5.png': frame('SD', 4, 'floor'),
  'SitHeadTurn6.png': frame('SD', 5, 'floor'),
  'SitLegsDown.png': frame('SD', 6, 'hang'),
  'SitDangle1.png': frame('SD', 6, 'hang'),
  'SitDangle2.png': frame('SD', 7, 'hang'),
  'Resist1.png': frame('SD', 9, 'floor'),
  'DivideTwist2.png': frame('SD', 9, 'floor'),
  'Resist2.png': frame('SD', 10, 'floor'),
  'DivideTwist3.png': frame('SD', 10, 'floor'),
  'StruggleHard2.png': frame('SD', 10, 'floor'),
  'StruggleHard3.png': frame('SC', 11, 'floor'),
  'PullUpPrepare.png': frame('SC', 1, 'floor'),
  'StruggleHard4.png': frame('SD', 11, 'floor'),
  'hug3.png': frame('SD', 11, 'floor'),
  'hug4.png': frame('SC', 5, 'floor'),
  'PullUpComplete.png': frame('SA', 4, 'floor'),
  'WalkWithWindow1.png': frame('SA', 0, 'floor'),
  'WalkWithWindow2.png': frame('SA', 2, 'floor'),
};

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
    out[i] = r; out[i + 1] = g; out[i + 2] = b; out[i + 3] = a;
  }
  return { data: out, width: w, height: h, key: [kr, kg, kb] };
}

function cellBox(img, index) {
  const cols = 4, rows = 3;
  const cw = Math.floor(img.width / cols);
  const ch = Math.floor(img.height / rows);
  const c = index % cols, r = Math.floor(index / cols);
  const x0 = c * cw + 2, y0 = r * ch + 2;
  const x1 = (c + 1) * cw - 3, y1 = (r + 1) * ch - 3;
  return { x0, y0, x1, y1 };
}

function trim(img, box, wall) {
  const { data, width } = img;
  let minX = box.x1, minY = box.y1, maxX = box.x0, maxY = box.y0;
  for (let y = box.y0; y <= box.y1; y++) {
    for (let x = box.x0; x <= box.x1; x++) {
      if (data[(y * width + x) * 4 + 3] > 28) {
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
      }
    }
  }
  if (maxX < minX) throw new Error('empty cell');
  if (wall) {
    // Drop a thin dark prop line clinging to the far left of the sprite.
    const span = maxY - minY + 1;
    for (let pass = 0; pass < 3; pass++) {
      let dark = 0;
      for (let y = minY; y <= maxY; y++) {
        const i = (y * width + minX) * 4;
        const lum = data[i] + data[i + 1] + data[i + 2];
        if (data[i + 3] > 40 && lum < 90) dark++;
      }
      if (dark > span * 0.45) {
        for (let y = minY; y <= maxY; y++) data[(y * width + minX) * 4 + 3] = 0;
        minX++;
      } else break;
    }
  }
  return { minX, minY, maxX, maxY };
}

async function spritePng(img, box) {
  const w = box.maxX - box.minX + 1;
  const h = box.maxY - box.minY + 1;
  const buf = Buffer.alloc(w * h * 4);
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const s = ((box.minY + y) * img.width + (box.minX + x)) * 4;
      const d = (y * w + x) * 4;
      buf[d] = img.data[s];
      buf[d + 1] = img.data[s + 1];
      buf[d + 2] = img.data[s + 2];
      buf[d + 3] = img.data[s + 3];
    }
  }
  return sharp(buf, { raw: { width: w, height: h, channels: 4 } }).png().toBuffer();
}

async function build(character, map) {
  const cache = new Map();
  async function getSheet(id) {
    if (!cache.has(id)) cache.set(id, await loadKeyed(sheets[id]));
    return cache.get(id);
  }
  const stand = map['Stand.png'];
  const standImg = await getSheet(stand.sheet);
  const standBox = trim(standImg, cellBox(standImg, stand.index), false);
  const standH = standBox.maxY - standBox.minY + 1;
  const targetH = 108;
  let scale = targetH / standH;

  const dest = path.join(root, 'Shimeji-ee/img', character);
  fs.mkdirSync(dest, { recursive: true });
  const report = [];

  for (const [name, spec] of Object.entries(map)) {
    const img = await getSheet(spec.sheet);
    const box = trim(img, cellBox(img, spec.index), spec.mode === 'wall');
    const bw = box.maxX - box.minX + 1;
    const bh = box.maxY - box.minY + 1;
    let s = scale;
    let w2 = Math.round(bw * s);
    let h2 = Math.round(bh * s);
    const maxW = spec.mode === 'wall' ? 118 : 120;
    const maxH = spec.mode === 'ceiling' ? 124 : 126;
    if (w2 > maxW || h2 > maxH) {
      const fit = Math.min(maxW / w2, maxH / h2);
      w2 = Math.round(w2 * fit);
      h2 = Math.round(h2 * fit);
    }
    const sprite = await sharp(await spritePng(img, box)).resize(w2, h2, { fit: 'fill' }).png().toBuffer();
    let left, top;
    if (spec.mode === 'ceiling') {
      left = Math.round(64 - w2 / 2);
      top = 0;
    } else if (spec.mode === 'wall') {
      left = 2;
      top = 128 - h2;
    } else if (spec.mode === 'seat') {
      left = Math.round(64 - w2 / 2);
      top = 112 - h2;
    } else if (spec.mode === 'hang') {
      left = Math.round(64 - w2 / 2);
      top = Math.round(96 - h2 * 0.42);
    } else {
      left = Math.round(64 - w2 / 2);
      top = 128 - h2;
    }
    if (left < 0) left = 0;
    if (top < 0) top = 0;
    if (left + w2 > 128) left = 128 - w2;
    if (top + h2 > 128) top = 128 - h2;
    const out = path.join(dest, name);
    await sharp({
      create: { width: 128, height: 128, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
    }).composite([{ input: sprite, left, top }]).png().toFile(out);
    report.push({ name, sheet: spec.sheet, index: spec.index, mode: spec.mode, box: [bw, bh], placed: [left, top, w2, h2] });
  }
  fs.writeFileSync(path.join(root, 'output', character + '-registration.json'), JSON.stringify(report, null, 2));
  console.log(character, report.length, 'frames, stand scale', scale.toFixed(3));
}

(async () => {
  fs.mkdirSync(path.join(root, 'output'), { recursive: true });
  await build('GojoSatoru', gojo);
  await build('SukunaRyomen', sukuna);
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
