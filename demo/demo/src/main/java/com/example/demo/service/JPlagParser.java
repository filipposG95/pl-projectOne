package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

@Service
public class JPlagParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, Object>> parse(File resultZipFile) {
        List<Map<String, Object>> results = new ArrayList<>();
        Path tempDir = null;

        try {
            tempDir = Files.createTempDirectory("jplag_result");
            unzip(resultZipFile, tempDir.toFile());

            // process each comparison JSON
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".json") && p.getFileName().toString().contains("-"))
                        .forEach(path -> {
                            try {
                                Map<String, Object> comparisonData = objectMapper.readValue(path.toFile(), Map.class);
                                String[] fileNames = path.getFileName().toString().replace(".json", "").split("-");
                                Map<String, Object> result = new HashMap<>();
                                result.put("file1", fileNames[0]);
                                result.put("file2", fileNames[1]);
                                result.put("similarity", ((Map<String, Number>)comparisonData.get("similarities")).get("AVG"));
                                results.add(result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tempDir != null) {
                cleanupTemporaryFiles(tempDir);
            }
        }

        return results;
    }

    private void unzip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                File filePath = new File(destDir, entry.getName());
                if (!entry.isDirectory()) {
                    if (!filePath.getParentFile().exists()) {
                        filePath.getParentFile().mkdirs();
                    }
                    try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath.toPath()))) {
                        byte[] bytesIn = new byte[1024];
                        int read;
                        while ((read = zipIn.read(bytesIn)) != -1) {
                            bos.write(bytesIn, 0, read);
                        }
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private void cleanupTemporaryFiles(Path directory) {
        try {
            Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
