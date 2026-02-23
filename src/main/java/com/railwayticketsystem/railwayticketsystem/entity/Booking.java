package com.railwayticketsystem.railwayticketsystem.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    @ManyToOne
    private User user;

    @ManyToOne
    private Train train;

    @ManyToOne
    private SeatClass seatClass;

    private LocalDateTime bookingTime;
    private LocalDate journeyDate;
    private String status = "CONFIRMED";

    private String pdfPath;   // path to stored ticket PDF
}