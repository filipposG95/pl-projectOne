package com.example.demo.service;

import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.Language;
import de.jplag.exceptions.ExitException;
import de.jplag.java.JavaLanguage;
import de.jplag.options.JPlagOptions;
import de.jplag.reporting.reportobject.ReportObjectFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class JPlagCallService {

    // Define the paths as class-level constants or variables
    private static final String RESULTS_CSV_PATH = "C://Users//filip//Downloads//results.csv";
    private static final String FETT_JAR_PATH = "C:\\Users\\filip\\IdeaProjects\\pl-projectOne\\demo\\demo\\libs\\fett.jar";
    private static final String CLASS_FILE_PATH = "C:\\Users\\filip\\12345";

    private final String SUBMISSION_DIR = "C://Users//filip//Submissions";
    private final String REPORT_DIR = "C:\\Users\\filip\\Downloads\\reportFilip.zip";


    public String runJPlag() {

        Language language = new JavaLanguage();
        Set<File> submissionDirectories = Set.of(new File("C://Users//filip//Submissions"));
        File baseCode = new File("C://Users//filip//Submissions");
        JPlagOptions options = new JPlagOptions(language, submissionDirectories, Collections.emptySet())
                .withBaseCodeSubmissionDirectory(baseCode);

        try {
            JPlagResult result = JPlag.run(options);
            return "JPlag analysis completed successfully.";
        } catch (ExitException e) {
            return "Error occurred during JPlag analysis: " + e.getMessage();
        }
    }

    public JPlagResult runJPlagWithReport() throws FileNotFoundException {
        Language language = new JavaLanguage();
        Set<File> submissionDirectories = Set.of(new File("C://Users//filip//Submissions"));
        File baseCode = new File("C://Users//filip//Submissions");
        JPlagOptions options = new JPlagOptions(language, submissionDirectories, Collections.emptySet())
                .withBaseCodeSubmissionDirectory(baseCode);

        try {
            JPlagResult result = JPlag.run(options);

            // Generate report
            ReportObjectFactory reportObjectFactory = new ReportObjectFactory(new File("C:\\Users\\filip\\Downloads\\reportFilip.zip"));
            reportObjectFactory.createAndSaveReport(result);

            return result; // Optionally return JPlagResult if needed
        } catch (ExitException e) {
            throw new RuntimeException("Error occurred during JPlag analysis", e);
        }
    }

    public File runJPlagWithReportFromUi(String language, File file) throws FileNotFoundException {
        Language jplagLanguage;
        switch (language.toLowerCase()){
            case "java":
                jplagLanguage = new JavaLanguage();
                break;
     //     case "c":
     //         jplagLanguage = new CLanguage();
     //         break;
     //     case "c++":
     //         jplagLanguage = new CppLanguage();
     //         break;
     //     case "python":
     //         jplagLanguage = new PythonLanguage();
     //         break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
        Set<File> submissionDirectories = Set.of(file); // Use the uploaded file
        //double similarityThreshold = 0.0;


        System.out.println("Running JPlag with submission directories: " + submissionDirectories);
        //System.out.println("Base code directory: " + baseCodeDirectory.getAbsolutePath());


        JPlagOptions options = new JPlagOptions(
                jplagLanguage,
                submissionDirectories,          // Use the uploaded file
                Collections.emptySet()
        );

        try {
            JPlagResult result = JPlag.run(options);

            // Generate report in a temporary directory
            Path tempReportDir = Files.createTempDirectory("jplag_report");
            File reportFile = tempReportDir.resolve("report.zip").toFile();

            ReportObjectFactory reportObjectFactory = new ReportObjectFactory(reportFile);
            reportObjectFactory.createAndSaveReport(result);

            // Return the path to the generated report file
            return reportFile;
        } catch (ExitException e) {
            throw new RuntimeException("Error occurred during JPlag analysis: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error occurred while handling JPlag report: " + e.getMessage(), e);
        }
    }

    public File runFettTool(File javaFilesDirectory, String language) {
        System.out.println("Checking directory: " + javaFilesDirectory.getAbsolutePath());

        // Validate that the directory exists and is a directory
        if (!javaFilesDirectory.exists() || !javaFilesDirectory.isDirectory()) {
            System.out.println("Invalid directory.");
            return new File("Invalid directory.");
        }

        if (!javaFilesDirectory.canRead()) {
            System.out.println("Cannot read directory: " + javaFilesDirectory.getAbsolutePath());
            return new File("Cannot read Java files directory: " + javaFilesDirectory.getAbsolutePath());
        }

        // Collect all .java files from the specified directory
        List<String> javaFiles = new ArrayList<>();
        File[] files = javaFilesDirectory.listFiles();
        if (files == null) {
            System.out.println("Unable to read contents of the directory: " + javaFilesDirectory.getAbsolutePath());
            return new File("Unable to access contents of the directory: " + javaFilesDirectory.getAbsolutePath());
        } else {
            for (File file : files) {
                System.out.println("Checking file: " + file.getAbsolutePath());
                if (file.isFile() && file.getName().endsWith(".java")) {
                    if (file.canRead()) {
                        System.out.println("Adding Java file: " + file.getAbsolutePath());
                        javaFiles.add(file.getAbsolutePath());
                    } else {
                        System.out.println("Cannot read Java file: " + file.getAbsolutePath());
                    }
                } else {
                    System.out.println("Skipping non-Java file or directory: " + file.getAbsolutePath());
                }
            }
        }

        if (javaFiles.isEmpty()) {
            System.out.println("No Java files found in: " + javaFilesDirectory.getAbsolutePath());
            return new File("No Java files found in: " + javaFilesDirectory.getAbsolutePath());
        }

        // Correct classFile path
       String correctedClassFilePath = "C:\\Users\\filip\\IdeaProjects\\plagiarism-detector\\src\\main\\resources\\new.json";
      //  String correctedClassFilePath = determineClassFilePath(language);
        // Construct the command using a list of strings to avoid issues with spaces in paths
        List<String> command = new ArrayList<>();
        command.add(language);
        command.add("-jar");
        command.add(FETT_JAR_PATH);
        command.add("-s");
        command.add("smith-waterman");
        command.add("-g");
        command.add("file");
        command.add("-X");
        command.add("similarity=classBased");
        command.add("matchScore=1");
        command.add("gapScore=-2");
        command.add("classFile=" + correctedClassFilePath);
        command.add("-c");
        command.add(RESULTS_CSV_PATH);
        command.add("-j6");
        command.add("allpairs");
        command.addAll(javaFiles);

        // Log the full command for debugging
        System.out.println("Running command: " + String.join(" ", command));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            Process process = processBuilder.start();

            String processOutput = captureProcessOutput(process);

            if (process.waitFor() == 0) {
                return new File("Fett tool ran successfully:\n" + processOutput);
            } else {
                return new File("Error running Fett tool:\n" + processOutput);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while running Fett tool: " + e.getMessage());
            return new File("Error occurred while running Fett tool: " + e.getMessage());
        }
    }

    private String captureProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        // Capture errors if the process fails
        if (process.exitValue() != 0) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    output.append(errorLine).append(System.lineSeparator());
                }
            }
        }

        return output.toString();
    }



}
