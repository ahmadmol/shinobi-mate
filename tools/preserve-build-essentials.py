"""Private restoration archive: Maven plus legacy packaging files/assets (runtime is in portable ZIP)."""
from pathlib import Path
import zipfile,hashlib,json
root=Path(__file__).resolve().parents[1]
destination=Path('C:/Users/molha/ShinobiMate-performance-20261102/ShinobiMate-rc3-build-and-legacy-PRIVATE.zip')
with zipfile.ZipFile(destination,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
 for directory in ['tools/maven','Shimeji-ee']:
  for path in (root/directory).rglob('*'):
   if not path.is_file():continue
   relative=path.relative_to(root).as_posix()
   if relative.startswith('Shimeji-ee/runtime/') or path.suffix in ['.log','.lck']:continue
   archive.write(path,relative)
with zipfile.ZipFile(destination) as archive:
 assert archive.testzip() is None
 for name in archive.namelist():
  assert hashlib.sha256(archive.read(name)).digest()==hashlib.sha256((root/name).read_bytes()).digest(),name
 print('PASS build/legacy ZIP CRC and every archived file matches workspace:',len(archive.namelist()))
print(destination,hashlib.sha256(destination.read_bytes()).hexdigest())
