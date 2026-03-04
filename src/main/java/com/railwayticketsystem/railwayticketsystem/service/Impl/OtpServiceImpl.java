package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.entity.Otp;
import com.railwayticketsystem.railwayticketsystem.repository.OtpRepository;
import com.railwayticketsystem.railwayticketsystem.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final int EXPIRATION_MINUTES = 5;

    @Override
    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    @Override
    public void saveOtp(String email, String otp) {
        otpRepository.deleteByEmail(email);  // Delete old OTP
        Otp newOtp = Otp.builder()
                .email(email)
                .otpCode(otp)
                .expirationTime(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();
        otpRepository.save(newOtp);
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        Optional<Otp> savedOtp = otpRepository.findByEmail(email);
        if (savedOtp.isPresent() && savedOtp.get().getOtpCode().equals(otp) && LocalDateTime.now().isBefore(savedOtp.get().getExpirationTime())) {
            otpRepository.deleteByEmail(email);  // Delete after validation
            return true;
        }
        return false;
    }
}
