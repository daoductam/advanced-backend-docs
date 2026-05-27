package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.AdClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdClickLogRepository extends JpaRepository<AdClickLog, Long> {

    long countByAdIdAndClickedAtBetween(Long adId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT l.adId FROM AdClickLog l WHERE l.clickedAt BETWEEN :start AND :end")
    List<Long> findDistinctAdIdsWithClicksBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
