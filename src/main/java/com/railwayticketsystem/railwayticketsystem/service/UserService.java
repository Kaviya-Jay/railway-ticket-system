package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.entity.User;

public interface UserService {
    void initiateRegistration(RegisterRequest request);
    void completeRegistration(RegisterRequest request, String otp);
    User findByNic(String nic);
    void updateProfile(User user, RegisterRequest request);
    void deleteProfile(Long id);
}