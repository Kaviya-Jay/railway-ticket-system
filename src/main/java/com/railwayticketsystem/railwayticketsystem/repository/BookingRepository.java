package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.bookingTime > :since")
    long countBookingsInLast24Hours(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    Optional<Booking> findByTransactionId(String transactionId);
}