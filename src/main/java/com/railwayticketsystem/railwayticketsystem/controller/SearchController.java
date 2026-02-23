package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.entity.Station;
import com.railwayticketsystem.railwayticketsystem.entity.Train;
import com.railwayticketsystem.railwayticketsystem.service.StationService;
import com.railwayticketsystem.railwayticketsystem.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final StationService stationService;
    private final TrainService trainService;

    @GetMapping("/search")
    public String showSearchPage(Model model) {
        List<Station> stations = stationService.getAllStations();
        model.addAttribute("stations", stations);
        model.addAttribute("journeyDate", LocalDate.now().plusDays(1));
        return "search";
    }

    @PostMapping("/search")
    public String searchTrains(
            @RequestParam Long startStationId,
            @RequestParam Long endStationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate,
            Model model) {

        List<Train> trains = trainService.searchTrains(startStationId, endStationId, journeyDate);

        model.addAttribute("trains", trains);
        model.addAttribute("startStationId", startStationId);
        model.addAttribute("endStationId", endStationId);
        model.addAttribute("journeyDate", journeyDate);

        return "search-results";
    }
}