package com.willows.rta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResendEmailService {

    @Value("${app.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Async("taskExecutor")
    public void sendOtpEmail(String toEmail, String otpCode) {
        // ALWAYS log OTP to console (for debugging and fallback)
        System.out.println("========================================");
        System.out.println("OTP CODE GENERATED:");
        System.out.println("To: " + toEmail);
        System.out.println("OTP Code: " + otpCode);
        System.out.println("Expires in: 10 minutes");
        System.out.println("========================================");

        if (!emailEnabled || resendApiKey == null || resendApiKey.isEmpty()) {
            System.out.println("Resend not configured - OTP logged above");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            // Build email body
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("from", fromEmail);
            emailData.put("to", new String[]{toEmail});
            emailData.put("subject", "Your Willows RTA Login Code");
            emailData.put("html", buildOtpEmailHtml(otpCode));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            // Send email via Resend API
            restTemplate.exchange(RESEND_API_URL, HttpMethod.POST, request, String.class);
            
            System.out.println("✅ OTP email sent successfully via Resend to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email via Resend to " + toEmail + ": " + e.getMessage());
            System.out.println("⚠️  Email failed but OTP is logged above - you can still use it!");
        }
    }

    @Async("taskExecutor")
    public void sendWelcomeEmail(String toEmail, String memberName) {
        if (!emailEnabled || resendApiKey == null || resendApiKey.isEmpty()) {
            System.out.println("Welcome email would be sent to: " + toEmail);
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + resendApiKey);

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("from", fromEmail);
            emailData.put("to", new String[]{toEmail});
            emailData.put("subject", "Welcome to The Willows RTA");
            emailData.put("html", buildWelcomeEmailHtml(memberName));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            restTemplate.exchange(RESEND_API_URL, HttpMethod.POST, request, String.class);
            
            System.out.println("✅ Welcome email sent via Resend to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email via Resend: " + e.getMessage());
        }
    }

    private String buildOtpEmailHtml(String otpCode) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<h2 style='color: #2c5f2d;'>🏘️ The Willows RTA</h2>" +
               "<p>Dear Member,</p>" +
               "<p>Your verification code for The Willows RTA portal is:</p>" +
               "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;'>" +
               otpCode +
               "</div>" +
               "<p>This code will expire in <strong>10 minutes</strong>.</p>" +
               "<p>If you did not request this code, please ignore this email.</p>" +
               "<p style='margin-top: 30px;'>Best regards,<br>The Willows RTA Committee</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    private String buildWelcomeEmailHtml(String memberName) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><meta charset='UTF-8'></head>" +
               "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
               "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
               "<h2 style='color: #2c5f2d;'>🏘️ The Willows RTA</h2>" +
               "<p>Dear " + memberName + ",</p>" +
               "<p>Welcome to The Willows Recognised Tenants' Association!</p>" +
               "<p>Your registration has been successful. You can now login to the portal.</p>" +
               "<p style='margin-top: 30px;'>Best regards,<br>The Willows RTA Committee</p>" +
               "</div>" +
               "</body>" +
               "</html>";
    }

    public boolean isEmailConfigured() {
        return emailEnabled && resendApiKey != null && !resendApiKey.isEmpty();
    }
}
