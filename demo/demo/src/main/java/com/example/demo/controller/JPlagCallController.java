package com.example.demo.controller;

import com.example.demo.ZipUtils;
import com.example.demo.service.JPlagCallService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/jplag")
@CrossOrigin(origins = "http://localhost:4200") // Adjust if necessary
public class JPlagCallController {

    private final JPlagCallService jPlagCallService;

    @Autowired
    public JPlagCallController(JPlagCallService jPlagCallService) {
        this.jPlagCallService = jPlagCallService;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runJPlag() {
        String message = jPlagCallService.runJPlag();
        return ResponseEntity.ok(message);
    }

    @PostMapping("/runJPlagWithReport")
    public ResponseEntity<String> runJPlagWithReport() throws FileNotFoundException {
        String message = String.valueOf(jPlagCallService.runJPlagWithReport());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/runJPlagWithReportFromUi")
    public ResponseEntity<Map<String, String>> runJPlagWithReportFromUi(
            @RequestParam("language") String language,
            @RequestParam("folder") MultipartFile folder) throws IOException {

        // Validate file
        if (folder.isEmpty() || !folder.getOriginalFilename().endsWith(".zip")) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Uploaded file must be a zip file");
            return ResponseEntity.badRequest().body(response);
        }

        // Create a temporary directory for the operation
        Path tempPath = Files.createTempDirectory("jplag_temp");
        File tempDir = tempPath.toFile();

        // Save the uploaded zip file to the temporary directory
        File uploadedZipFile = new File(tempDir, folder.getOriginalFilename());
        folder.transferTo(uploadedZipFile);

        // Create a directory to extract the zip file
        File extractedFolder = new File(tempDir, "extracted");
        extractedFolder.mkdirs();

        // Unzip the contents
        ZipUtils.unzip(uploadedZipFile.getAbsolutePath(), extractedFolder.getAbsolutePath());

        // Check if there is an extra nested folder and move files to the correct directory
        File correctFolder = extractCorrectFolder(extractedFolder);

        // Run JPlag processing and get the result file
        File resultZipFile = jPlagCallService.runJPlagWithReportFromUi(language, correctFolder);

        // Clean up temporary files
        deleteFile(uploadedZipFile);
        FileUtils.deleteDirectory(extractedFolder);

        // Prepare the response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Report generated successfully");
        response.put("reportFilePath", resultZipFile.getAbsolutePath());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/run-fett")
    public ResponseEntity<Map<String, Object>> runFett(
            @RequestParam("language") String language,
            @RequestParam("folder") MultipartFile folder) throws IOException {


        // Validate file
        if (folder.isEmpty() || !folder.getOriginalFilename().endsWith(".zip")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Uploaded file must be a zip file");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        // Create a temporary directory for the operation
        Path tempPath = Files.createTempDirectory("fett_temp");
        File tempDir = tempPath.toFile();

        // Save the uploaded zip file to the temporary directory
        File uploadedZipFile = new File(tempDir, folder.getOriginalFilename());
        folder.transferTo(uploadedZipFile);

        // Create a directory to extract the zip file
        File extractedFolder = new File(tempDir, "extracted");
        extractedFolder.mkdirs();

        // Unzip the contents
        ZipUtils.unzip(uploadedZipFile.getAbsolutePath(), extractedFolder.getAbsolutePath());

        // Check if there is an extra nested folder and move files to the correct directory
        File correctFolder = extractCorrectFolder(extractedFolder);

        String resultMessageJson  = jPlagCallService.runFettTool(correctFolder, language);

        // Clean up temporary files
        deleteFile(uploadedZipFile);
        FileUtils.deleteDirectory(extractedFolder);

        // Log the resultMessageJson to ensure it's correct
        System.out.println("Response JSON: " + resultMessageJson);

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report generated successfully");
        response.put("results", resultMessageJson);
        //return ResponseEntity.ok(response);
        return ResponseEntity.ok(Map.of("message", "Report generated successfully", "results", resultMessageJson));
    }

    private File extractCorrectFolder(File extractedFolder) {
        File[] files = extractedFolder.listFiles();
        if (files != null && files.length == 1 && files[0].isDirectory()) {
            return files[0];
        }
        return extractedFolder;
    }

    private void deleteFile(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}




