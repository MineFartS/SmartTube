from philh_myftp_biz.pc import loc
from dataclasses import dataclass

p = loc.script.parent.child('/src/main/res/values/values.xml')

@dataclass
class Item:
    x: int
    name: str
    kind: str

items: list[Item] = []

to_del: list[int] = []

lines: list[str] = p.open('r').readlines()

for x, line in enumerate(lines):

    if ' name="' in line:

        items += [Item(
            x = x,
            name = line.split('name="')[1].split('"')[0],
            kind = line.split('<')[1].split(' ')[0]        
        )]

for item in items:

    copies = list(filter(
        lambda i: i.name == item.name,
        items
    ))

    if len(copies) > 1:
        for copy in copies:
            if copy.kind in ['string', 'integer', 'dimen']:
                print(f'{copy.name=}')    
                to_del += [copy.x]
                break

to_del.sort()
to_del.reverse()

for x in to_del:
    del lines[x]

p.open('w').writelines(lines)

