package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
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

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatClassRepository seatClassRepository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final QRCodeGenerator qrCodeGenerator;
    private final TicketPDFGenerator ticketPDFGenerator;

    @Override
    @Transactional
    public Booking bookTicket(BookingRequest request, String nic, String transactionId, String payherePaymentId, String paymentMethod) {

        User user = userRepository.findByNic(nic)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int requestedQty = request.getQuantity() > 0 ? request.getQuantity() : 1;

        // Quota පරීක්ෂාව
        int maxQuota = systemSettingRepository.findById("MAX_TICKETS_PER_24H")
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElse(3);

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long bookingCount = bookingRepository.countBookingsInLast24Hours(user.getId(), twentyFourHoursAgo);

        // වෙනස: Quantity එක අදාල කර නොගෙන, වාර ගණන පමණක් පරීක්ෂා කරයි
        if (bookingCount >= maxQuota) {
            throw new QuotaExceededException("Daily booking quota exceeded. You can only make " + maxQuota + " bookings in 24 hours.");
        }

        SeatClass seatClass = seatClassRepository.findById(request.getSeatClassId())
                .orElseThrow(() -> new RuntimeException("Seat class not found"));

        if (seatClass.getAvailableSeats() < requestedQty) {
            throw new SeatNotAvailableException("Seats not available in this class.");
        }

        Booking booking = Booking.builder()
                .transactionId(transactionId)
                .user(user)
                .train(seatClass.getTrain())
                .seatClass(seatClass)
                .quantity(requestedQty)
                .bookingTime(LocalDateTime.now())
                .journeyDate(request.getJourneyDate())
                .status("CONFIRMED")
                .payherePaymentId(payherePaymentId)
                .paymentMethod(paymentMethod)
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