package com.railwayticketsystem.railwayticketsystem.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_classes")
@Getter
@Setter
@Builder
public class SeatClass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    private String classType; // "1st", "2nd", "3rd"
    private BigDecimal price;
    private int totalSeats;
    private int availableSeats;

    @Version
    private Long version;   // OPTIMISTIC LOCKING
}