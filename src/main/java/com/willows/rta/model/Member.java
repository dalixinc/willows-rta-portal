package com.willows.rta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Flat/Unit number is required")
    @Column(nullable = false)
    private String flatNumber;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, length = 500)
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean isLeaseholder;

    @Column(nullable = false)
    private String preferredCommunication; // EMAIL, PHONE, POST

    @Column(nullable = false)
    private boolean consentGiven = false;

    @Column
    private String signatureData; // Could be typed name or actual signature

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @Column(nullable = false)
    private String membershipStatus; // ACTIVE, SUSPENDED, TERMINATED

    @Column(nullable = false)
    private boolean hasUserAccount = false; // Tracks if login account was created

    @Column
    private String accountCreationMethod; // SELF_REGISTRATION or ADMIN_CREATED

    // Transient fields for displaying user account status in admin views
    @Transient
    private Boolean userEnabled;
    
    @Transient
    private Boolean userAccountLocked;
    
    @Transient
    private Integer userFailedAttempts;

    // Constructors
    public Member() {
        this.registrationDate = LocalDateTime.now();
        this.membershipStatus = "ACTIVE";
        this.hasUserAccount = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isLeaseholder() {
        return isLeaseholder;
    }

    public void setLeaseholder(boolean leaseholder) {
        isLeaseholder = leaseholder;
    }

    public String getPreferredCommunication() {
        return preferredCommunication;
    }

    public void setPreferredCommunication(String preferredCommunication) {
        this.preferredCommunication = preferredCommunication;
    }

    public boolean isConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(boolean consentGiven) {
        this.consentGiven = consentGiven;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public void setSignatureData(String signatureData) {
        this.signatureData = signatureData;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public boolean isHasUserAccount() {
        return hasUserAccount;
    }

    public void setHasUserAccount(boolean hasUserAccount) {
        this.hasUserAccount = hasUserAccount;
    }

    public String getAccountCreationMethod() {
        return accountCreationMethod;
    }

    public void setAccountCreationMethod(String accountCreationMethod) {
        this.accountCreationMethod = accountCreationMethod;
    }

    public Boolean getUserEnabled() {
        return userEnabled;
    }

    public void setUserEnabled(Boolean userEnabled) {
        this.userEnabled = userEnabled;
    }

    public Boolean getUserAccountLocked() {
        return userAccountLocked;
    }

    public void setUserAccountLocked(Boolean userAccountLocked) {
        this.userAccountLocked = userAccountLocked;
    }

    public Integer getUserFailedAttempts() {
        return userFailedAttempts;
    }

    public void setUserFailedAttempts(Integer userFailedAttempts) {
        this.userFailedAttempts = userFailedAttempts;
    }
}
