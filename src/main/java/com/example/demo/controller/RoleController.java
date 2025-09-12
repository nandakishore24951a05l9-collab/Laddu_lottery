package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/role")
public class RoleController {

    @GetMapping("/customer")
    public String customerPage() {
        return "customer"; // make sure customer.html exists in templates
    }

    @GetMapping("/technician")
    public String technicianPage() {
        return "technician"; // make sure technician.html exists in templates
    }
}
