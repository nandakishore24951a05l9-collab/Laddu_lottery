package com.example.demo.repository;

import com.example.demo.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    // Search by profession (case-insensitive)
    List<Technician> findByProfessionContainingIgnoreCase(String profession);

    // Search by specialization (case-insensitive)
    List<Technician> findBySpecializationContainingIgnoreCase(String specialization);

    // Search by location (case-insensitive)
    List<Technician> findByLocationContainingIgnoreCase(String location);

    // Find technician by exact phone number (for OTP login)
    Optional<Technician> findByPhone(String phone);

    // Find technician by email (for signup check / dashboard lookup)
    Optional<Technician> findByEmail(String email);

    // Find by firstName and lastName
    List<Technician> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName
    );
}
