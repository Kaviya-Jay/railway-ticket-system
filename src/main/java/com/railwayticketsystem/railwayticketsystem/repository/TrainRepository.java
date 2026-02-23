package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    /**
     * Search trains by start and end station
     * (Trains run daily - you can later add schedule table for specific dates)
     */
    List<Train> findByStartStationIdAndEndStationId(Long startStationId, Long endStationId);

    // Optional: find trains with available seats in any class
    // List<Train> findByStartStationIdAndEndStationIdAndSeatClasses_AvailableSeatsGreaterThan(
    //     Long startId, Long endId, int seats);
}
