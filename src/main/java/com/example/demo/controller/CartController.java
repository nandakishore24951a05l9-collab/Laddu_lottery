package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.model.User;
import com.example.demo.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public CartItem add(@RequestBody CartItem item) { return service.addItem(item); }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) { service.removeItem(id); }

    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return service.getCart(user);
    }
}
