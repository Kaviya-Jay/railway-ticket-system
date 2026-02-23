package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.exception.QuotaExceededException;
import com.railwayticketsystem.railwayticketsystem.exception.SeatNotAvailableException;
import com.railwayticketsystem.railwayticketsystem.repository.BookingRepository;
import com.railwayticketsystem.railwayticketsystem.repository.SeatClassRepository;
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
    private final QRCodeGenerator qrCodeGenerator;
    private final TicketPDFGenerator ticketPDFGenerator;

    @Override
    @Transactional
    public Booking bookTicket(BookingRequest request, String nic) {

        User user = userRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ================== CRITICAL: Rolling 24-Hour Quota Check ==================
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long bookingCount = bookingRepository.countBookingsInLast24Hours(user.getId(), twentyFourHoursAgo);

        if (bookingCount >= 3) {
            throw new QuotaExceededException("You have already booked 3 tickets in the last 24 hours (NIC-based quota).");
        }

        // ================== Find Seat Class ==================
        SeatClass seatClass = seatClassRepository.findById(request.getSeatClassId())
                .orElseThrow(() -> new RuntimeException("Seat class not found"));

        if (seatClass.getAvailableSeats() <= 0) {
            throw new SeatNotAvailableException("No seats available in " + seatClass.getClassType() + " class.");
        }

        // ================== Generate Transaction ID ==================
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // ================== Create Booking ==================
        Booking booking = Booking.builder()
                .transactionId(transactionId)
                .user(user)
                .train(seatClass.getTrain())
                .seatClass(seatClass)
                .bookingTime(LocalDateTime.now())
                .journeyDate(request.getJourneyDate())
                .status("CONFIRMED")
                .build();

        // ================== Reduce Seat (Optimistic Locking) ==================
        seatClass.setAvailableSeats(seatClass.getAvailableSeats() - 1);
        seatClassRepository.save(seatClass);   // @Version will prevent double booking

        Booking savedBooking = bookingRepository.save(booking);

        // ================== Generate QR & PDF ==================
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