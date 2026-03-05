package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.entity.*;
import com.railwayticketsystem.railwayticketsystem.repository.*;
import com.railwayticketsystem.railwayticketsystem.service.StationService;
import com.railwayticketsystem.railwayticketsystem.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StationService stationService;
    private final TrainService trainService;
    private final TrainRepository trainRepository;
    private final SeatClassRepository seatClassRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;

    // ====================== DASHBOARD ======================
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long totalBookings = bookingRepository.count();
        long todayBookings = bookingRepository.countByBookingTimeAfter(LocalDateTime.now().minusDays(1));

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("todayBookings", todayBookings);
        model.addAttribute("totalStations", stationService.getAllStations().size());
        model.addAttribute("totalTrains", trainService.getAllTrains().size());

        return "admin/dashboard";
    }

    // ====================== STATIONS MANAGEMENT ======================
    @GetMapping("/stations")
    public String listStations(Model model) {
        model.addAttribute("stations", stationService.getAllStations());
        return "admin/stations";
    }

    @GetMapping("/stations/new")
    public String newStationForm(Model model) {
        model.addAttribute("station", new Station());
        return "admin/station-form";
    }

    @GetMapping("/stations/edit/{id}")
    public String editStationForm(@PathVariable Long id, Model model) {
        Station station = stationService.getAllStations().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Station not found"));
        model.addAttribute("station", station);
        return "admin/station-form";
    }

    @PostMapping("/stations")
    public String saveStation(@ModelAttribute Station station, RedirectAttributes redirectAttributes) {
        stationService.saveStation(station);
        redirectAttributes.addFlashAttribute("success", "Station saved successfully!");
        return "redirect:/admin/stations";
    }

    @GetMapping("/stations/delete/{id}")
    public String deleteStation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        stationService.deleteStation(id);
        redirectAttributes.addFlashAttribute("success", "Station deleted successfully!");
        return "redirect:/admin/stations";
    }

    // ====================== TRAINS MANAGEMENT ======================
    @GetMapping("/trains")
    public String listTrains(Model model) {
        model.addAttribute("trains", trainService.getAllTrains());
        return "admin/trains";
    }

    @GetMapping("/trains/new")
    public String newTrainForm(Model model) {
        model.addAttribute("train", new Train());
        model.addAttribute("stations", stationService.getAllStations());
        return "admin/train-form";
    }

    @GetMapping("/trains/edit/{id}")
    public String editTrainForm(@PathVariable Long id, Model model) {
        Optional<Train> trainOpt = trainRepository.findById(id);
        if (trainOpt.isEmpty()) {
            throw new RuntimeException("Train not found");
        }
        model.addAttribute("train", trainOpt.get());
        model.addAttribute("stations", stationService.getAllStations());
        return "admin/train-form";
    }

    @PostMapping("/trains")
    public String saveTrain(@ModelAttribute Train train, RedirectAttributes redirectAttributes) {
        trainService.saveTrain(train);
        redirectAttributes.addFlashAttribute("success", "Train saved successfully!");
        return "redirect:/admin/trains";
    }

    @GetMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        trainRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Train deleted successfully!");
        return "redirect:/admin/trains";
    }

    // ====================== SEAT CLASSES MANAGEMENT ======================
    @GetMapping("/trains/{trainId}/seats")
    public String manageSeatClasses(@PathVariable Long trainId, Model model) {
        Optional<Train> trainOpt = trainRepository.findById(trainId);
        if (trainOpt.isEmpty()) throw new RuntimeException("Train not found");

        model.addAttribute("train", trainOpt.get());
        model.addAttribute("seatClasses", seatClassRepository.findByTrainId(trainId));
        return "admin/seat-classes";
    }

    // යාවත්කාලීන කරන ලද Seat Update මෙතඩ් එක
    @PostMapping("/seats/update/{seatClassId}")
    public String updateSeatAvailability(
            @PathVariable Long seatClassId,
            @RequestParam BigDecimal price,
            @RequestParam int totalSeats,
            @RequestParam int availableSeats,
            RedirectAttributes redirectAttributes) {

        SeatClass seatClass = seatClassRepository.findById(seatClassId)
                .orElseThrow(() -> new RuntimeException("Seat class not found"));

        seatClass.setPrice(price);
        seatClass.setTotalSeats(totalSeats);
        seatClass.setAvailableSeats(availableSeats);

        seatClassRepository.save(seatClass);
        redirectAttributes.addFlashAttribute("success", seatClass.getClassType() + " class details updated successfully!");
        return "redirect:/admin/trains/" + seatClass.getTrain().getId() + "/seats";
    }

    // ====================== USER & ADMIN MANAGEMENT ======================
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findByRole(Role.USER));
        return "admin/users";
    }

    @GetMapping("/admins")
    public String listAdmins(Model model) {
        model.addAttribute("admins", userRepository.findByRole(Role.ADMIN));
        return "admin/admins";
    }

    @GetMapping("/users/make-admin/{id}")
    public String makeAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", user.getFullName() + " is now an Admin!");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users";
    }

    // ====================== TICKET BOOKING QUOTA ======================
    @GetMapping("/quota")
    public String manageQuota(Model model) {
        SystemSetting quotaSetting = systemSettingRepository.findById("MAX_TICKETS_PER_24H")
                .orElse(new SystemSetting("MAX_TICKETS_PER_24H", "3"));
        model.addAttribute("currentQuota", quotaSetting.getValue());
        return "admin/quota";
    }

    @PostMapping("/quota")
    public String updateQuota(@RequestParam String quotaValue, RedirectAttributes redirectAttributes) {
        SystemSetting setting = new SystemSetting("MAX_TICKETS_PER_24H", quotaValue);
        systemSettingRepository.save(setting);
        redirectAttributes.addFlashAttribute("success", "Booking Quota updated to " + quotaValue + " tickets per 24 hours.");
        return "redirect:/admin/quota";
    }

    // ====================== TRANSACTION REPORTS ======================
    @GetMapping("/reports")
    public String viewReports(Model model) {
        model.addAttribute("recentBookings", bookingRepository.findTop10ByOrderByBookingTimeDesc());
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("todayBookings", bookingRepository.countByBookingTimeAfter(LocalDateTime.now().minusDays(1)));
        model.addAttribute("totalRevenue", BigDecimal.ZERO);
        return "admin/reports";
    }
}