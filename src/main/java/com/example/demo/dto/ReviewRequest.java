package com.example.demo.dto;

public class ReviewRequest {
    private String userName;  // optional; default "Anonymous"
    private int rating;       // 1..5
    private String text;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
