package com.railwayticketsystem.railwayticketsystem.repository;

import com.railwayticketsystem.railwayticketsystem.entity.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatClassRepository extends JpaRepository<SeatClass, Long> {

    List<SeatClass> findByTrainId(Long trainId);

    // Optional: find available seat classes for a train
    List<SeatClass> findByTrainIdAndAvailableSeatsGreaterThan(Long trainId, int minSeats);
}