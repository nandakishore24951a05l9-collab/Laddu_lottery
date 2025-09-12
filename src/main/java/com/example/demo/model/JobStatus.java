package com.example.demo.model;

public enum JobStatus {
    PENDING,        // customer sent request, waiting for technician
    ACCEPTED,       // technician accepted
    REJECTED,       // technician rejected
    IN_PROGRESS,    // technician started the work / on the way
    COMPLETED,      // work finished
    CANCELLED       // cancelled by customer/technician
}
