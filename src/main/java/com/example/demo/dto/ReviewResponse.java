package com.example.demo.dto;

import java.time.Instant;

public class ReviewResponse {
    public Long id;
    public String author;  // shown as author in UI
    public int rating;
    public String text;
    public Instant createdAt;

    public ReviewResponse(Long id, String author, int rating, String text, Instant createdAt) {
        this.id = id;
        this.author = author;
        this.rating = rating;
        this.text = text;
        this.createdAt = createdAt;
    }
}
