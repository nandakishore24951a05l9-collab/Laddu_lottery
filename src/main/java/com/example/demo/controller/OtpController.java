package com.example.demo.controller;

import com.example.demo.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    // Step 1: Send OTP
    @PostMapping("/send")
    public String sendOtp(@RequestParam String phone, HttpSession session, Model model) {
        try {
            if (phone == null || phone.isEmpty()) {
                model.addAttribute("error", "Phone number is required ❌");
                return "login";
            }

            String otp = otpService.generateOtp();
            session.setAttribute("otp", otp);
            session.setAttribute("phone", phone);

            String response = otpService.sendOtp(phone, otp);
            System.out.println("OTP API Response: " + response);

            model.addAttribute("phone", phone);
            return "verify-otp"; // go to OTP verification page

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error sending OTP ❌: " + e.getMessage());
            return "login";
        }
    }

    // Step 2: Verify OTP and redirect to role selection
    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String otpInput, HttpSession session, Model model) {
        try {
            String otp = (String) session.getAttribute("otp");
            String phone = (String) session.getAttribute("phone");

            if (otp != null && otp.equals(otpInput)) {
                // OTP verified
                session.removeAttribute("otp");
                model.addAttribute("phone", phone);

                // Redirect to role selection page instead of welcome
                return "role-selection";
            } else {
                model.addAttribute("error", "Invalid OTP ❌");
                model.addAttribute("phone", phone);
                return "verify-otp";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error verifying OTP ❌: " + e.getMessage());
            return "verify-otp";
        }
    }
}
