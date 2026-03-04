package com.railwayticketsystem.railwayticketsystem.service;

public interface OtpService {
    String generateOtp();
    void saveOtp(String email, String otp);
    boolean validateOtp(String email, String otp);
}