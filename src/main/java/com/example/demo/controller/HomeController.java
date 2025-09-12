// src/main/java/com/example/demo/controller/HomeController.java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    // 1️⃣ Default home page
    @GetMapping("/")
    public String home() {
        return "home"; // home.html
    }

    // 2️⃣ Role selection page
    @GetMapping("/role-selection")
    public String roleSelection() {
        return "role-selection"; // role-selection.html
    }

    // ✅ NEW: Track Job page
    @GetMapping("/track-job")
    public String trackJob() {
        return "track-job"; // track-job.html
    }

    // 3️⃣ Login page for general users
    @GetMapping("/login-user")
    public String loginUser() {
        return "login-user"; // login-user.html
    }

    // ✅ alias so /login works
    @GetMapping("/login")
    public String loginAlias() {
        return "login-user"; // reuse your existing template
    }

    // 4️⃣ Customer login page
    @GetMapping("/customer-login")
    public String customerLogin() {
        return "login"; // login.html
    }

    // 5️⃣ Customer OTP verification page
    @GetMapping("/customer-otp")
    public String customerOtp() {
        return "otp-verify"; // otp-verify.html
    }

    // 6️⃣ Customer details page
    @GetMapping("/customer-details")
    public String customerDetails() {
        return "customer"; // customer.html
    }

    // 7️⃣ Technician login page
    @GetMapping("/technician-login")
    public String technicianLogin() {
        return "login-technician"; // login-technician.html
    }

    // 8️⃣ Technician dashboard page
    @GetMapping("/technician")
    public String technicianDashboard() {
        return "technician"; // technician.html
    }

    // 9️⃣ Carpenter page
    @GetMapping("/carpenter")
    public String carpenterPage() {
        return "carpenter"; // carpenter.html
    }

    // ✅ NEW: Plumber page
    @GetMapping("/plumber")
    public String plumberPage() {
        return "plumber"; // plumber.html
    }

    // ✅ NEW: Mechanic page
    @GetMapping("/mechanic")
    public String mechanicPage() {
        return "mechanic"; // mechanic.html
    }

    // ✅ NEW: Electrician page
    @GetMapping("/electrician")
    public String electricianPage() {
        return "electrician"; // electrician.html
    }

    // ✅ NEW: Painter page
    @GetMapping("/painter")
    public String painterPage() {
        return "painter"; // painter.html
    }

    // ✅ NEW: Lawn Mowing page
    @GetMapping("/lawnmowing")
    public String lawnmowingPage() {
        return "lawnmowing"; // lawnmowing.html
    }

    // ✅ NEW: Mason page
    @GetMapping("/mason")
    public String masonPage() {
        return "mason"; // mason.html
    }

    // ✅ NEW: Cleaner page
    @GetMapping("/cleaner")
    public String cleanerPage() {
        return "cleaner"; // cleaner.html
    }

    // 🔟 Worker Details page
    @GetMapping({"/worker-details", "/worker-details.html"})
    public String workerDetails(
            @RequestParam(name = "id", required = false) Long id,
            Model model) {
        model.addAttribute("id", id);
        return "worker-details"; // worker-details.html
    }

    // 1️⃣1️⃣ My Requests page
    @GetMapping("/my-requests")
    public String myRequests() {
        return "my-requests"; // my-requests.html
    }

    // 1️⃣2️⃣ Tracking page (old one)
    @GetMapping("/track")
    public String trackPage() {
        return "track"; // track.html
    }

    // 1️⃣3️⃣ Locate Technician page
    @GetMapping({"/locate-technician", "/locate-technician.html"})
    public String locateTechnician(
            @RequestParam(name = "id", required = false) Long id,
            Model model) {
        model.addAttribute("id", id);
        return "locate-technician"; // locate-technician.html
    }
}
