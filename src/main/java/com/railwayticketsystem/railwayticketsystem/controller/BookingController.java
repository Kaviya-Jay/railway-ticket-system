package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.exception.QuotaExceededException;
import com.railwayticketsystem.railwayticketsystem.exception.SeatNotAvailableException;
import com.railwayticketsystem.railwayticketsystem.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Step 1: Show booking confirmation page with selected train & class
    @GetMapping("/booking/confirm")
    public String showBookingConfirm(
            @RequestParam Long trainId,
            @RequestParam Long seatClassId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate,
            @AuthenticationPrincipal User currentUser,
            Model model) {

        model.addAttribute("trainId", trainId);
        model.addAttribute("seatClassId", seatClassId);
        model.addAttribute("journeyDate", journeyDate);
        model.addAttribute("user", currentUser);

        return "booking-confirm";
    }

    // Step 2: Process Booking → Mock Payment
    @PostMapping("/book")
    public String processBooking(@ModelAttribute BookingRequest request,
                                 @AuthenticationPrincipal User currentUser,
                                 RedirectAttributes redirectAttributes) {

        try {
            Booking booking = bookingService.bookTicket(request, currentUser.getNic());

            redirectAttributes.addFlashAttribute("booking", booking);
            return "redirect:/booking/success/" + booking.getTransactionId();

        } catch (QuotaExceededException | SeatNotAvailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/search";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
            return "redirect:/search";
        }
    }

    // Step 3: Booking Success Page with Download links
    @GetMapping("/booking/success/{transactionId}")
    public String bookingSuccess(@PathVariable String transactionId,
                                 Model model) {
        model.addAttribute("transactionId", transactionId);
        return "booking-success";
    }

    // Download PDF Ticket
    @GetMapping("/download/pdf/{transactionId}")
    public String downloadTicket(@PathVariable String transactionId, Model model) {
        model.addAttribute("pdfPath", "/tickets/TICKET-" + transactionId + ".pdf");
        return "redirect:/booking/success/" + transactionId; // or serve file via Resource
    }
}
