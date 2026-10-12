"""Inspect alpha islands and write crop metadata; does not modify any images."""
from pathlib import Path
from collections import deque
import json
import sys
from PIL import Image

path = Path(sys.argv[1])
image = Image.open(path).convert('RGBA')
w, h = image.size
alpha = bytearray(image.getchannel('A').tobytes())
islands = []
for start in range(w*h):
    if alpha[start] < 32:
        continue
    alpha[start] = 0
    todo = deque([start])
    left = right = start % w
    top = bottom = start // w
    count = 0
    while todo:
        p = todo.popleft()
        x, y = p % w, p // w
        left, right = min(left, x), max(right, x)
        top, bottom = min(top, y), max(bottom, y)
        count += 1
        for q in ((p-1 if x else -1), (p+1 if x+1<w else -1), p-w, p+w):
            if 0 <= q < w*h and alpha[q] >= 32:
                alpha[q] = 0
                todo.append(q)
    if count >= 800:
        islands.append({'box': [left, top, right+1, bottom+1], 'pixels': count})
# The generated rows have unequal height. Sort each column by its actual islands.
columns = [[] for _ in range(6)]
for island in islands:
    b = island['box']
    column = min(5, int((b[0]+b[2])/2/w*6))
    columns[column].append(island)
for column in columns:
    column.sort(key=lambda i: (i['box'][1]+i['box'][3])/2)
# A few generated sprites touch at the hands/feet between the last two rows.
# These reviewed boundaries separate those contacts without assuming a grid.
splits = {
    'AstaBlackClover-core.png': {1:1050, 5:1059},
    'SasukeShippuden-core.png': {1:1057, 4:1075},
}
for col, cut in splits.get(path.name, {}).items():
    merged = columns[col].pop()
    l,t,r,b = merged['box']
    columns[col].extend([{'box':[l,t,r,cut], 'reviewed_split':True},
                         {'box':[l,cut,r,b], 'reviewed_split':True}])
result = {'source': path.name, 'size': [w,h], 'columns': columns}
dest = path.with_suffix('.crops.json')
dest.write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps({'source': str(path), 'islands':len(islands), 'column_counts':[len(c) for c in columns], 'crops':str(dest)}))
