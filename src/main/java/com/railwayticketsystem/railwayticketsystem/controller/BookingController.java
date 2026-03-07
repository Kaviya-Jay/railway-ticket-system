package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.BookingRequest;
import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.repository.BookingRepository;
import com.railwayticketsystem.railwayticketsystem.repository.SeatClassRepository;
import com.railwayticketsystem.railwayticketsystem.repository.SystemSettingRepository;
import com.railwayticketsystem.railwayticketsystem.service.BookingService;
import com.railwayticketsystem.railwayticketsystem.util.PayHereUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SeatClassRepository seatClassRepository;
    private final BookingRepository bookingRepository;
    private final SystemSettingRepository systemSettingRepository;

    private final String MERCHANT_ID = "1234323";
    private final String MERCHANT_SECRET = "MzczMTAwNjAwOTEzMjQxMDY0ODU3NDg5Njc5MTcxNzE2MTA3MTUx";

    // ඔබේ Ngrok URL එක මෙතනට ලබා දෙන්න
    private final String NGROK_BASE_URL = "https://autarkic-jaqueline-sprawly.ngrok-free.dev";

    @GetMapping("/booking/confirm")
    public String showBookingConfirm(
            @RequestParam Long trainId,
            @RequestParam Long seatClassId,
            @RequestParam LocalDate journeyDate,
            @RequestParam(defaultValue = "1") int quantity,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        int maxQuota = systemSettingRepository.findById("MAX_TICKETS_PER_24H")
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElse(3);

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long bookingCount = bookingRepository.countBookingsInLast24Hours(currentUser.getId(), twentyFourHoursAgo);

        if (bookingCount >= maxQuota) {
            redirectAttributes.addFlashAttribute("error", "Daily Quota Exceeded! You have already made " + maxQuota + " bookings in the last 24 hours.");
            return "redirect:/search";
        }

        model.addAttribute("trainId", trainId);
        model.addAttribute("seatClassId", seatClassId);
        model.addAttribute("journeyDate", journeyDate);
        model.addAttribute("quantity", quantity);
        model.addAttribute("user", currentUser);

        return "booking-confirm";
    }

    @PostMapping("/payment/checkout")
    public String processCheckout(
            @RequestParam Long trainId,
            @RequestParam Long seatClassId,
            @RequestParam LocalDate journeyDate,
            @RequestParam int quantity,
            @AuthenticationPrincipal User currentUser,
            Model model) {

        SeatClass seatClass = seatClassRepository.findById(seatClassId).orElseThrow();
        double amount = seatClass.getPrice().doubleValue() * quantity;
        String orderId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String currency = "LKR";

        String hash = PayHereUtil.generateHash(MERCHANT_ID, orderId, amount, currency, MERCHANT_SECRET);

        String notifyUrl = NGROK_BASE_URL + "/payhere/notify";
        String returnUrl = NGROK_BASE_URL + "/payment/return";
        String cancelUrl = NGROK_BASE_URL + "/payment/cancel";

        String custom1 = trainId + "|" + seatClassId + "|" + journeyDate + "|" + quantity;
        String custom2 = currentUser.getNic();

        model.addAttribute("merchantId", MERCHANT_ID);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("cancelUrl", cancelUrl);
        model.addAttribute("notifyUrl", notifyUrl);
        model.addAttribute("orderId", orderId);
        model.addAttribute("items", "Sri Lanka Railway E-Ticket");
        model.addAttribute("currency", currency);
        model.addAttribute("amount", amount);
        model.addAttribute("user", currentUser);
        model.addAttribute("hash", hash);
        model.addAttribute("custom1", custom1);
        model.addAttribute("custom2", custom2);

        return "payhere-checkout";
    }

    @ResponseBody
    @PostMapping("/payhere/notify")
    public String payhereNotify(
            @RequestParam("merchant_id") String merchantId,
            @RequestParam("order_id") String orderId,
            @RequestParam("payhere_amount") String payhereAmount,
            @RequestParam("payhere_currency") String payhereCurrency,
            @RequestParam("status_code") int statusCode,
            @RequestParam("md5sig") String md5sig,
            @RequestParam("custom_1") String custom1,
            @RequestParam("custom_2") String custom2,
            @RequestParam(value = "payment_id", required = false) String paymentId,
            @RequestParam(value = "method", required = false) String method) {

        String localMd5sig = PayHereUtil.generateWebhookHash(MERCHANT_ID, orderId, payhereAmount, payhereCurrency, statusCode, MERCHANT_SECRET);

        if (localMd5sig.equals(md5sig) && statusCode == 2) {
            try {
                String[] bookingDetails = custom1.split("\\|");
                BookingRequest bookingReq = new BookingRequest();
                bookingReq.setTrainId(Long.parseLong(bookingDetails[0]));
                bookingReq.setSeatClassId(Long.parseLong(bookingDetails[1]));
                bookingReq.setJourneyDate(LocalDate.parse(bookingDetails[2]));
                bookingReq.setQuantity(Integer.parseInt(bookingDetails[3]));

                bookingService.bookTicket(bookingReq, custom2, orderId, paymentId, method);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "OK";
    }

    @GetMapping("/payment/return")
    public String paymentReturn(@RequestParam("order_id") String orderId, Model model) {
        model.addAttribute("transactionId", orderId);
        return "booking-success";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Payment was cancelled. Please try again.");
        return "redirect:/search";
    }

    @GetMapping("/booking/success/{transactionId}")
    public String bookingSuccess(@PathVariable String transactionId, Model model) {
        model.addAttribute("transactionId", transactionId);
        return "booking-success";
    }

    // වෙනස: inline යොදා ඇති බැවින් Browser එකේම විවෘත වේ
    @GetMapping("/download/pdf/{transactionId}")
    public ResponseEntity<Resource> downloadTicket(@PathVariable String transactionId) {
        try {
            Path filePath = Paths.get("src/main/resources/static/tickets/TICKET-" + transactionId + ".pdf").toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"TICKET-" + transactionId + ".pdf\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Ticket PDF not found!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading ticket: " + e.getMessage());
        }
    }
}