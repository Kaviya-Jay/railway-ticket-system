package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.entity.Train;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainService {
    List<Train> searchTrains(Long startStationId, Long endStationId, LocalDate journeyDate);
    List<Train> getAllTrains();
    Train saveTrain(Train train);
    Optional<Train> findById(Long id);
}