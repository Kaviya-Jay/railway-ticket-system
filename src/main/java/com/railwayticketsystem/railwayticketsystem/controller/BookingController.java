package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.dto.PaymentRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.exception.QuotaExceededException;
import com.railwayticketsystem.railwayticketsystem.exception.SeatNotAvailableException;
import com.railwayticketsystem.railwayticketsystem.service.BookingService;
import com.railwayticketsystem.railwayticketsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping("/booking/confirm")
    public String showBookingConfirm(
            @RequestParam Long trainId,
            @RequestParam Long seatClassId,
            @RequestParam LocalDate journeyDate,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal User currentUser,
            Model model) {

        model.addAttribute("trainId", trainId);
        model.addAttribute("seatClassId", seatClassId);
        model.addAttribute("journeyDate", journeyDate);
        model.addAttribute("quantity", quantity);
        model.addAttribute("user", currentUser);

        return "booking-confirm";
    }

    @GetMapping("/payment/mock")
    public String showPaymentMock(
            @RequestParam Long trainId,
            @RequestParam Long seatClassId,
            @RequestParam LocalDate journeyDate,
            @RequestParam int quantity,
            Model model) {

        model.addAttribute("paymentRequest", new PaymentRequest());
        model.addAttribute("trainId", trainId);
        model.addAttribute("seatClassId", seatClassId);
        model.addAttribute("journeyDate", journeyDate);
        model.addAttribute("quantity", quantity);

        return "payment-mock";
    }

    @PostMapping("/payment/process")
    public String processPayment(@ModelAttribute PaymentRequest paymentReq,
                                 @AuthenticationPrincipal User currentUser,
                                 RedirectAttributes redirectAttributes) {

        if (paymentService.processMockPayment(paymentReq)) {
            BookingRequest bookingReq = new BookingRequest();
            bookingReq.setTrainId(paymentReq.getTrainId());
            bookingReq.setSeatClassId(paymentReq.getSeatClassId());
            bookingReq.setJourneyDate(paymentReq.getJourneyDate());
            bookingReq.setQuantity(paymentReq.getQuantity());

            try {
                Booking booking = bookingService.bookTicket(bookingReq, currentUser.getNic());
                redirectAttributes.addFlashAttribute("booking", booking);
                return "redirect:/booking/success/" + booking.getTransactionId();
            } catch (QuotaExceededException | SeatNotAvailableException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/search";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
                return "redirect:/search";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Mock payment failed. Please try again.");
            return "redirect:/payment/mock?trainId=" + paymentReq.getTrainId() + "&seatClassId=" + paymentReq.getSeatClassId() + "&journeyDate=" + paymentReq.getJourneyDate() + "&quantity=" + paymentReq.getQuantity();
        }
    }

    @GetMapping("/booking/success/{transactionId}")
    public String bookingSuccess(@PathVariable String transactionId,
                                 Model model) {
        model.addAttribute("transactionId", transactionId);
        return "booking-success";
    }

    @GetMapping("/download/pdf/{transactionId}")
    public String downloadTicket(@PathVariable String transactionId, Model model) {
        model.addAttribute("pdfPath", "/tickets/TICKET-" + transactionId + ".pdf");
        return "redirect:/booking/success/" + transactionId;
    }
}