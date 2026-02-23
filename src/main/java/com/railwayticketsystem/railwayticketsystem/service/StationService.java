package com.railwayticketsystem.railwayticketsystem.service;

import com.railwayticketsystem.railwayticketsystem.entity.Station;
import java.util.List;

public interface StationService {
    List<Station> getAllStations();
    Station saveStation(Station station);
    void deleteStation(Long id);
}