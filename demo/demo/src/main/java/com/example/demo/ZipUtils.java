package com.example.demo;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ZipUtils {

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new FileInputStream(zipFilePath))) {
            ArchiveEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File entryFile = new File(destDirectory, entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        IOUtils.copy(zipIn, fos);
                    }
                }
            }
        }
    }
}
