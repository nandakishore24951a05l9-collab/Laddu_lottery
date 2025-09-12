package com.example.demo.repository;

import com.example.demo.model.CompletedWork;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletedWorkRepository extends JpaRepository<CompletedWork, Long> {
    List<CompletedWork> findByTechnician(User technician);
    List<CompletedWork> findByTechnicianEmail(String email); // ✅ NEW
}
