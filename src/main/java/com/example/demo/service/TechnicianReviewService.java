package com.example.demo.service;

import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.ReviewResponse;
import com.example.demo.model.Review;
import com.example.demo.model.Technician;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.TechnicianRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianReviewService {

    private final TechnicianRepository technicianRepository;
    private final ReviewRepository reviewRepository;

    public TechnicianReviewService(TechnicianRepository technicianRepository,
                                   ReviewRepository reviewRepository) {
        this.technicianRepository = technicianRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewResponse> list(Long techId) {
        return reviewRepository.findByTechnician_IdOrderByCreatedAtDesc(techId)
                .stream()
                .map(r -> new ReviewResponse(
                        r.getId(),
                        r.getUserName(),
                        r.getRating(),
                        r.getText(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public ReviewResponse add(Long techId, ReviewRequest req) {
        Technician tech = technicianRepository.findById(techId)
                .orElseThrow(() -> new IllegalArgumentException("Technician not found: " + techId));

        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        String author = (req.getUserName() == null || req.getUserName().isBlank())
                ? "Anonymous" : req.getUserName().trim();

        Review r = new Review();
        r.setTechnician(tech);
        r.setUserName(author);
        r.setRating(req.getRating());
        r.setText(req.getText() == null ? "" : req.getText().trim());

        Review saved = reviewRepository.save(r);
        return new ReviewResponse(saved.getId(), saved.getUserName(),
                saved.getRating(), saved.getText(), saved.getCreatedAt());
    }
}
