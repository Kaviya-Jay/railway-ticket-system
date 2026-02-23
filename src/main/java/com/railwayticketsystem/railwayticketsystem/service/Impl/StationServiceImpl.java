package com.railwayticketsystem.railwayticketsystem.service.Impl;

import com.railwayticketsystem.railwayticketsystem.entity.Station;
import com.railwayticketsystem.railwayticketsystem.repository.StationRepository;
import com.railwayticketsystem.railwayticketsystem.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    @Transactional
    public Station saveStation(Station station) {
        return stationRepository.save(station);
    }

    @Override
    @Transactional
    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }
}