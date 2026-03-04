package com.railwayticketsystem.railwayticketsystem.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class SearchRequest {

    @NotNull(message = "Start station is required")
    private Long startStationId;

    @NotNull(message = "End station is required")
    private Long endStationId;

    @NotNull(message = "Please select a journey date")
    @Future(message = "Journey date must be from tomorrow onwards")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate journeyDate;
}