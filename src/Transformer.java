import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Transformer {

    private static class ClassVisitor extends org.objectweb.asm.ClassVisitor {

        public String className;

        public ClassVisitor(ClassReader reader, ClassWriter writer) {            
            super(Opcodes.ASM9, writer);
            this.className = reader.getClassName().replace('/', '.');
        }
        
        private void log(String type, String suffix) {
            System.out.println(String.format("[%s] %s", type, className + suffix));
        }

        private int parse(int access) {
            int cleanAccess = access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED;
            return cleanAccess | Opcodes.ACC_PUBLIC;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            log("class", "");
            super.visit(version, parse(access), name, signature, superName, interfaces);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            log("field", "." + name);
            return super.visitField(parse(access), name, descriptor, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if ("<clinit>".equals(name)) {
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
            log("method", "." + name + "()");
            return super.visitMethod(parse(access), name, descriptor, signature, exceptions);
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            log("inner-class", name.replace('/', '.'));
            super.visitInnerClass(name, outerName, innerName, parse(access));
        }

    }

    private static class SimpleFileVisitor extends java.nio.file.SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

            if (file.toString().endsWith(".class")) {

                try {
                    
                    ClassReader reader = new ClassReader(Files.readAllBytes(file));
                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

                    reader.accept(new ClassVisitor(reader, writer), 0);

                    Files.write(file, writer.toByteArray());

                } catch (Exception e) {
                    System.err.println("  [Error] Transformation Failed for " + file.getFileName() + ": " + e.getMessage());
                }
                
            }

            return FileVisitResult.CONTINUE;
        }
    
    }

    public static void main(String[] args) throws Exception {

        Path targetDir = Paths.get(args[0]).toAbsolutePath();

        System.out.println("=== Starting ASM Publicizing Transformation in: " + targetDir + " ===");

        Files.walkFileTree(targetDir, new SimpleFileVisitor());

        System.out.println("=== Transformation Complete ===");

    }

}
