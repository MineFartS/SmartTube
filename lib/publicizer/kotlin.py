from philh_myftp_biz.pc import Path, loc
from philh_myftp_biz.process import Run

def to_java(src:Path):

    dst = src.with_ext('java')

    tmp_path = loc.temp.child('translate_kotlin_to_java')
    tmp_path.delete()

    bytecode_build_dir = tmp_path.child("/build/")
    bytecode_build_dir.mkdir()
    
    decompiled_dir = tmp_path.child("/decompiled/")
    decompiled_dir.mkdir()

    Run([
        'kotlinc', src,
        "-d", bytecode_build_dir,
        "-jvm-target", "17"
    ])

    Run([
        "java", "-jar", "vineflower-1.10.1.jar",
        "--inline-records=1",
        "--dump-code-lines=0",
        bytecode_build_dir,
        decompiled_dir
    ])

    with dst.open('w') as f:
        for java_file in decompiled_dir._pure.glob("**/*.java"):
            dst.write( java_file.read_text("utf-8") )

    return dst

