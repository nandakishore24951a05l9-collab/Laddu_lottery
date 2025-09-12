package com.example.demo.controller;

import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.ReviewResponse;
import com.example.demo.service.TechnicianReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Optional if you call from a different origin; safe to keep:
@CrossOrigin(origins = "*")
@RequestMapping("/technicians/{id}/reviews")
public class TechnicianReviewController {

    private final TechnicianReviewService reviewService;

    public TechnicianReviewController(TechnicianReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> list(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reviewService.list(id));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> add(@PathVariable("id") Long id,
                                              @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(reviewService.add(id, req));
    }
}
