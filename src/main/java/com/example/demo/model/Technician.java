package com.example.demo.model;

import jakarta.persistence.*;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

@Entity
@Table(name = "technicians")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================
    // Personal info
    // =====================
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // stored hashed with BCrypt

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    // =====================
    // Professional info
    // =====================
    @Column(nullable = false)
    private String profession; // Electrician, plumber, carpenter, etc.

    @Column(length = 500)
    private String specialization; // description/details

    @Column(nullable = false)
    private int experience; // years of experience

    // =====================
    // Location info
    // =====================
    @Column(nullable = false)
    private String location; // Example: "17.385044,78.486671"

    // =====================
    // File Uploads
    // =====================
    // Store just the saved filenames (e.g. "u_42_1736492198012.jpg")
    // We will expose URLs via transient getters (no DB change).
    private String profilePhoto;   // filename saved under /uploads/
    private String workshopPhoto;  // filename saved under /uploads/

    // =====================
    // Constructors
    // =====================
    public Technician() {}

    public Technician(String firstName, String lastName, String email, String password,
                      String phone, String profession, String specialization,
                      int experience, String location, String profilePhoto, String workshopPhoto) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.profession = profession;
        this.specialization = specialization;
        this.experience = experience;
        this.location = location;
        this.profilePhoto = profilePhoto;
        this.workshopPhoto = workshopPhoto;
    }

    // =====================
    // Getters & Setters
    // =====================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getWorkshopPhoto() { return workshopPhoto; }
    public void setWorkshopPhoto(String workshopPhoto) { this.workshopPhoto = workshopPhoto; }

    // =====================
    // Non-persistent helpers (no schema change)
    // =====================

    @Transient
    public String getProfilePhotoUrl() {
        return buildUploadsUrlOrPlaceholder(profilePhoto);
    }

    @Transient
    public String getWorkshopPhotoUrl() {
        return buildUploadsUrlOrPlaceholder(workshopPhoto);
    }

    @Transient
    public Double getLatitude() {
        try {
            String[] parts = (location == null ? "" : location).split(",");
            return parts.length == 2 ? Double.parseDouble(parts[0].trim()) : null;
        } catch (Exception e) { return null; }
    }

    @Transient
    public Double getLongitude() {
        try {
            String[] parts = (location == null ? "" : location).split(",");
            return parts.length == 2 ? Double.parseDouble(parts[1].trim()) : null;
        } catch (Exception e) { return null; }
    }

    private String buildUploadsUrlOrPlaceholder(String filename) {
        if (filename == null || filename.isBlank()) {
            // keep a static placeholder in /img
            return "/img/placeholder-avatar.png";
        }
        try {
            String enc = URLEncoder.encode(filename, StandardCharsets.UTF_8);
            return "/uploads/" + enc;
        } catch (Exception e) {
            return "/img/placeholder-avatar.png";
        }
    }
}
