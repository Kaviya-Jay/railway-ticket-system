package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.SearchRequest;
import com.railwayticketsystem.railwayticketsystem.entity.Station;
import com.railwayticketsystem.railwayticketsystem.entity.Train;
import com.railwayticketsystem.railwayticketsystem.service.StationService;
import com.railwayticketsystem.railwayticketsystem.service.TrainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setJourneyDate(LocalDate.now().plusDays(1)); // default = tomorrow

        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("stations", stations);
        model.addAttribute("minDate", LocalDate.now().plusDays(1).toString());
        model.addAttribute("maxDate", LocalDate.now().plusDays(60).toString());

        if (stations.isEmpty()) {
            model.addAttribute("error", "No stations available. Please add stations via admin panel.");
        }

        return "search";
    }

    @PostMapping("/search")
    public String searchTrains(@Valid @ModelAttribute("searchRequest") SearchRequest searchRequest,
                               BindingResult bindingResult,
                               Model model) {

        List<Station> stations = stationService.getAllStations();

        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stations);
            model.addAttribute("minDate", LocalDate.now().plusDays(1).toString());
            model.addAttribute("maxDate", LocalDate.now().plusDays(60).toString());
            return "search";
        }

        List<Train> trains = trainService.searchTrains(
                searchRequest.getStartStationId(),
                searchRequest.getEndStationId(),
                searchRequest.getJourneyDate());

        model.addAttribute("trains", trains);
        model.addAttribute("journeyDate", searchRequest.getJourneyDate());

        return "search-results";
    }
}