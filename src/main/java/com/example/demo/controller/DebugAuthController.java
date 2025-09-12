package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
public class DebugAuthController {

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "message", "No session / not logged in"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream()
                        .map(a -> a.getAuthority()).collect(Collectors.toList())
        ));
    }
}
