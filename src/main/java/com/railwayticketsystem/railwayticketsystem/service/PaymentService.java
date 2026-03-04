package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.dto.PaymentRequest;

public interface PaymentService {
    boolean processMockPayment(PaymentRequest req);
}
