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
        JPlagOptions options = new JPlagOptions(jplagLanguage, submissionDirectories, Collections.emptySet())
                .withBaseCodeSubmissionDirectory(file);

        try {
            JPlagResult result = JPlag.run(options);

            // Generate report
            File reportFile = new File("C:\\Users\\filip\\Downloads\\reportFilip.zip");
            ReportObjectFactory reportObjectFactory = new ReportObjectFactory(reportFile);
            reportObjectFactory.createAndSaveReport(result);

            return reportFile;
        } catch (ExitException e) {
            throw new RuntimeException("Error occurred during JPlag analysis", e);
        }
    }

}
