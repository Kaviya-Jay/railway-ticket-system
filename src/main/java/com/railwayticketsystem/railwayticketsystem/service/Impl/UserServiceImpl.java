package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.repository.UserRepository;
import com.railwayticketsystem.railwayticketsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByNic(request.getNic())) {
            throw new RuntimeException("NIC already registered! Please use a different NIC.");
        }

        User user = User.builder()
                .nic(request.getNic().toUpperCase())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(com.railwayticketsystem.railwayticketsystem.entity.Role.USER)
                .build();

        userRepository.save(user);
    }

    @Override
    public User findByNic(String nic) {
        return userRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("User not found with NIC: " + nic));
    }
}