package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmail(String email);
    void deleteByEmail(String email);
}