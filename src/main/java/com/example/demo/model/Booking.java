package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer who created booking
    @ManyToOne(optional = false)
    private User customer;

    // Technician who accepts
    @ManyToOne
    private User technician;

    // PENDING, ACCEPTED, REJECTED, COMPLETED
    @Column(nullable = false)
    private String status = "PENDING";

    private String serviceType;

    private Double customerLat;
    private Double customerLng;

    private Instant createdAt = Instant.now();

    // ✅ NEW: customer-written description
    @Column(length = 2000)
    private String description;

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public User getTechnician() { return technician; }
    public void setTechnician(User technician) { this.technician = technician; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Double getCustomerLat() { return customerLat; }
    public void setCustomerLat(Double customerLat) { this.customerLat = customerLat; }

    public Double getCustomerLng() { return customerLng; }
    public void setCustomerLng(Double customerLng) { this.customerLng = customerLng; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ✅ Getter/Setter for description
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
