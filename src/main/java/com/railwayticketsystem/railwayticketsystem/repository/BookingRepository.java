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

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Booking b WHERE b.user.id = :userId")
    Long countTotalTicketsBooked(@Param("userId") Long userId);


    @Query("SELECT COALESCE(SUM(b.quantity * b.seatClass.price), 0) FROM Booking b WHERE b.user.id = :userId")
    BigDecimal calculateTotalSpent(@Param("userId") Long userId);

    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.bookingTime >= :since")
    long countBookingsInLast24Hours(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    long countByBookingTimeAfter(LocalDateTime date);

    List<Booking> findTop10ByOrderByBookingTimeDesc();
}