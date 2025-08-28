package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FileUploadController {

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";  // this will look for upload.html in templates folder
    }
}
