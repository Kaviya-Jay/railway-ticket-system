package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.repository.BookingRepository;
import com.railwayticketsystem.railwayticketsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal User currentUser, Model model) {
        User user = userService.findByNic(currentUser.getNic());

        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(user.getId());

        long totalTicketsBooked = bookingRepository.countTotalTicketsBooked(user.getId());
        BigDecimal totalSpent = bookingRepository.calculateTotalSpent(user.getId());

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        long ticketsInLast24Hrs = bookingRepository.countBookingsInLast24Hours(user.getId(), twentyFourHoursAgo);
        long quotaRemaining = Math.max(0, 3 - ticketsInLast24Hrs);

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalTicketsBooked", totalTicketsBooked);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("quotaRemaining", quotaRemaining);

        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal User currentUser, Model model) {
        User user = userService.findByNic(currentUser.getNic());
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @ModelAttribute RegisterRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByNic(currentUser.getNic());
            userService.updateProfile(user, request);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile. Please try again.");
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(@AuthenticationPrincipal User currentUser, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByNic(currentUser.getNic());
            userService.deleteProfile(user.getId());
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting account. Please try again.");
            return "redirect:/profile";
        }
    }
}