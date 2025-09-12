// package com.example.demo.controller;

// import com.example.demo.service.OtpService;
// import jakarta.servlet.http.HttpSession;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.*;

// @Controller
// @RequestMapping("/otp")
// public class OtpController {

//     @Autowired
//     private OtpService otpService;

//     @PostMapping("/send")
//     @ResponseBody
//     public String sendOtp(@RequestParam String phone, HttpSession session) {
//         try {
//             if (phone == null || !phone.matches("\\d{10}")) {
//                 return "invalid_phone";
//             }

//             // Store OTP in session
//             String otp = otpService.getFixedOtp();
//             session.setAttribute("otp", otp);
//             session.setAttribute("phone", phone);

//             // Send OTP
//             String response = otpService.sendOtp(phone);
//             if (response.equals("error_sending_otp")) {
//                 return "error_sending_otp";
//             }

//             return "success";

//         } catch (Exception e) {
//             e.printStackTrace();
//             return "error";
//         }
//     }

//     @PostMapping("/verify")
//     @ResponseBody
//     public String verifyOtp(@RequestParam String otpInput, @RequestParam String phone, HttpSession session) {
//         String sessionOtp = (String) session.getAttribute("otp");
//         String sessionPhone = (String) session.getAttribute("phone");

//         if (sessionOtp == null || sessionPhone == null) return "expired";
//         if (!sessionPhone.equals(phone)) return "phone_mismatch";

//         return sessionOtp.equals(otpInput) ? "success" : "invalid";
//     }
// }

package com.example.demo.controller;

import com.example.demo.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    // Step 1: "Send" OTP (static, no API)
    @PostMapping("/send")
    @ResponseBody
    public String sendOtp(@RequestParam String phone, HttpSession session) {
        if (phone == null || !phone.matches("\\d{10}")) {
            return "invalid_phone";
        }

        // Store static OTP in session
        String otp = otpService.getFixedOtp();
        session.setAttribute("otp", otp);
        session.setAttribute("phone", phone);

        // Simulate sending OTP
        System.out.println("Static OTP for " + phone + " is " + otp);

        return "success";
    }

    // Step 2: Verify OTP
    @PostMapping("/verify")
    @ResponseBody
    public String verifyOtp(@RequestParam String otpInput, @RequestParam String phone, HttpSession session) {
        String sessionOtp = (String) session.getAttribute("otp");
        String sessionPhone = (String) session.getAttribute("phone");

        if (sessionOtp == null || sessionPhone == null) return "expired";
        if (!sessionPhone.equals(phone)) return "phone_mismatch";

        return sessionOtp.equals(otpInput) ? "success" : "invalid";
    }
}
