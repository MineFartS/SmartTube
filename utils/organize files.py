from philh_myftp_biz.pc import Path, loc
from dataclasses import dataclass

p = loc.script.parent.child('/src/main/')

@dataclass
class Item:
    path: Path
    pkg: str

items: list[Item] = []

for file in p.child('/java2/').descendants:

    if file.is_dir or (file.ext not in ['java', 'kt']):
        continue

    with file.open('r') as f:
        for line in f.readlines():
            if line.startswith('package '):
                items += [Item(
                    path = file,
                    pkg = line.split(' ')[1].replace(';', '').replace('\n', '').replace('minefarts.', '', 1)
                )]
                break

for item in items:

    dst = '/java/' + item.pkg.replace('.', '/') + '/' + item.path.seg()

    print(f'{item.path=}')
    print(f'{item.pkg=}')
    print(f'{dst=}')
    print()

    item.path.move(p.child(dst))

