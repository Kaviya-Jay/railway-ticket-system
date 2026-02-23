package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
import com.railwayticketsystem.railwayticketsystem.entity.Station;
import com.railwayticketsystem.railwayticketsystem.entity.Train;
import com.railwayticketsystem.railwayticketsystem.repository.BookingRepository;
import com.railwayticketsystem.railwayticketsystem.repository.SeatClassRepository;
import com.railwayticketsystem.railwayticketsystem.repository.TrainRepository;
import com.railwayticketsystem.railwayticketsystem.service.StationService;
import com.railwayticketsystem.railwayticketsystem.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StationService stationService;
    private final TrainService trainService;
    private final TrainRepository trainRepository;          // for findById
    private final SeatClassRepository seatClassRepository;
    private final BookingRepository bookingRepository;

    // ====================== DASHBOARD ======================
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Quick stats
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
        model.addAttribute("stations", stationService.getAllStations()); // for dropdown
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

    // ====================== SEAT CLASSES MANAGEMENT (Update Availability) ======================
    @GetMapping("/trains/{trainId}/seats")
    public String manageSeatClasses(@PathVariable Long trainId, Model model) {
        Optional<Train> trainOpt = trainRepository.findById(trainId);
        if (trainOpt.isEmpty()) {
            throw new RuntimeException("Train not found");
        }
        List<SeatClass> seatClasses = seatClassRepository.findByTrainId(trainId);

        model.addAttribute("train", trainOpt.get());
        model.addAttribute("seatClasses", seatClasses);
        return "admin/seat-classes";
    }

    @PostMapping("/seats/update/{seatClassId}")
    public String updateSeatAvailability(
            @PathVariable Long seatClassId,
            @RequestParam int availableSeats,
            RedirectAttributes redirectAttributes) {

        SeatClass seatClass = seatClassRepository.findById(seatClassId)
                .orElseThrow(() -> new RuntimeException("Seat class not found"));

        seatClass.setAvailableSeats(availableSeats);
        seatClassRepository.save(seatClass);

        redirectAttributes.addFlashAttribute("success", "Seat availability updated!");
        return "redirect:/admin/trains/" + seatClass.getTrain().getId() + "/seats";
    }

    // ====================== TRANSACTION REPORTS ======================
    @GetMapping("/reports")
    public String viewReports(Model model) {
        List<Booking> recentBookings = bookingRepository.findTop10ByOrderByBookingTimeDesc();

        long totalBookings = bookingRepository.count();
        long todayBookings = bookingRepository.countByBookingTimeAfter(LocalDateTime.now().minusDays(1));

        // Simple revenue (you can improve with JPQL sum later)
        BigDecimal totalRevenue = BigDecimal.ZERO; // Placeholder - enhance if needed

        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("todayBookings", todayBookings);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin/reports";
    }
}
