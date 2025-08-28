package com.example.demo;

import jakarta.persistence.*;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = true) // ✅ Now it can be null
    private String screenshotFileName;

    // ✅ Default constructor (needed by JPA)
    public Payment() {}

    // ✅ Constructor
    public Payment(String name, String mobileNumber, String screenshotFileName) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.screenshotFileName = screenshotFileName;
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getScreenshotFileName() {
        return screenshotFileName;
    }
    public void setScreenshotFileName(String screenshotFileName) {
        this.screenshotFileName = screenshotFileName;
    }

  @Override
public String toString() {
    return "Payment{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", mobileNumber='" + mobileNumber + '\'' +
            ", screenshotFileName='" + screenshotFileName + '\'' +
            '}';
}

}
