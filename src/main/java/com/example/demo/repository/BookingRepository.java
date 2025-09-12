package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 🔹 Original methods (kept)
    List<Booking> findByStatus(String status);

    List<Booking> findByTechnicianAndStatus(User technician, String status);

    List<Booking> findByCustomer(User customer);

    // 🔹 New methods for better filtering
    // Case-insensitive + newest first
    List<Booking> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    // Technician + status filter, case-insensitive + newest first
    List<Booking> findByTechnicianAndStatusIgnoreCaseOrderByCreatedAtDesc(
            User technician,
            String status
    );
}
