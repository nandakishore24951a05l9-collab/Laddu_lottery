package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.CompletedWork;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CompletedWorkRepository;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/completed-works")
@CrossOrigin(origins = "*")
public class CompletedWorkController {

    private final CompletedWorkRepository repo;
    private final UserService userService;
    private final BookingRepository bookingRepo;

    public CompletedWorkController(
            CompletedWorkRepository repo,
            UserService userService,
            BookingRepository bookingRepo
    ) {
        this.repo = repo;
        this.userService = userService;
        this.bookingRepo = bookingRepo;
    }

    // ✅ Save manually (if needed)
    @PostMapping
    public CompletedWork saveWork(@RequestBody CompletedWork work) {
        if (work.getTechnician() != null && work.getTechnician().getEmail() != null) {
            User tech = userService.getUserByEmail(work.getTechnician().getEmail());
            work.setTechnician(tech);
        }
        if (work.getFinishedAt() == null) {
            work.setFinishedAt(Instant.now());
        }
        return repo.save(work);
    }

    // ✅ Create CompletedWork from a booking
    @PostMapping("/from-booking/{bookingId}")
    public ResponseEntity<?> createFromBooking(
            @PathVariable Long bookingId,
            @RequestBody CompletedWork payload) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        CompletedWork work = new CompletedWork();
        work.setTechnician(booking.getTechnician());
        work.setCustomer(booking.getCustomer());
        work.setServiceType(booking.getServiceType());

        work.setSummary(payload.getSummary());
        work.setAmount(payload.getAmount());
        work.setDistanceKm(payload.getDistanceKm());
        work.setDurationMin(payload.getDurationMin());
        work.setStartedAt(payload.getStartedAt() != null ? payload.getStartedAt() : Instant.now());
        work.setFinishedAt(payload.getFinishedAt() != null ? payload.getFinishedAt() : Instant.now());

        repo.save(work);

        return ResponseEntity.ok(work);
    }

    // ✅ FIXED: safer email lookup
    @GetMapping("/tech/{email}")
    public List<CompletedWork> getByTechnician(@PathVariable String email) {
        return repo.findByTechnicianEmail(email);
    }

    // (Optional) Debug: list all completed works
    @GetMapping
    public List<CompletedWork> getAll() {
        return repo.findAll();
    }
}
