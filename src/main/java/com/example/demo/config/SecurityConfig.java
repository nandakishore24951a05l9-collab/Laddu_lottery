package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;

// ✅ CORS imports
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // will use the corsConfigurationSource() bean below
            .authorizeHttpRequests(auth -> auth
                // Dev tools / static
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                // Public APIs your front-end actually calls
                .requestMatchers("/users/signup", "/users/login", "/users/email").permitAll()
                .requestMatchers("/users/**").permitAll() // keep open if you rely on JSON login responses
                .requestMatchers("/technicians/**", "/api/technicians/**").permitAll()
                .requestMatchers("/api/bookings/**").permitAll()
                .requestMatchers("/api/save-user-location").permitAll()

                // Public pages
                .requestMatchers("/login-user", "/customer-login", "/technician-login", "/login").permitAll()
                .requestMatchers("/auth/**").permitAll()

                // Technician area (either authority spelling is accepted)
                .requestMatchers("/technician", "/technician/**")
                    .hasAnyAuthority("ROLE_TECHNICIAN", "technician")

                // Everything else
                .anyRequest().permitAll()
            )
            .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
            .formLogin(form -> form
                .loginPage("/login-user")
                .loginProcessingUrl("/login")
                .successHandler(roleRedirectHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login-user")
                .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Matches your DB hashes that start with $2a$10$…
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Redirect based on authority after Spring form login (/login)
    @Bean
    public AuthenticationSuccessHandler roleRedirectHandler() {
        return (jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                Authentication authentication) -> {
            boolean isTech = authentication.getAuthorities().stream().anyMatch(a ->
                    "ROLE_TECHNICIAN".equalsIgnoreCase(a.getAuthority()) ||
                    "technician".equalsIgnoreCase(a.getAuthority())
            );
            response.sendRedirect(isTech ? "/technician" : "/customer-details");
        };
    }

    // CORS for local dev (allow cookies)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(java.util.List.of(
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
        ));
        c.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(java.util.List.of("Content-Type", "Accept", "Authorization", "X-Requested-With"));
        c.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
