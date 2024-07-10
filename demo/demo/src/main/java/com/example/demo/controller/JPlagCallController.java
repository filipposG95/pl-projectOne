package com.example.demo.controller;

import com.example.demo.service.JPlagCallService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/jplag")
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



}
