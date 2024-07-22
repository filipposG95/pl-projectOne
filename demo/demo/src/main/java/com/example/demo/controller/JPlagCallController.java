package com.example.demo.controller;

import com.example.demo.ZipUtils;
import com.example.demo.service.JPlagCallService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    public ResponseEntity<String> runJPlagWithReportFromUi(
            @RequestParam("language") String language,
            @RequestParam("folder") MultipartFile folder) throws IOException {

        // Temporary directory to store the uploaded content
        String tempDirPath = "C://Users//filip//jplag";
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Extracting the zip file to temp folder
        String zipFileName = folder.getOriginalFilename();
        if (zipFileName == null || !zipFileName.endsWith(".zip")) {
            return ResponseEntity.badRequest().body("Uploaded file must be a zip file");
        }

        File uploadedZipFile = new File(tempDir, zipFileName);
        folder.transferTo(uploadedZipFile);

        // Create temp folder to extract the zip file
        String extractedFolderName = zipFileName.replace(".zip", "");
        File extractedFolder = new File(tempDir, extractedFolderName);
        extractedFolder.mkdirs();

        // Unzip the contents
        ZipUtils.unzip(uploadedZipFile.getAbsolutePath(), extractedFolder.getAbsolutePath());


        // Ensure the direct extracted folder contains files
        File correctFolder = extractedFolder;
        File[] files = extractedFolder.listFiles();
        if (files != null && files.length == 1 && files[0].isDirectory()) {
            correctFolder = files[0];
        }

        // Here you can pass 'language' and 'extractedFolder' to your JPlag service for processing
        String message = jPlagCallService.runJPlagWithReportFromUi( extractedFolder);

        // Cleanup
        //FileUtils.deleteQuietly(uploadedZipFile);
        if (uploadedZipFile.exists() && !uploadedZipFile.delete()) {
            System.err.println("Failed to delete file: " + uploadedZipFile.getAbsolutePath());
        }

        if (extractedFolder.exists()) {
            try {
                FileUtils.deleteDirectory(extractedFolder);
            } catch (IOException e) {
                System.err.println("Failed to delete directory: " + extractedFolder.getAbsolutePath());
                e.printStackTrace();
            }
        }


        FileUtils.deleteDirectory(extractedFolder);

        return ResponseEntity.ok(message);
    }

}