package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
import com.railwayticketsystem.railwayticketsystem.entity.SystemSetting;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.exception.QuotaExceededException;
import com.railwayticketsystem.railwayticketsystem.exception.SeatNotAvailableException;
import com.railwayticketsystem.railwayticketsystem.repository.BookingRepository;
import com.railwayticketsystem.railwayticketsystem.repository.SeatClassRepository;
import com.railwayticketsystem.railwayticketsystem.repository.SystemSettingRepository;
import com.railwayticketsystem.railwayticketsystem.repository.UserRepository;
import com.railwayticketsystem.railwayticketsystem.service.BookingService;
import com.railwayticketsystem.railwayticketsystem.util.QRCodeGenerator;
import com.railwayticketsystem.railwayticketsystem.util.TicketPDFGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatClassRepository seatClassRepository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository; // අලුතින් එකතු කළා
    private final QRCodeGenerator qrCodeGenerator;
    private final TicketPDFGenerator ticketPDFGenerator;

    @Override
    @Transactional
    public Booking bookTicket(BookingRequest request, String nic) {

        User user = userRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int requestedQty = request.getQuantity() > 0 ? request.getQuantity() : 1;

        // ================== Dynamic Quota Check ==================
        int maxQuota = systemSettingRepository.findById("MAX_TICKETS_PER_24H")
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElse(3); // Database එකේ නැත්නම් 3යි

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long bookingCount = bookingRepository.countBookingsInLast24Hours(user.getId(), twentyFourHoursAgo);

        if (bookingCount + requestedQty > maxQuota) {
            throw new QuotaExceededException("You can only book up to " + maxQuota + " tickets in 24 hours. You are trying to book " + requestedQty + " but you already have " + bookingCount + " recent bookings.");
        }

        SeatClass seatClass = seatClassRepository.findById(request.getSeatClassId())
                .orElseThrow(() -> new RuntimeException("Seat class not found"));

        if (seatClass.getAvailableSeats() < requestedQty) {
            throw new SeatNotAvailableException("Only " + seatClass.getAvailableSeats() + " seats available in " + seatClass.getClassType() + " class.");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Booking booking = Booking.builder()
                .transactionId(transactionId)
                .user(user)
                .train(seatClass.getTrain())
                .seatClass(seatClass)
                .quantity(requestedQty)
                .bookingTime(LocalDateTime.now())
                .journeyDate(request.getJourneyDate())
                .status("CONFIRMED")
                .build();

        seatClass.setAvailableSeats(seatClass.getAvailableSeats() - requestedQty);
        seatClassRepository.save(seatClass);

        Booking savedBooking = bookingRepository.save(booking);

        try {
            String qrPath = qrCodeGenerator.generateQR(savedBooking);
            String pdfPath = ticketPDFGenerator.generateTicketPDF(savedBooking, qrPath);
            savedBooking.setPdfPath(pdfPath);
            return bookingRepository.save(savedBooking);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ticket documents", e);
        }
    }
}