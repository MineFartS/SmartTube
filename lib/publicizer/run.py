from philh_myftp_biz.terminal import set_package
from philh_myftp_biz.pc import loc, Path
from sys import argv

set_package(loc.script)

from . import java, kotlin

for file in Path(argv[1]).descendants:

    print(f'Modifying:', file)

    if file.ext == 'kt':
        file = kotlin.to_java(file)

    if file.ext == 'java':

        with file.open() as f:
            content = f.read()

        with file.open('w') as f:
            f.write( java.rewrite(content) )
