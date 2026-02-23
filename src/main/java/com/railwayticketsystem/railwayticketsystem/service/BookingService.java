package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;

public interface BookingService {
    Booking bookTicket(BookingRequest request, String nic);
}