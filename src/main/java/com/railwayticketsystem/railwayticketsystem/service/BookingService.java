package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;

public interface BookingService {

    // PayHere Payment Integration සඳහා අලුතින් පරාමිතීන් (transactionId, payherePaymentId, paymentMethod) එකතු කර ඇත
    Booking bookTicket(BookingRequest request, String nic, String transactionId, String payherePaymentId, String paymentMethod);

}