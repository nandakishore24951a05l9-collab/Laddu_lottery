package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Signup (Register)
    @PostMapping("/signup")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // ✅ Get all technicians
    @GetMapping("/technicians")
    public List<User> getAllTechnicians() {
        return userService.getAllTechnicians();
    }

    // Login
    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        boolean success = userService.login(email, password);
        if (success) {
            // ✅ return full user object instead of just a string
            return userService.getUserByEmail(email);
        } else {
            throw new RuntimeException("Invalid email or password");
        }
    }

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by email
    @GetMapping("/email")
    public User getByEmail(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    // Update profile (including location)
    @PutMapping("/{id}/profile")
    public User updateProfile(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUserProfile(
                id,
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhone(),
                updatedUser.getLocation()
        );
    }

    // ✅ Get nearby carpenters dynamically
    @GetMapping("/nearby-carpenters")
    public List<User> getNearbyCarpenters(
            @RequestParam double userLat,
            @RequestParam double userLng,
            @RequestParam(defaultValue = "15") double radiusKm
    ) {
        return userService.getNearbyCarpenters(userLat, userLng, radiusKm);
    }

    // ✅ NEW: Save/update user location (called from frontend showUserAddress)
    @PostMapping("/save-location")
    public User saveUserLocation(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Double latitude = Double.valueOf(payload.get("latitude").toString());
        Double longitude = Double.valueOf(payload.get("longitude").toString());
        String fullAddress = payload.get("fullAddress").toString();

        return userService.updateUserLocation(userId, latitude, longitude, fullAddress);
    }
}
