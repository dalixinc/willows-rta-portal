package com.willows.rta.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@willowsrta.org}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void sendOtpEmail(String toEmail, String otpCode) {
        if (!emailEnabled || mailSender == null) {
            // Email not configured - log the OTP instead
            System.out.println("========================================");
            System.out.println("EMAIL NOT CONFIGURED - OTP CODE:");
            System.out.println("To: " + toEmail);
            System.out.println("OTP Code: " + otpCode);
            System.out.println("This code will expire in 10 minutes");
            System.out.println("========================================");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your Willows RTA Login Code");
            message.setText(buildOtpEmailBody(otpCode));
            
            mailSender.send(message);
            System.out.println("OTP email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            // Fallback: log the OTP
            System.out.println("========================================");
            System.out.println("EMAIL FAILED - OTP CODE:");
            System.out.println("To: " + toEmail);
            System.out.println("OTP Code: " + otpCode);
            System.out.println("========================================");
        }
    }

    public void sendWelcomeEmail(String toEmail, String memberName) {
        if (!emailEnabled || mailSender == null) {
            System.out.println("Welcome email would be sent to: " + toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to The Willows RTA");
            message.setText(buildWelcomeEmailBody(memberName));
            
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    private String buildOtpEmailBody(String otpCode) {
        return "Dear Member,\n\n" +
               "Your verification code for The Willows RTA portal is:\n\n" +
               otpCode + "\n\n" +
               "This code will expire in 10 minutes.\n\n" +
               "If you did not request this code, please ignore this email.\n\n" +
               "Best regards,\n" +
               "The Willows RTA Committee";
    }

    private String buildWelcomeEmailBody(String memberName) {
        return "Dear " + memberName + ",\n\n" +
               "Welcome to The Willows Recognised Tenants' Association!\n\n" +
               "Your registration has been successful. You can now login to the portal.\n\n" +
               "Best regards,\n" +
               "The Willows RTA Committee";
    }

    public boolean isEmailConfigured() {
        return emailEnabled && mailSender != null;
    }
}
