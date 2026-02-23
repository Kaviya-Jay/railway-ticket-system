package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.entity.User;

public interface UserService {
    void registerUser(RegisterRequest request);
    User findByNic(String nic);
}