package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    List<Train> findByStartStationIdAndEndStationId(Long startStationId, Long endStationId);
}