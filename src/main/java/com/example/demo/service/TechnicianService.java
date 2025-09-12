package com.example.demo.service;

import com.example.demo.dto.NearbyTechnicianDTO;
import com.example.demo.model.Technician;
import com.example.demo.repository.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TechnicianService {

    private final TechnicianRepository repo;

    public TechnicianService(TechnicianRepository repo) {
        this.repo = repo;
    }

    // =========================
    // Get all technicians
    // =========================
    public List<Technician> getAll() {
        return repo.findAll();
    }

    // Save a technician (signup)
    public Technician save(Technician technician) {
        return repo.save(technician);
    }

    // Search by profession
    public List<Technician> searchByProfession(String profession) {
        return repo.findByProfessionContainingIgnoreCase(profession);
    }

    // Search by specialization
    public List<Technician> searchBySpecialization(String specialization) {
        return repo.findBySpecializationContainingIgnoreCase(specialization);
    }

    // Search by location
    public List<Technician> searchByLocation(String location) {
        return repo.findByLocationContainingIgnoreCase(location);
    }

    // Find by phone
    public Optional<Technician> findByPhone(String phone) {
        return repo.findByPhone(phone);
    }

    // Find by email
    public Optional<Technician> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    // Find by firstName + lastName
    public List<Technician> findByName(String firstName, String lastName) {
        return repo.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
    }

    // --------------------- NEW: distance calc ---------------------
    private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // --------------------- NEW: build URL from filename or stored URL ---------------------
    private String toUploadsUrl(String stored) {
        if (stored == null || stored.isBlank()) return "/img/placeholder-avatar.png";
        // If already a URL like "/uploads/xxxx.jpg", keep it
        if (stored.startsWith("/uploads/")) return stored;
        // Else treat as filename and prefix /uploads/
        String enc = URLEncoder.encode(stored, StandardCharsets.UTF_8);
        return "/uploads/" + enc;
    }

    // --------------------- NEW: nearby by profession ---------------------
    public List<NearbyTechnicianDTO> getNearbyByProfession(String profession, double userLat, double userLng, double radiusKm) {
        List<Technician> techs = repo.findByProfessionContainingIgnoreCase(profession);

        return techs.stream().map(t -> {
            try {
                String loc = t.getLocation();
                if (loc == null || loc.isBlank() || !loc.contains(",")) return null;
                String[] p = loc.split(",");
                if (p.length != 2) return null;

                double lat = Double.parseDouble(p[0].trim());
                double lng = Double.parseDouble(p[1].trim());

                double d = distanceInKm(userLat, userLng, lat, lng);
                if (Double.isNaN(d) || d > radiusKm) return null;

                NearbyTechnicianDTO dto = new NearbyTechnicianDTO(
                        t.getId(),
                        t.getFirstName(),
                        t.getLastName(),
                        t.getPhone(),
                        t.getEmail(),
                        t.getProfession(),
                        t.getLocation(),
                        Math.round(d * 100.0) / 100.0
                );
                // 🔹 IMPORTANT: supply the image URL the frontend should use
                dto.setPhotoUrl(toUploadsUrl(t.getProfilePhoto()));
                return dto;
            } catch (Exception e) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingDouble(NearbyTechnicianDTO::getDistanceKm))
        .toList();
    }

    // --------------------- NEW: update location ---------------------
    @Transactional
    public void updateLocation(Long id, String location) {
        Technician t = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Technician not found: " + id));
        t.setLocation(location);
        repo.save(t);
    }
}
