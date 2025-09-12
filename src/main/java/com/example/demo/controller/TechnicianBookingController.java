package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tech-bookings")
public class TechnicianBookingController {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;

    public TechnicianBookingController(BookingRepository bookingRepo, UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getMyPending(@RequestParam("techId") Long techId) {
        return userRepo.findById(techId)
                .<ResponseEntity<?>>map(tech -> {
                    List<Booking> jobs = bookingRepo.findByTechnicianAndStatusIgnoreCaseOrderByCreatedAtDesc(
                            tech, "PENDING"
                    );
                    return ResponseEntity.ok(jobs);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("Technician not found"));
    }
}
