package com.example.demo.controller;

import com.example.demo.dto.JobRequestDTO;
import com.example.demo.model.JobRequest;
import com.example.demo.model.JobStatus;
import com.example.demo.service.JobRequestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@CrossOrigin(origins = "*")
public class JobRequestController {

    private final JobRequestService service;

    public JobRequestController(JobRequestService service) {
        this.service = service;
    }

    // ---------- helpers to resolve/mint the stable customer key ----------
    private String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private String resolveCustomerKey(HttpServletRequest req, HttpServletResponse res) {
        String key = req.getHeader("X-Customer-Key");
        if (key == null || key.isBlank()) key = readCookie(req, "lc_key");
        if (key == null || key.isBlank()) {
            key = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from("lc_key", key)
                    .httpOnly(false)
                    .secure(false)
                    .path("/")
                    .maxAge(60L * 60 * 24 * 365)
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return key;
    }
    // --------------------------------------------------------------------

    // Customer creates a booking (attach customerKey automatically)
    @PostMapping
    public ResponseEntity<JobRequest> create(@RequestBody JobRequestDTO dto,
                                             HttpServletRequest req,
                                             HttpServletResponse res) {
        String key = resolveCustomerKey(req, res);
        // 🔹 you will add this overload in the service (see “Next steps” below)
        return ResponseEntity.ok(service.create(dto, key));
    }

    // Technician views their jobs by status (default PENDING)
    @GetMapping("/for-tech/{techId}")
    public List<JobRequest> forTech(
            @PathVariable Long techId,
            @RequestParam(defaultValue = "PENDING") JobStatus status
    ) {
        return service.listForTech(techId, status);
    }

    // Customer views by phone (kept for backward compatibility)
    @GetMapping("/for-customer")
    public List<JobRequest> forCustomer(@RequestParam String phone) {
        return service.listForCustomer(phone);
    }

    // 🔹 NEW: Customer views their requests using stable key (no phone)
    @GetMapping("/for-key")
    public List<JobRequest> forKey(@RequestParam("key") String customerKey) {
        return service.listForKey(customerKey);
    }

    // Accept / Reject / etc.
    @PatchMapping("/{id}/status")
    public ResponseEntity<JobRequest> setStatus(@PathVariable Long id, @RequestParam JobStatus value) {
        return ResponseEntity.ok(service.updateStatus(id, value));
    }

    // One job
    @GetMapping("/{id}")
    public JobRequest one(@PathVariable Long id) { return service.get(id); }
}
