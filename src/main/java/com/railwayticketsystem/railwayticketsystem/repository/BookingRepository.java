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

    // UserController එකට අවශ්‍ය වූ මෙතඩ් 1 (පරිශීලකයාගේ මුළු ටිකට් ගණන ලබා ගැනීම)
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Booking b WHERE b.user.id = :userId")
    Long countTotalTicketsBooked(@Param("userId") Long userId);

    // UserController එකට අවශ්‍ය වූ මෙතඩ් 2 (පරිශීලකයා වියදම් කළ මුළු මුදල ලබා ගැනීම)
    @Query("SELECT COALESCE(SUM(b.quantity * b.seatClass.price), 0) FROM Booking b WHERE b.user.id = :userId")
    BigDecimal calculateTotalSpent(@Param("userId") Long userId);

    // යම් User කෙනෙකුගේ සියලුම Bookings ලබාගැනීමට
    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);

    // පැය 24 ඇතුළත සිදුකර ඇති Bookings ගණන ලබාගැනීමේ අලුත් Query එක (Quota එක සඳහා)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.bookingTime >= :since")
    long countBookingsInLast24Hours(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Admin Dashboard එකේ අද දවසේ Bookings ගණන පෙන්වීමට
    long countByBookingTimeAfter(LocalDateTime date);

    // Admin Dashboard එකේ අවසන් Bookings 10 පෙන්වීමට
    List<Booking> findTop10ByOrderByBookingTimeDesc();
}