from philh_myftp_biz.terminal import set_package
from philh_myftp_biz.pc import loc, Path
from philh_myftp_biz import VERBOSE
from sys import argv
import re

VERBOSE.enable()

set_package(loc.script)

patterns = [
    
    # Classes
    r'\b(private|protected|internal)(?=\s+(?:(?:abstract|sealed|data|enum|open|inner|final|synchronized)\s+)*(?:class|interface|object)\b)',
    
    # Functions
    r'\b(private|protected|internal)(?=\s+(?:(?:synchronized|final|abstract|inline|external|tailrec|operator|infix)\s+)*(?:fun|void\s+\w+|[\w<>\[\]]+\s+\w+(?=\s*\()))(?!\s+(?:[^\{]*?\b(?:open|override)\b))'

]

for file in Path(argv[1]).descendants:

    if file.ext not in ['java', 'kt']:
        continue
 
    print(f'Modifying:', file)

    with file.open() as f:
        content = f.read()

    for pat in patterns:
        content = re.sub(pat, 'public', content)

    with file.open('w') as f:
        f.write(content)
