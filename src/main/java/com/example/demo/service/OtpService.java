// package com.example.demo.service;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.*;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// @Service
// public class OtpService {

//     @Value("${fast2sms.api.key}")
//     private String apiKey;

//     @Value("${fast2sms.api.url}")
//     private String apiUrl;

//     private static final String FIXED_OTP = "749373"; // Fixed OTP

//     public String sendOtp(String phone) {
//         try {
//             RestTemplate restTemplate = new RestTemplate();

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_JSON);
//             headers.set("authorization", apiKey);

//             // POST body for Fast2SMS fixed OTP route
//             String body = "{"
//                     + "\"route\":\"otp\","
//                     + "\"variables_values\":\"" + FIXED_OTP + "\","
//                     + "\"flash\":0,"
//                     + "\"numbers\":\"" + phone + "\""
//                     + "}";

//             HttpEntity<String> entity = new HttpEntity<>(body, headers);

//             ResponseEntity<String> response = restTemplate.exchange(
//                     apiUrl, HttpMethod.POST, entity, String.class
//             );

//             System.out.println("OTP sent to " + phone + ": " + FIXED_OTP + " | Response: " + response.getBody());
//             return response.getBody();

//         } catch (Exception e) {
//             System.err.println("Error sending OTP to " + phone + ": " + e.getMessage());
//             return "error_sending_otp";
//         }
//     }

//     public String getFixedOtp() {
//         return FIXED_OTP;
//     }
// }

package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final String FIXED_OTP = "986610"; // Static OTP

    // Return the fixed OTP
    public String getFixedOtp() {
        return FIXED_OTP;
    }
}

