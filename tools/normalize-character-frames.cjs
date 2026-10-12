// Height alignment and prop-line cleanup for the new character frames.
const fs = require('fs');
const path = require('path');
const sharp = require('C:/Users/molha/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');

const base = path.resolve(__dirname, '../Shimeji-ee/img');

function isPropMagenta(r, g, b, a) {
  if (a < 12) return true;
  return g < 48 && r > g + 22 && b > g + 4 && r < 210 && b < 160;
}

async function load(file) {
  const { data, info } = await sharp(file).ensureAlpha().raw().toBuffer({ resolveWithObject: true });
  return { data, width: info.width, height: info.height };
}

function bounds(img, alphaMin = 24) {
  let minX = img.width, minY = img.height, maxX = -1, maxY = -1;
  for (let y = 0; y < img.height; y++) {
    for (let x = 0; x < img.width; x++) {
      if (img.data[(y * img.width + x) * 4 + 3] < alphaMin) continue;
      if (x < minX) minX = x;
      if (y < minY) minY = y;
      if (x > maxX) maxX = x;
      if (y > maxY) maxY = y;
    }
  }
  if (maxX < 0) return null;
  return { minX, minY, maxX, maxY };
}

async function saveRaw(file, img) {
  await sharp(img.data, { raw: { width: img.width, height: img.height, channels: 4 } }).png().toFile(file);
}

function scrub(img) {
  let n = 0;
  for (let i = 0; i < img.data.length; i += 4) {
    if (isPropMagenta(img.data[i], img.data[i + 1], img.data[i + 2], img.data[i + 3])) {
      if (img.data[i + 3] !== 0) n++;
      img.data[i + 3] = 0;
    }
  }
  return n;
}

async function refit(file, mode, targetH) {
  const img = await load(file);
  const box = bounds(img);
  if (!box) throw new Error('empty ' + file);
  const bh = box.maxY - box.minY + 1;
  const bw = box.maxX - box.minX + 1;
  if (Math.abs(bh - targetH) < 3) return 'kept ' + bh;
  let sprite = await sharp(file)
    .extract({ left: box.minX, top: box.minY, width: bw, height: bh })
    .resize({ height: targetH, withoutEnlargement: false })
    .png()
    .toBuffer();
  let meta = await sharp(sprite).metadata();
  if (meta.width > 122) {
    sprite = await sharp(sprite).resize({ width: 122 }).png().toBuffer();
    meta = await sharp(sprite).metadata();
  }
  const w2 = meta.width;
  const h2 = meta.height;
  let left = mode === 'wall' ? 2 : Math.round(64 - w2 / 2);
  let top = mode === 'ceiling' ? 0 : 128 - h2;
  if (left < 0) left = 0;
  if (top < 0) top = 0;
  if (left + w2 > 128) left = 128 - w2;
  if (top + h2 > 128) top = 128 - h2;
  await sharp({
    create: { width: 128, height: 128, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
  }).composite([{ input: sprite, left, top }]).png().toFile(file);
  return 'h' + h2 + ' @' + left + ',' + top;
}

(async () => {
  const wallish = ['GrabWall.png', 'ClimbWallExtend.png', 'ClimbWallContract.png'];
  for (const ch of ['GojoSatoru', 'SukunaRyomen']) {
    let removed = 0;
    for (const name of wallish) {
      const file = path.join(base, ch, name);
      const img = await load(file);
      removed += scrub(img);
      await saveRaw(file, img);
    }
    console.log(ch, 'prop pixels', removed);
  }

  const standing = [
    'Blink.png', 'WalkLeft.png', 'WalkPass1.png', 'WalkRight.png', 'WalkPass2.png',
    'DragWince.png', 'HandSign.png', 'MouseSurprise.png', 'Smile.png',
    'WalkWithWindow1.png', 'WalkWithWindow2.png',
  ];
  for (const ch of ['GojoSatoru', 'SukunaRyomen']) {
    for (const name of standing) {
      console.log(ch, name, await refit(path.join(base, ch, name), 'floor', 108));
    }
    console.log(ch, 'GrabWall', await refit(path.join(base, ch, 'GrabWall.png'), 'wall', 104));
    console.log(ch, 'ClimbWallExtend', await refit(path.join(base, ch, 'ClimbWallExtend.png'), 'wall', 104));
    console.log(ch, 'ClimbWallContract', await refit(path.join(base, ch, 'ClimbWallContract.png'), 'wall', 88));
    console.log(ch, 'GrabCeiling', await refit(path.join(base, ch, 'GrabCeiling.png'), 'ceiling', 108));
    console.log(ch, 'ClimbCeiling1', await refit(path.join(base, ch, 'ClimbCeiling1.png'), 'ceiling', 108));
    console.log(ch, 'ClimbCeiling2', await refit(path.join(base, ch, 'ClimbCeiling2.png'), 'ceiling', 108));
  }
})().catch((err) => {
  console.error(err);
  process.exit(1);
});
