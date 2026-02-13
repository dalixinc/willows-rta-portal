package com.willows.rta.service;

import com.willows.rta.model.OtpCode;
import com.willows.rta.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final ResendEmailService resendEmailService;
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final int OTP_LENGTH = 6;

    @Autowired
    public OtpService(OtpRepository otpRepository, ResendEmailService resendEmailService) {
        this.otpRepository = otpRepository;
        this.resendEmailService = resendEmailService;
    }

    /**
     * Generate and send OTP code to user's email
     */
    @Transactional
    public String generateAndSendOtp(String username, String email) {
        // Invalidate any existing unused OTPs for this user
        invalidateExistingOtps(username);

        // Generate new 6-digit OTP
        String otpCode = generateOtpCode();

        // Save to database
        OtpCode otp = new OtpCode(username, otpCode, OTP_VALIDITY_MINUTES);
        otpRepository.save(otp);

        // Send via email (async)
        resendEmailService.sendOtpEmail(email, otpCode);

        return otpCode; // Return for testing purposes
    }

    /**
     * Validate OTP code
     */
    @Transactional
    public boolean validateOtp(String username, String code) {
        Optional<OtpCode> otpOpt = otpRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc(username);

        if (otpOpt.isEmpty()) {
            return false;
        }

        OtpCode otp = otpOpt.get();

        // Check if code matches and is still valid
        if (otp.getCode().equals(code) && otp.isValid()) {
            // Mark as used
            otp.setUsed(true);
            otpRepository.save(otp);
            return true;
        }

        return false;
    }

    /**
     * Check if user has a valid unused OTP
     */
    public boolean hasValidOtp(String username) {
        Optional<OtpCode> otpOpt = otpRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc(username);
        return otpOpt.isPresent() && otpOpt.get().isValid();
    }

    /**
     * Invalidate all unused OTPs for a user
     */
    @Transactional
    public void invalidateExistingOtps(String username) {
        var otps = otpRepository.findByUsernameAndUsedFalse(username);
        otps.forEach(otp -> otp.setUsed(true));
        otpRepository.saveAll(otps);
    }

    /**
     * Generate random 6-digit OTP code
     */
    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(code);
    }

    /**
     * Clean up expired OTP codes (should be run periodically)
     */
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}
