package com.willows.rta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/register", "/constitution", "/css/**", "/js/**", "/documents/**").permitAll()
                // H2 Console (for development/data export)
                .requestMatchers("/h2-console/**").permitAll()
                // Login and OTP authentication endpoints - must be public
                .requestMatchers("/login", "/login-with-otp", "/verify-otp", "/resend-otp").permitAll()
                // Admin-only pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Member pages (both admin and members can access)
                .requestMatchers("/member/**").hasAnyRole("ADMIN", "MEMBER")
                // All other pages require authentication
                .anyRequest().authenticated()
            )
            // Disable form login - we're using custom OTP authentication
            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/login-with-otp", "/verify-otp", "/resend-otp", "/h2-console/**")
            )
            // Allow H2 Console to be displayed in frames (it uses iframes)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
