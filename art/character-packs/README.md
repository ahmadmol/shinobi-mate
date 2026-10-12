# ShinobiMate character art

The deployable packs live in `img/<Character>/` and `Shimeji-ee/img/<Character>/`.
Each pack contains 59 unique transparent RGBA PNGs, all exactly 128×128.
These six high-resolution atlases are **art sources**, kept outside the engine's
image folders so future reference images can be used to refine individual poses.

The art was generated using the built-in ImageGen tool. `prompts.json` records
the prompt specifications, visual-reference roles, atlas order, and export rules.
Asta uses the supplied `images (23).jpg` and `images (27).jpg`; Naruto uses the
supplied `images.png`. Sasuke uses the existing character's identity and Asta's
new atlas for style. Existing previews are preserved in the output backup.

For each character:

- `*-core.png`: 36 principal poses and expressions.
- `*-utility.png`: 24 secondary poses; the final neutral reference cell is used
  to calibrate scale, while the first 23 are exported to the required filenames.
- `*.crops.json`: inspected alpha-island bounds. Some adjacent Asta/Sasuke
  sprites touch in the source; reviewed separating boundaries are recorded by
  `tools/inspect-character-atlas.py` rather than assuming a uniform grid.
- `*-frames.json`: every engine filename, source cell, crop, scale, and alignment.

Re-export after refining sources (PowerShell from the repository root):

```powershell
& '<python-with-Pillow>' tools/inspect-character-atlas.py art/character-packs/AstaBlackClover-core.png
& '<python-with-Pillow>' tools/inspect-character-atlas.py art/character-packs/AstaBlackClover-utility.png
powershell -NoProfile -ExecutionPolicy Bypass -File tools/build-character-art.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tools/create-character-preview.ps1
& '<python-with-Pillow>' tools/audit-character-art.py
```

Export keeps floor poses on a common foot baseline, wall pixels at/after x=16,
and ceiling hands at y=0. It removes detached crop noise, downsamples with
antialiasing, and synchronizes both engine folders. It never renames frames or
rewrites existing Naruto/Sasuke behavior files. Asta inherits the current stable
five-file configuration; its legacy `HandSign.png` filename displays the sword
pose through `CuteIdle`.

Review artifacts, complete backups, and check logs are in
`output/character-art-refresh/`. In particular `preview.html` is a self-contained
animated art preview, `overview.png` compares the three packs, and the Arabic
report states the remaining animation and test limitations.

To rerun engine checks with a JDK and the installed application JAR:

```powershell
javac -encoding UTF-8 -cp Shimeji-ee/app/Shimeji-ee.jar -d output/character-art-refresh/check-classes tools/PolishEngineCheck.java tools/CharacterArtEngineCheck.java tools/CharacterNativeSmoke.java
Set-Location Shimeji-ee
java --enable-native-access=ALL-UNNAMED -cp '../output/character-art-refresh/check-classes;app/Shimeji-ee.jar' CharacterArtEngineCheck
```

`CharacterNativeSmoke` requires an isolated configuration with
`AlwaysShowShimejiChooser=false` and an `ActiveShimeji` list matching its argument.
It runs the installed `Main.run()` and verifies real Windows native images and
visible native windows, then shuts down its own test process. Do not run it
against the user's ordinary settings; the supplied results used the isolated
`output/character-art-refresh/gui-app` folder.
