package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.HomestayAvailabilityReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayAvailabilityReportRepository extends JpaRepository<HomestayAvailabilityReport, Long> {

    List<HomestayAvailabilityReport> findByHomestayNameContainingIgnoreCase(String homestayName);
}
