// src/main/java/com/example/demo/service/FileStorageService.java
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public String save(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IOException("Empty file");
        String ext = getExt(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        Path target = root.resolve(filename).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        // Return the public URL path for the file
        return "/uploads/" + filename;
    }

    private static String getExt(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot == -1 ? "" : name.substring(dot + 1);
    }
}
