package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.entity.Role;
import com.railwayticketsystem.railwayticketsystem.repository.UserRepository;
import com.railwayticketsystem.railwayticketsystem.service.MailService;
import com.railwayticketsystem.railwayticketsystem.service.OtpService;
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
    private final OtpService otpService;
    private final MailService mailService;

    @Override
    @Transactional
    public void initiateRegistration(RegisterRequest request) {
        if (userRepository.existsByNic(request.getNic())) {
            throw new RuntimeException("NIC already registered!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        String otp = otpService.generateOtp();
        otpService.saveOtp(request.getEmail(), otp);
        mailService.sendOtpEmail(request.getEmail(), otp);

        // Store temp user data in session or cache (for simplicity, assume session in controller)
    }

    @Override
    @Transactional
    public void completeRegistration(RegisterRequest request, String otp) {
        if (!otpService.validateOtp(request.getEmail(), otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = User.builder()
                .nic(request.getNic().toUpperCase())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Override
    public User findByNic(String nic) {
        return userRepository.findByNic(nic).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // NEW: Profile Update
    @Override
    @Transactional
    public void updateProfile(User user, RegisterRequest request) {
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
    }

    // NEW: Delete Profile
    @Override
    @Transactional
    public void deleteProfile(Long id) {
        userRepository.deleteById(id);
    }
}