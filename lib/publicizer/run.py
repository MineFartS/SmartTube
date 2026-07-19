from philh_myftp_biz.terminal import set_package
from philh_myftp_biz.pc import loc, Path
from philh_myftp_biz import VERBOSE
from sys import argv

VERBOSE.enable()

set_package(loc.script)

for file in Path(argv[1]).descendants:

    match file.ext:
        case 'kt':
            from .kotlin import rewrite
        case 'java':
            from .java import rewrite
        case _:
            continue
    
    print(f'Modifying:', file)

    with file.open() as f:
        content = f.read()
    
    content = rewrite(content)

    with file.open('w') as f:
        f.write(content)
