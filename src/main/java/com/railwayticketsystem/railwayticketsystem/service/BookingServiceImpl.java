package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
import com.railwayticketsystem.railwayticketsystem.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepo;
    private final SeatClassRepository seatRepo;
    private final UserRepository userRepo;
    private final QRCodeGenerator qrGenerator;
    private final TicketPDFGenerator pdfGenerator;

    @Override
    public Booking bookTicket(BookingRequest req, String nic) {
        User user = userRepo.findByNic(nic).orElseThrow();

        // 1. QUOTA CHECK (Rolling 24 hours)
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long count = bookingRepo.countBookingsInLast24Hours(user.getId(), twentyFourHoursAgo);
        if (count >= 3) {
            throw new QuotaExceededException("You have reached the maximum 3 tickets in last 24 hours (NIC-based quota).");
        }

        // 2. Find SeatClass
        SeatClass seatClass = seatRepo.findById(req.getSeatClassId())
                .orElseThrow(() -> new RuntimeException("Seat class not found"));

        if (seatClass.getAvailableSeats() <= 0) {
            throw new SeatNotAvailableException("No seats available in this class.");
        }

        // 3. Generate unique Transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. Create Booking
        Booking booking = Booking.builder()
                .transactionId(transactionId)
                .user(user)
                .train(seatClass.getTrain())
                .seatClass(seatClass)
                .bookingTime(LocalDateTime.now())
                .journeyDate(req.getJourneyDate())
                .build();

        // 5. Reduce seat (Optimistic locking via @Version)
        seatClass.setAvailableSeats(seatClass.getAvailableSeats() - 1);
        seatRepo.save(seatClass);   // version check happens here

        Booking saved = bookingRepo.save(booking);

        // 6. Generate QR & PDF
        String qrPath = qrGenerator.generateQR(saved);
        String pdfPath = pdfGenerator.generateTicketPDF(saved, qrPath);

        saved.setPdfPath(pdfPath);
        return bookingRepo.save(saved);
    }
}