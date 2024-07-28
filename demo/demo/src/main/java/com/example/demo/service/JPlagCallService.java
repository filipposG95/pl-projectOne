package com.example.demo.service;

import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.Language;
import de.jplag.clustering.ClusteringOptions;
import de.jplag.exceptions.ExitException;
import de.jplag.java.JavaLanguage;
import de.jplag.merging.MergingOptions;
import de.jplag.options.JPlagOptions;
//import de.jplag.options.SimilarityMetric;
import de.jplag.reporting.reportobject.ReportObjectFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Service
public class JPlagCallService {

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
        double similarityThreshold = 0.0;


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

}
