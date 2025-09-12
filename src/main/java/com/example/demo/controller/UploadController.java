// src/main/java/com/example/demo/controller/UploadController.java
package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {
    private final FileStorageService storage;

    public UploadController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = storage.save(file); // e.g. /uploads/abc123.jpg
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
