package com.example.demo.service;

import com.example.demo.dto.JobRequestDTO;
import com.example.demo.model.Booking;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;

    public BookingService(BookingRepository bookingRepo, UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
    }

    // Create booking linked to a chosen technician
    public Booking createBooking(JobRequestDTO dto, User customer) {
        Booking b = new Booking();
        b.setCustomer(customer);
        b.setServiceType(dto.mode != null ? dto.mode.name() : "Carpenter");

        if (dto.customerLocation != null && dto.customerLocation.contains(",")) {
            try {
                String[] parts = dto.customerLocation.split(",");
                b.setCustomerLat(Double.parseDouble(parts[0].trim()));
                b.setCustomerLng(Double.parseDouble(parts[1].trim()));
            } catch (Exception ignored) {}
        }

        b.setStatus("PENDING");

        // ✅ Assign technician
        if (dto.technicianId != null) {
            Optional<User> tech = userRepo.findById(dto.technicianId);
            tech.ifPresent(b::setTechnician);
        }

        return bookingRepo.save(b);
    }
}
