package com.railwayticketsystem.railwayticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO for Train Search Request
 * Used in SearchController for clean binding
 */
@Data
public class SearchRequest {

    @NotNull(message = "Start station is required")
    private Long startStationId;

    @NotNull(message = "End station is required")
    private Long endStationId;

    @NotNull(message = "Journey date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate journeyDate;
}
