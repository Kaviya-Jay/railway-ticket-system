package com.railwayticketsystem.railwayticketsystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentRequest {
    private Long trainId;
    private Long seatClassId;
    private LocalDate journeyDate;
    private int quantity;
    private String cardNumber;
    private String expiry;
    private String cvv;
}