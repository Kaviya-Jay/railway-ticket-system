package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * CRITICAL: Rolling 24-hour quota check (used in BookingService)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.bookingTime > :since")
    long countBookingsInLast24Hours(@Param("userId") Long userId,
                                    @Param("since") LocalDateTime since);

    Optional<Booking> findByTransactionId(String transactionId);


    List<Booking> findTop5ByUserOrderByBookingTimeDesc(User user);

    /**
     * Admin Reports & Dashboard
     */
    long countByBookingTimeAfter(LocalDateTime dateTime);

    List<Booking> findTop10ByOrderByBookingTimeDesc();


    // Optional: revenue calculation (uncomment when you add price to Booking or join)
    // @Query("SELECT SUM(sc.price) FROM Booking b JOIN b.seatClass sc WHERE b.bookingTime BETWEEN :start AND :end")
    // BigDecimal calculateRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

