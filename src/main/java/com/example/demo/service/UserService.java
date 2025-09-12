package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isValidRole(String role) {
        if (role == null) return false;
        String r = role.toLowerCase();
        return r.equals("customer") || r.equals("technician") || r.equals("carpenter");
    }

    /** Helper: detect BCrypt-looking hashes to avoid double-encoding and support legacy plaintext */
    private boolean isBCrypt(String value) {
        if (value == null) return false;
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    // Fetch user by email
    public User getUserByEmail(String email) {
        if (email == null) return null;
        // normalize for lookup without altering repository contracts
        String normalized = email.trim().toLowerCase();
        return userRepository.findByEmail(normalized);
    }

    @Transactional
    public User registerUser(User user) {
        if (!isValidRole(user.getRole())) {
            throw new IllegalArgumentException("Invalid role. Use 'customer', 'technician', or 'carpenter'.");
        }

        // normalize email once, consistently
        String normalizedEmail = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Email already exists");
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new IllegalStateException("Phone already exists");
        }

        user.setEmail(normalizedEmail);
        user.setRole(user.getRole().toLowerCase());

        // Encode only if not already a BCrypt hash (pre-seeded users)
        String raw = user.getPassword();
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (!isBCrypt(raw)) {
            user.setPassword(passwordEncoder.encode(raw));
        } else {
            user.setPassword(raw);
        }

        return userRepository.save(user);
    }

    public boolean login(String email, String rawPassword) {
        if (email == null || rawPassword == null) return false;
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail);
        if (user == null) return false;

        String stored = user.getPassword();
        if (stored == null || stored.isEmpty()) return false;

        // Standard path: BCrypt stored
        if (isBCrypt(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        // Legacy path: stored as plaintext -> compare directly, then migrate to BCrypt
        boolean ok = stored.equals(rawPassword);
        if (ok) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
        return ok;
    }

    // Return all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Fetch all technicians
    public List<User> getAllTechnicians() {
        return userRepository.findByRole("technician");
    }

    // Fetch all carpenters
    public List<User> getAllCarpenters() {
        return userRepository.findByRole("carpenter");
    }

    // Update user profile — now includes location
    @Transactional
    public User updateUserProfile(Long id, String firstName, String lastName, String phone, String location) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        if (location != null && !location.isEmpty()) {
            user.setLocation(location); // Save location dynamically
        }

        return userRepository.save(user);
    }

    // NEW: Update user latitude/longitude (stored as "lat,lng" in `location`) and optional address
    @Transactional
    public User updateUserLocation(Long userId, double latitude, double longitude, String fullAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Persist in the existing `location` field as "lat,lng"
        user.setLocation(latitude + "," + longitude);

        // If your User entity has a dedicated address field, uncomment the next line:
        // user.setLocationAddress(fullAddress);

        return userRepository.save(user);
    }

    // ------------------- Distance / Nearby -------------------

    // Haversine formula to calculate distance between two lat/lng in km
    private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // Fetch nearby carpenters within given radius
    public List<User> getNearbyCarpenters(double userLat, double userLng, double radiusKm) {
        List<User> carpenters = userRepository.findByRole("carpenter");
        return carpenters.stream()
                .filter(carpenter -> {
                    try {
                        if (carpenter.getLocation() == null || carpenter.getLocation().isEmpty()) {
                            return false;
                        }
                        String[] parts = carpenter.getLocation().split(",");
                        if (parts.length != 2) return false;
                        double lat = Double.parseDouble(parts[0].trim());
                        double lng = Double.parseDouble(parts[1].trim());
                        double distance = distanceInKm(userLat, userLng, lat, lng);
                        return distance <= radiusKm;
                    } catch (Exception e) {
                        return false; // skip if parsing fails
                    }
                })
                .toList();
    }
}
