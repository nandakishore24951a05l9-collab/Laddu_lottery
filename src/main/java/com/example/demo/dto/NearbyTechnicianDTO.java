package com.example.demo.dto;

public class NearbyTechnicianDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String profession;
    private String location;   // "lat,lng"
    private double distanceKm;

    // New: avatar URL for cards in carpenter.html
    private String photoUrl;

    public NearbyTechnicianDTO() {}  // required by Jackson

    public NearbyTechnicianDTO(Long id, String firstName, String lastName, String phone,
                               String email, String profession, String location, double distanceKm) {
        this.id = id; this.firstName = firstName; this.lastName = lastName;
        this.phone = phone; this.email = email; this.profession = profession;
        this.location = location; this.distanceKm = distanceKm;
    }

    // Optional convenience ctor with photoUrl
    public NearbyTechnicianDTO(Long id, String firstName, String lastName, String phone,
                               String email, String profession, String location,
                               double distanceKm, String photoUrl) {
        this(id, firstName, lastName, phone, email, profession, location, distanceKm);
        this.photoUrl = photoUrl;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; } public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; } public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getProfession() { return profession; } public void setProfession(String profession) { this.profession = profession; }
    public String getLocation() { return location; } public void setLocation(String location) { this.location = location; }
    public double getDistanceKm() { return distanceKm; } public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
