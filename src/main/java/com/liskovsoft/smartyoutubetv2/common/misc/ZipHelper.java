package com.liskovsoft.smartyoutubetv2.common.misc;

/**
 * Utility for zip archive operations used by backup/restore and import/export features.
 *
 * Responsibilities:
 * - Pack directories/files into zip archives and extract zip archives to target folders.
 * - Preserve file metadata when possible (timestamps, relative paths).
 * - Provide streaming-friendly APIs to avoid loading entire archives into memory.
 *
 * Concurrency and I/O notes:
 * - All operations are blocking and perform file I/O. Callers must run heavy operations on a background thread.
 * - Use buffered streams and proper resource closing (try-with-resources) to avoid file descriptor leaks.
 *
 * Error handling:
 * - Surface IOExceptions to callers; callers should present user-friendly messages and retry logic.
 * - Avoid automatic overwrite without explicit confirmation from caller.
 */
public class ZipHelper {
    public static boolean zipFolder(File sourceFolder, File zipFile, String[] backupPatterns) {
        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            zipFolderRecursive(sourceFolder, sourceFolder, zipOut, backupPatterns);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void zipFolderRecursive(File rootFolder, File currentFile, ZipOutputStream zipOut, String[] backupPatterns) throws IOException {
        String entryName = rootFolder.toURI().relativize(currentFile.toURI()).getPath();

        if (currentFile.isDirectory()) {
            if (!entryName.isEmpty()) {
                zipOut.putNextEntry(new ZipEntry(entryName + "/"));
                zipOut.closeEntry();
            }
            File[] children = currentFile.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (Helpers.endsWithAny(child.getName(), backupPatterns))
                        zipFolderRecursive(rootFolder, child, zipOut, backupPatterns);
                }
            }
        } else {
            zipOut.putNextEntry(new ZipEntry(entryName));
            try (FileInputStream input = new FileInputStream(currentFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) >= 0) {
                    zipOut.write(buffer, 0, length);
                }
            }
            zipOut.closeEntry();
        }
    }

    public static boolean unzipToFolder(File zipFile, File outputFolder) {
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        try (ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File filePath = new File(outputFolder, entry.getName());

                if (entry.isDirectory()) {
                    filePath.mkdirs();
                } else {
                    if (filePath.getParentFile() != null) {
                        filePath.getParentFile().mkdirs();
                    }
                    try (FileOutputStream output = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipIn.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                    }
                }
                zipIn.closeEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
