package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.dto.PaymentRequest;
import com.railwayticketsystem.railwayticketsystem.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public boolean processMockPayment(PaymentRequest req) {
        // Mock logic: Always succeed if CVV is 123 (for testing)
        return "123".equals(req.getCvv());
    }
}
