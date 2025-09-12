package com.example.demo.dto;

public class CreateBookingRequest {
    private Long technicianId;
    private String serviceType;
    private String mode;               // "I_COME_TO_YOU" etc.
    private String customerLocation;   // "lat,lng" (optional)
    private Double customerLat;        // optional
    private Double customerLng;        // optional
    private String note;

    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getCustomerLocation() { return customerLocation; }
    public void setCustomerLocation(String customerLocation) { this.customerLocation = customerLocation; }

    public Double getCustomerLat() { return customerLat; }
    public void setCustomerLat(Double customerLat) { this.customerLat = customerLat; }

    public Double getCustomerLng() { return customerLng; }
    public void setCustomerLng(Double customerLng) { this.customerLng = customerLng; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
