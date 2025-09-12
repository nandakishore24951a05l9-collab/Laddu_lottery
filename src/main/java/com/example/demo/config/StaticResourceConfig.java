// src/main/java/com/example/demo/config/StaticResourceConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.nio.file.Paths; // ✅ added

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Files you save to ./uploads will be reachable at http://host/uploads/<name>
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:./uploads/",              // uploads folder in project root (most common)
                        "classpath:/static/uploads/",   // fallback if you placed files under static/uploads
                        // ✅ ALSO serve the old location used earlier:  <user.home>/localcraft_uploads/
                        "file:" + Paths.get(
                                System.getProperty("user.home"),
                                "localcraft_uploads"
                        ).toAbsolutePath().toString() + "/"
                )
                .setCachePeriod(3600)
                // 👇 Ensures safe path resolution; avoids odd classpath vs file confusion
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}
