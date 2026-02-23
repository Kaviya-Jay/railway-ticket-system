package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.entity.Train;
import java.time.LocalDate;
import java.util.List;

public interface TrainService {
    List<Train> searchTrains(Long startStationId, Long endStationId, LocalDate journeyDate);
    List<Train> getAllTrains();
    Train saveTrain(Train train);
}