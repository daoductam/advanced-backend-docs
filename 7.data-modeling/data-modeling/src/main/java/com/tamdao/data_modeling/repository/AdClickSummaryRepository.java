package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.AdClickSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AdClickSummaryRepository extends JpaRepository<AdClickSummary, Long> {

    Optional<AdClickSummary> findByAdIdAndClickDate(Long adId, LocalDate clickDate);
}
