package com.willows.rta.controller;

import com.willows.rta.model.User;
import com.willows.rta.service.OtpService;
import com.willows.rta.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.mfa.enabled:true}")
    private boolean mfaEnabled;

    @Autowired
    public AuthController(UserService userService, OtpService otpService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    // Login page
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("mfaEnabled", mfaEnabled);
        return "login";
    }

    @PostMapping("/login-with-otp")
    public String loginWithOtp(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        // Check if account is locked due to failed login attempts
        if (userService.isAccountLocked(username)) {
            redirectAttributes.addFlashAttribute("error", 
                "Account temporarily locked due to multiple failed login attempts. Please try again in 15 minutes or contact an administrator.");
            return "redirect:/login";
        }

        // Validate username and password first
        Optional<User> userOpt = userService.getUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            // Don't reveal if username exists - just record attempt
            userService.recordFailedLoginAttempt(username);
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }

        User user = userOpt.get();

        // Check if account is locked by admin
        if (!user.isEnabled()) {
            redirectAttributes.addFlashAttribute("error", "Your account has been locked. Please contact an administrator.");
            return "redirect:/login";
        }

        // Check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Record failed login attempt
            userService.recordFailedLoginAttempt(username);
            
            // Refresh user to get updated failed attempts count and lock status
            user = userService.getUserByUsername(username).get();
            
            // Check if account just got locked
            if (user.isAccountLocked()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Account locked due to multiple failed login attempts. Please try again in 15 minutes or contact an administrator.");
                return "redirect:/login";
            }
            
            int remainingAttempts = 5 - user.getFailedLoginAttempts();
            if (remainingAttempts > 0 && remainingAttempts <= 3) {
                redirectAttributes.addFlashAttribute("error", 
                    "Invalid username or password. " + remainingAttempts + " attempts remaining before account lock.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            }
            return "redirect:/login";
        }

        // Password correct - reset failed login attempts
        userService.resetFailedLoginAttempts(username);

        // If MFA is disabled, login directly
        if (!mfaEnabled) {
            System.out.println("MFA is disabled - logging in directly");
            
            // Create authentication token
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                );
            
            authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set authentication in security context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            
            // Save to session
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            
            // Check if password change is required
            if (user.isPasswordChangeRequired()) {
                return "redirect:/member/change-password?required=true";
            }
            
            return "redirect:/dashboard";
        }

        // MFA is enabled - proceed with OTP
        String email = user.getMember() != null ? user.getMember().getEmail() : username;
        otpService.generateAndSendOtp(username, email);

        // Store username in session for OTP verification
        session.setAttribute("otp_username", username);
        session.setAttribute("otp_timestamp", System.currentTimeMillis());

        redirectAttributes.addFlashAttribute("successMessage", 
            "A verification code has been sent to your email: " + maskEmail(email));
        
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String showOtpPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("otp_username");
        
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        // Check session timeout (10 minutes)
        Long timestamp = (Long) session.getAttribute("otp_timestamp");
        if (timestamp != null && (System.currentTimeMillis() - timestamp) > 600000) {
            session.removeAttribute("otp_username");
            session.removeAttribute("otp_timestamp");
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        model.addAttribute("username", username);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otpCode,
                           HttpSession session,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        
        String username = (String) session.getAttribute("otp_username");
        
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        System.out.println("Verifying OTP for user: " + username);
        System.out.println("OTP Code entered: " + otpCode);

        // Validate OTP
        if (otpService.validateOtp(username, otpCode)) {
            System.out.println("OTP validated successfully!");
            
            // OTP valid - complete login
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                System.out.println("User found: " + user.getUsername() + ", Role: " + user.getRole());
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                    );
                
                authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in security context
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                
                // Save to session
                session.setAttribute("SPRING_SECURITY_CONTEXT", context);
                
                System.out.println("Authentication set in SecurityContext");
                System.out.println("Authenticated: " + authentication.isAuthenticated());
                
                // Clear OTP session data
                session.removeAttribute("otp_username");
                session.removeAttribute("otp_timestamp");
                
                // Check if password change is required
                if (user.isPasswordChangeRequired()) {
                    return "redirect:/member/change-password?required=true";
                }
                
                // Redirect to dashboard
                return "redirect:/dashboard";
            } else {
                System.out.println("User not found!");
            }
        } else {
            System.out.println("OTP validation failed!");
        }

        // OTP invalid
        redirectAttributes.addFlashAttribute("error", "Invalid or expired verification code. Please try again.");
        return "redirect:/verify-otp";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("otp_username");
        
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String email = user.getMember() != null ? user.getMember().getEmail() : username;
            
            otpService.generateAndSendOtp(username, email);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "A new verification code has been sent to " + maskEmail(email));
        }

        return "redirect:/verify-otp";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        
        return local.substring(0, 2) + "***@" + domain;
    }
}
