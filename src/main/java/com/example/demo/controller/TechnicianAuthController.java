package com.example.demo.controller;

import com.example.demo.dto.NearbyTechnicianDTO;
import com.example.demo.model.Technician;
import com.example.demo.repository.TechnicianRepository;
import com.example.demo.service.TechnicianService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/technicians")
@CrossOrigin(origins = "*")
public class TechnicianAuthController {

    private final TechnicianRepository technicianRepository;
    private final TechnicianService technicianService;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadDir;

    public TechnicianAuthController(
            TechnicianRepository technicianRepository,
            TechnicianService technicianService,
            PasswordEncoder passwordEncoder,
            @Value("${app.upload.dir:}") String configuredUploadDir) {

        this.technicianRepository = technicianRepository;
        this.technicianService = technicianService;
        this.passwordEncoder = passwordEncoder;

        // 🔹 Serve from the same place StaticResourceConfig exposes: ./uploads
        if (configuredUploadDir != null && !configuredUploadDir.isBlank()) {
            uploadDir = Paths.get(configuredUploadDir).toAbsolutePath().normalize();
        } else {
            uploadDir = Paths.get("uploads").toAbsolutePath().normalize();  // << changed from user.home
        }

        try {
            Files.createDirectories(uploadDir);
            // ✅ debug line so you know where files are saved at runtime
            System.out.println("🔎 Uploading to: " + uploadDir.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create upload directory: " + uploadDir, e);
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        if (original.isEmpty()) throw new IOException("Empty filename");
        if (original.contains("..")) throw new IOException("Invalid filename: " + original);

        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + original;
        Path target = uploadDir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return filename; // 🔹 return filename ONLY
    }

    // =========================
    // Signup Technician
    // =========================
    @PostMapping("/signup")
    public ResponseEntity<?> signupTechnician(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam String profession,
            @RequestParam(required = false) String specialization,
            @RequestParam String experience,
            @RequestParam String location,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "shopPhoto", required = false) MultipartFile shopPhoto
    ) {
        try {
            if (technicianRepository.findByPhone(phone).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phone already registered");
            }
            if (technicianRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already registered");
            }

            int expYears;
            try {
                expYears = Integer.parseInt(experience);
            } catch (NumberFormatException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid experience value");
            }

            String profileFilename = storeFile(photo);
            String workshopFilename = null;
            if (shopPhoto != null && !shopPhoto.isEmpty()) {
                workshopFilename = storeFile(shopPhoto);
            }

            Technician technician = new Technician();
            technician.setFirstName(firstName);
            technician.setLastName(lastName);
            technician.setEmail(email);
            technician.setPhone(phone);
            technician.setProfession(profession);
            technician.setSpecialization(specialization);
            technician.setExperience(expYears);
            technician.setPassword(passwordEncoder.encode(password));
            technician.setLocation(location);

            // 🔹 Store filenames only; service will expose as /uploads/<filename>
            technician.setProfilePhoto(profileFilename);
            if (workshopFilename != null) {
                technician.setWorkshopPhoto(workshopFilename);
            }

            technicianRepository.save(technician);
            return ResponseEntity.ok("Technician account created successfully");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving images: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // =========================
    // Get All Technicians
    // =========================
    @GetMapping("/all")
    public List<Technician> getAllTechnicians() {
        return technicianService.getAll();
    }

    // =========================
    // Find By Email
    // =========================
    @GetMapping("/findByEmail")
    public ResponseEntity<?> findByEmail(@RequestParam String email) {
        Optional<Technician> technician = technicianRepository.findByEmail(email);
        return technician.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Technician not found"));
    }

    // =========================
    // Find By Phone
    // =========================
    @GetMapping("/findByPhone")
    public ResponseEntity<?> findByPhone(@RequestParam String phone) {
        Optional<Technician> technician = technicianRepository.findByPhone(phone);
        return technician.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Technician not found"));
    }

    // =========================
    // ✅ Technician detail by id (safe DTO)
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<com.example.demo.dto.TechnicianDetailDTO> getTechnicianDetail(@PathVariable Long id) {
        return technicianRepository.findById(id)
            .map(t -> new com.example.demo.dto.TechnicianDetailDTO(
                    t.getId(), t.getFirstName(), t.getLastName(), t.getPhone(), t.getEmail(),
                    t.getProfession(), t.getSpecialization(), t.getExperience(),
                    t.getLocation(), t.getProfilePhoto(), t.getWorkshopPhoto()
            ))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // =========================
    // ✅ Nearby by profession
    // =========================
    // http://localhost:8080/technicians/nearby?profession=carpenter&userLat=17.5843&userLng=78.4186&radiusKm=50
    @GetMapping("/nearby")
    public List<NearbyTechnicianDTO> nearby(
            @RequestParam String profession,
            @RequestParam double userLat,
            @RequestParam double userLng,
            @RequestParam(defaultValue = "15") double radiusKm
    ) {
        return technicianService.getNearbyByProfession(profession, userLat, userLng, radiusKm);
    }

    // =========================
    // ✅ Update technician location
    // =========================
    @PatchMapping("/{id}/location")
    public void updateLocation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String lat = body.get("lat");
        String lng = body.get("lng");
        if (lat != null && !lat.isBlank() && lng != null && !lng.isBlank()) {
            technicianService.updateLocation(id, lat.trim() + "," + lng.trim());
        }
    }
}
