package com.example.demo.service;

import com.example.demo.model.CartItem;
import com.example.demo.model.User;
import com.example.demo.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private final CartRepository repo;

    public CartService(CartRepository repo) {
        this.repo = repo;
    }

    public List<CartItem> getCart(User user) { return repo.findByUser(user); }
    public CartItem addItem(CartItem item) { return repo.save(item); }
    public void removeItem(Long id) { repo.deleteById(id); }
}
