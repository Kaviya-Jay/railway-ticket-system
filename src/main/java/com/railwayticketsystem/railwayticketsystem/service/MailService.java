package com.railwayticketsystem.railwayticketsystem.service;

public interface MailService {
    void sendOtpEmail(String email, String otp);
}