package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Technician;
import com.example.demo.service.UserService;
import com.example.demo.service.TechnicianService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final TechnicianService technicianService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          TechnicianService technicianService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.technicianService = technicianService;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // CUSTOMER SIGNUP (unchanged)
    // =========================
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> signup(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.registerUser(user); // service should encode password
            response.put("status", "success");
            response.put("message", "User Registered Successfully!");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(409).body(response);
        }
    }

    // =========================
    // LOGIN (Technician first, then Customer)
    // =========================
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> creds,
                                                     HttpServletRequest request) {
        String email = creds.get("email");
        String password = creds.get("password");

        Map<String, String> response = new HashMap<>();

        // ---- 1) Try TECHNICIANS FIRST (prevents shadow-USER from stealing the login path)
        Optional<Technician> techOpt = technicianService.findByEmail(email);
        if (techOpt.isPresent()) {
            Technician tech = techOpt.get();

            // Support both BCrypted and legacy-plain tech passwords
            String stored = tech.getPassword() == null ? "" : tech.getPassword();
            boolean ok = stored.startsWith("$2")                      // BCrypt?
                    ? passwordEncoder.matches(password, stored)
                    : password.equals(stored);                        // legacy plain

            if (!ok) {
                response.put("status", "error");
                response.put("message", "Incorrect password");
                return ResponseEntity.ok(response);
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority("ROLE_TECHNICIAN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            response.put("status", "success");
            response.put("role", "technician");

            String fn = tech.getFirstName() == null ? "" : tech.getFirstName().trim();
            String ln = tech.getLastName()  == null ? "" : tech.getLastName().trim();
            String full = (fn + " " + ln).trim();
            response.put("name", full.isEmpty() ? tech.getEmail() : full);
            response.put("techId", tech.getId() == null ? "" : String.valueOf(tech.getId()));
            return ResponseEntity.ok(response);
        }

        // ---- 2) Then try USERS (customers)
        User user = userService.getUserByEmail(email);
        if (user != null) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                response.put("status", "error");
                response.put("message", "Incorrect password");
                return ResponseEntity.ok(response);
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            response.put("status", "success");
            response.put("role", "customer");
            response.put("name", user.getEmail());
            response.put("techId", "");
            return ResponseEntity.ok(response);
        }

        // ---- 3) Not found
        response.put("status", "error");
        response.put("message", "No account found with the provided email.");
        return ResponseEntity.ok(response);
    }

    // =========================
    // WHO AM I (unchanged)
    // =========================
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> out = new HashMap<>();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            out.put("authenticated", false);
            return ResponseEntity.status(401).body(out);
        }

        String email = String.valueOf(auth.getPrincipal());
        boolean isTech = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_TECHNICIAN".equalsIgnoreCase(a.getAuthority()) ||
                "technician".equalsIgnoreCase(a.getAuthority())
        );
        boolean isCustomer = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_CUSTOMER".equalsIgnoreCase(a.getAuthority()) ||
                "customer".equalsIgnoreCase(a.getAuthority())
        );

        out.put("authenticated", true);
        out.put("email", email);
        out.put("role", isTech ? "technician" : (isCustomer ? "customer" : "unknown"));

        if (isTech) {
            Optional<Technician> t = technicianService.findByEmail(email);
            if (t.isPresent()) {
                Technician tech = t.get();
                String fn = tech.getFirstName() == null ? "" : tech.getFirstName().trim();
                String ln = tech.getLastName()  == null ? "" : tech.getLastName().trim();
                String full = (fn + " " + ln).trim();
                out.put("name", full.isEmpty() ? tech.getEmail() : full);
                out.put("techId", tech.getId());
                return ResponseEntity.ok(out);
            }
        } else if (isCustomer) {
            User u = userService.getUserByEmail(email);
            out.put("name", (u != null && u.getEmail() != null) ? u.getEmail() : email);
            out.put("techId", null);
            return ResponseEntity.ok(out);
        }

        out.put("name", email);
        out.put("techId", null);
        return ResponseEntity.ok(out);
    }
}
