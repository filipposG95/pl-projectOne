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
            @RequestParam("algorithms") String algorithms,
            @RequestParam("folder") MultipartFile folder) throws FileNotFoundException, IOException {

        // Save the uploaded folder to a temp location
        File tempDir = new File("C://Users//filip//Submissions");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Save the uploaded folder contents to the temp directory
        String folderName = folder.getOriginalFilename();
        File tempFolder = new File(tempDir, folderName);
        tempFolder.mkdirs();

        // Save the uploaded folder as a zip file
        File zipFile = new File(tempDir, folderName + ".zip");
        folder.transferTo(zipFile);

        // Unzip the folder into the temp directory
        try {
            ZipUtils.unzip(zipFile.getAbsolutePath(), tempFolder.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Call the method to run JPlag
        String message = jPlagCallService.runJPlagWithReportFromUi(tempFolder);

        // Delete the temporary folder and zip file after processing
        if (zipFile.exists()) {
            zipFile.delete();
        }
        if (tempFolder.exists()) {
            tempFolder.delete();
        }

        return ResponseEntity.ok(message);
    }

}