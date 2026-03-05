package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- Admin Dashboard සහ Reports සඳහා අවශ්‍ය Methods ---
    long countByBookingTimeAfter(LocalDateTime time);

    List<Booking> findTop10ByOrderByBookingTimeDesc();

    // --- User Dashboard සඳහා අවශ්‍ය Methods ---
    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Booking b WHERE b.user.id = :userId AND b.bookingTime >= :time")
    long countBookingsInLast24Hours(@Param("userId") Long userId, @Param("time") LocalDateTime time);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Booking b WHERE b.user.id = :userId")
    long countTotalTicketsBooked(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(b.quantity * s.price), 0) FROM Booking b JOIN b.seatClass s WHERE b.user.id = :userId")
    BigDecimal calculateTotalSpent(@Param("userId") Long userId);
}