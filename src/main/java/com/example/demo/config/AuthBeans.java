package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthBeans {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, PasswordEncoder encoder) {
        return username -> {
            // username is email
            User u = userRepository.findByEmail(username);
            if (u == null) throw new UsernameNotFoundException("No user: " + username);

            // IMPORTANT: u.getPassword() must be BCrypt because your SecurityConfig uses BCryptPasswordEncoder
            // IMPORTANT: u.getRole() should be exactly "technician" or "customer"
            return org.springframework.security.core.userdetails.User
                    .withUsername(u.getEmail())
                    .password(u.getPassword())
                    .authorities(u.getRole())
                    .build();
        };
    }
}
