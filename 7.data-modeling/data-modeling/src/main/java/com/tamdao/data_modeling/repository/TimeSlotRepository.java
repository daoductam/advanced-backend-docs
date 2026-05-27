package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByBeginLocalTimeBetweenOrderByBeginLocalTimeAsc(
            LocalDateTime start, LocalDateTime end);
}
