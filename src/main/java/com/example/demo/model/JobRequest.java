package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_requests")
public class JobRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which technician is requested
    @Column(nullable = false)
    private Long technicianId;

    // How the service will happen
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    // 🔹 Stable browser identity (stored in cookie/localStorage)
    @Column(nullable = false, length = 72)
    private String customerKey;

    // Customer info
    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, length = 15)
    private String customerPhone;

    @Column(nullable = false, length = 500)
    private String customerAddress;

    // "lat,lng" (same format you already use)
    @Column(nullable = false)
    private String customerLocation;

    @Column(length = 500)
    private String note;

    // Simple pricing placeholders
    private Double deliveryFee;  // only for I_COME_TO_YOU
    private Double quotedPrice;  // optional (future)

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() { this.updatedAt = Instant.now(); }

    // ===== Getters & Setters =====
    public Long getId() { return id; }

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    public ServiceMode getMode() { return mode; }
    public void setMode(ServiceMode mode) { this.mode = mode; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public String getCustomerKey() { return customerKey; }
    public void setCustomerKey(String customerKey) { this.customerKey = customerKey; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public String getCustomerLocation() { return customerLocation; }
    public void setCustomerLocation(String customerLocation) { this.customerLocation = customerLocation; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Double getQuotedPrice() { return quotedPrice; }
    public void setQuotedPrice(Double quotedPrice) { this.quotedPrice = quotedPrice; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
}
