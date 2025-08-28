package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    // ✅ Landing page
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("showPayment", false);
        model.addAttribute("showUpload", false);
        model.addAttribute("showLottery", false);
        return "index";  // must exist inside templates/
    }

    // ✅ Step 1: Enter details → show upload section
    @PostMapping("/enter")
    public String enterLottery(@RequestParam("name") String name,
                               @RequestParam("mobile") String mobile,
                               Model model) {
        model.addAttribute("name", name);
        model.addAttribute("mobile", mobile);

        model.addAttribute("showPayment", true);
        model.addAttribute("showUpload", true);
        model.addAttribute("showLottery", false);

        return "index";
    }

    // ✅ Step 2: Redirect to WhatsApp after "upload"
    @PostMapping("/upload")
    public String redirectToWhatsapp(@RequestParam("name") String name,
                                     @RequestParam("mobile") String mobile,
                                     RedirectAttributes redirectAttributes) {

        // ✅ Save payment in DB without screenshot
        Payment payment = new Payment(name, mobile, null);
        payment = paymentRepository.save(payment);

        // ✅ Prepare WhatsApp message
        String message = "🎉 Ganesh Laddu Lottery 🎉\n" +
                         "Name: " + name + "\n" +
                         "Lottery ID: " + payment.getId() + "\n" +
                         "Mobile: " + mobile + "\n\n" +
                         "Please attach your payment screenshot manually.";

        // ✅ Encode message
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        // ✅ WhatsApp URL (send to 8464044204)
        String whatsappUrl = "https://wa.me/918464044204?text=" + encodedMessage;

        // ✅ Pass attributes for showing congratulations card after returning
        redirectAttributes.addFlashAttribute("showLottery", true);
        redirectAttributes.addFlashAttribute("name", name);
        redirectAttributes.addFlashAttribute("mobile", mobile);

        // ✅ Redirect to WhatsApp
        return "redirect:" + whatsappUrl;
    }

    // ✅ Step 3: Admin - view all payments
//     @GetMapping("/all-payments")
//     public String viewAllPayments(Model model) {
//         model.addAttribute("payments", paymentRepository.findAll());
//         return "all-payments";  // matches the template name
//     }
}
