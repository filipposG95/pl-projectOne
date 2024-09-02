package com.example.demo.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CollectFileUtils {

    public static Map<String, List<String>> collectJavaFilesPerStudent(File rootDirectory) {
        Map<String, List<String>> studentFilesMap = new HashMap<>();
        File[] studentDirectories = rootDirectory.listFiles(File::isDirectory);
        if (studentDirectories != null) {
            for (File studentDir : studentDirectories) {
                List<String> javaFiles = new ArrayList<>();
                collectJavaFilesRecursively(studentDir, javaFiles);
                studentFilesMap.put(studentDir.getName(), javaFiles);
            }
        }
        return studentFilesMap;
    }

    private static void collectJavaFilesRecursively(File directory, List<String> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectJavaFilesRecursively(file, javaFiles);
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file.getAbsolutePath());
                }
            }
        }
    }

}
