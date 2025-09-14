package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
public class OtpService {

    @Value("${fast2sms.api.key}")
    private String apiKey;

    private static final String API_URL = "https://www.fast2sms.com/dev/bulkV2";

    // Generate 6-digit OTP
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Send OTP via Fast2SMS
    public String sendOtp(String phone, String otp) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", apiKey);

            String body = "{"
                    + "\"route\":\"q\","
                    + "\"message\":\"Your OTP is " + otp + "\","
                    + "\"flash\":0,"
                    + "\"numbers\":\"" + phone + "\""
                    + "}";

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, entity, String.class
            );

            return response.getBody();
        } catch (Exception e) {
            return "Error sending OTP: " + e.getMessage();
        }
    }
}
