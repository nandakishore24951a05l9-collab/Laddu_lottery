package com.example.demo.repository;

import com.example.demo.model.JobRequest;
import com.example.demo.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {

    // keep your existing methods
    List<JobRequest> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
    List<JobRequest> findByTechnicianIdAndStatusOrderByCreatedAtDesc(Long technicianId, JobStatus status);

    // 🔹 NEW: fetch by stable customer key (no phone needed)
    List<JobRequest> findByCustomerKeyOrderByCreatedAtDesc(String customerKey);
}
