package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "completed_works")
public class CompletedWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User technician;

    @ManyToOne
    private User customer;

    private String serviceType;
    private String summary;
    private Double amount;

    private Double distanceKm;
    private Long durationMin;

    private Instant startedAt;
    private Instant finishedAt;

    @Column(length = 2000)
    private String notes;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getTechnician() { return technician; }
    public void setTechnician(User technician) { this.technician = technician; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Long getDurationMin() { return durationMin; }
    public void setDurationMin(Long durationMin) { this.durationMin = durationMin; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
