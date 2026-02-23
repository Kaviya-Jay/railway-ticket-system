package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.entity.Train;
import com.railwayticketsystem.railwayticketsystem.repository.TrainRepository;
import com.railwayticketsystem.railwayticketsystem.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;

    @Override
    public List<Train> searchTrains(Long startStationId, Long endStationId, LocalDate journeyDate) {
        // Simple search - assumes trains run daily (you can enhance later with schedule table)
        return trainRepository.findByStartStationIdAndEndStationId(startStationId, endStationId);
    }

    @Override
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    public Train saveTrain(Train train) {
        return trainRepository.save(train);
    }
}