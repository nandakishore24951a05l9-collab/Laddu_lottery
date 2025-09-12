package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private int quantity;
    private double price;

    @ManyToOne
    private User user;

    public CartItem() {}

    public CartItem(String serviceName, int quantity, double price, User user) {
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.price = price;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
