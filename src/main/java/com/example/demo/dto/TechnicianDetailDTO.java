package com.example.demo.dto;

public class TechnicianDetailDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String profession;
    private String specialization;
    private int experience;
    private String location;

    // Existing fields remain (can still hold raw filenames if you already used them)
    private String profilePhoto;
    private String workshopPhoto;

    // New: full URLs ready for <img src="">
    private String profilePhotoUrl;
    private String workshopPhotoUrl;

    public TechnicianDetailDTO() {}  // required by Jackson

    public TechnicianDetailDTO(Long id, String firstName, String lastName, String phone, String email,
                               String profession, String specialization, int experience,
                               String location, String profilePhoto, String workshopPhoto) {
        this.id = id; this.firstName = firstName; this.lastName = lastName;
        this.phone = phone; this.email = email; this.profession = profession;
        this.specialization = specialization; this.experience = experience;
        this.location = location; this.profilePhoto = profilePhoto; this.workshopPhoto = workshopPhoto;
    }

    // Optional convenience ctor including URLs
    public TechnicianDetailDTO(Long id, String firstName, String lastName, String phone, String email,
                               String profession, String specialization, int experience,
                               String location, String profilePhoto, String workshopPhoto,
                               String profilePhotoUrl, String workshopPhotoUrl) {
        this(id, firstName, lastName, phone, email, profession, specialization, experience,
             location, profilePhoto, workshopPhoto);
        this.profilePhotoUrl = profilePhotoUrl;
        this.workshopPhotoUrl = workshopPhotoUrl;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; } public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; } public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getProfession() { return profession; } public void setProfession(String profession) { this.profession = profession; }
    public String getSpecialization() { return specialization; } public void setSpecialization(String specialization) { this.specialization = specialization; }
    public int getExperience() { return experience; } public void setExperience(int experience) { this.experience = experience; }
    public String getLocation() { return location; } public void setLocation(String location) { this.location = location; }

    public String getProfilePhoto() { return profilePhoto; } public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public String getWorkshopPhoto() { return workshopPhoto; } public void setWorkshopPhoto(String workshopPhoto) { this.workshopPhoto = workshopPhoto; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getWorkshopPhotoUrl() { return workshopPhotoUrl; }
    public void setWorkshopPhotoUrl(String workshopPhotoUrl) { this.workshopPhotoUrl = workshopPhotoUrl; }
}
