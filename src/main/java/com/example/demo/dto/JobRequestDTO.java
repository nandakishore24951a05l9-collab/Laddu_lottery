package com.example.demo.dto;

import com.example.demo.model.JobStatus;
import com.example.demo.model.ServiceMode;

public class JobRequestDTO {
    public Long id;
    public Long technicianId;
    public ServiceMode mode;
    public JobStatus status;
    public String customerName;
    public String customerPhone;
    public String customerAddress;
    public String customerLocation; // "lat,lng"
    public String note;
    public Double deliveryFee;
    public Double quotedPrice;

    public JobRequestDTO() {}
}
