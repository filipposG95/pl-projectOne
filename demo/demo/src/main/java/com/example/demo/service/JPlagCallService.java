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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;

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
        switch (language.toLowerCase()) {
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

    public String runFettTool(File javaFilesDirectory, String language) {
        System.out.println("Checking directory: " + javaFilesDirectory.getAbsolutePath());

        // Validate that the directory exists and is a directory
        if (!javaFilesDirectory.exists() || !javaFilesDirectory.isDirectory()) {
            System.out.println("Invalid directory.");
            return "{\"error\": \"Invalid directory.\"}";
        }

        if (!javaFilesDirectory.canRead()) {
            System.out.println("Cannot read directory: " + javaFilesDirectory.getAbsolutePath());
            return "{\"error\": \"Cannot read Java files directory.\"}";
        }

        // Collect all .java files from the specified directory
        List<String> javaFiles = new ArrayList<>();
        File[] files = javaFilesDirectory.listFiles();
        if (files == null) {
            System.out.println("Unable to read contents of the directory: " + javaFilesDirectory.getAbsolutePath());
            return "{\"error\": \"Unable to access contents of the directory.\"}";
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
            return "{\"error\": \"No Java files found in the directory.\"}";
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
                // Parse results into a structured format if needed
                //  String jsonResults = parseSimilarityResults(processOutput);
                return parseResultsFromCSV(RESULTS_CSV_PATH);
            } else {
                return "{\"error\": \"Error running Fett tool\"}";
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error occurred while running Fett tool: " + e.getMessage());
            return "{\"error\": \"Error occurred while running Fett tool: " + e.getMessage() + "\"}";
        }
    }

    private String captureProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
                System.out.println("Tool Output Line: " + line); // Logging each line
            }
        }

        if (process.exitValue() != 0) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println("Tool Error Output: " + errorLine); // Log error output
                }
            }
        }

        return output.toString();
    }


    private String parseSimilarityResults(String csvOutput) {
        StringBuilder jsonBuilder = new StringBuilder("{\"results\":[");
        String[] lines = csvOutput.split(System.lineSeparator());

        if (lines.length < 2) return "{\"results\":[]}"; // If less than 2 lines, no data to process

        // The first line of the CSV seems to be the file names header, split them
        String[] files = lines[0].split(",");

        // Iterate over each pair of lines to extract similarity score except the header line
        for (int i = 1; i < lines.length; i++) {
            String[] scores = lines[i].split(",");

            // Start from the second column because the first column is a filename
            for (int j = 1; j < scores.length; j++) {
                try {
                    double similarity = Double.parseDouble(scores[j]);

                    // Avoid self-comparison which would always be 1.0
                    if (i != j && similarity != 1.0) {
                        jsonBuilder.append("{\"file1\":\"").append(files[i]).append("\",")
                                .append("\"file2\":\"").append(files[j]).append("\",")
                                .append("\"similarity\":").append(new DecimalFormat("#.##").format(similarity * 100)).append("},");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid similarity value between " + files[i] + " and " + files[j] + ": " + scores[j]);
                }
            }
        }

        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }

    private String parseResultsFromCSV(String csvFilePath) {
        StringBuilder jsonBuilder = new StringBuilder("{\"results\":[");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String[] files = br.readLine().split(","); // Read the first line for filenames (header)

            int numberOfFiles = files.length - 1;

            int rowIndex = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int colIndex = 1; colIndex <= numberOfFiles; colIndex++) {
                    if (rowIndex != colIndex - 1) { // Skip self-similarity
                        try {
                            double similarity = Double.parseDouble(values[colIndex]);
                            if (similarity != 1.0) {
                                jsonBuilder.append("{\"file1\":\"")
                                        .append(files[rowIndex + 1].replace("\\", "\\\\"))
                                        .append("\",")
                                        .append("\"file2\":\"")
                                        .append(files[colIndex].replace("\\", "\\\\"))
                                        .append("\",")
                                        .append("\"similarity\":")
                                        .append(new DecimalFormat("#.##").format(similarity * 100))
                                        .append("},");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid similarity value between " + files[rowIndex + 1] + " and " + files[colIndex] + ": " + values[colIndex]);
                        }
                    }
                }
                rowIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"results\":[]}";
        }

        if (jsonBuilder.length() > 11) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1); // Remove last comma
        }

        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }


}