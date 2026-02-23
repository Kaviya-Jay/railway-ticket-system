package com.railwayticketsystem.railwayticketsystem.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BookingRequest {
    private Long trainId;
    private Long seatClassId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate journeyDate;
}