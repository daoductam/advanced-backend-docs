package com.tamdao.data_modeling.repository;

import com.tamdao.data_modeling.entity.HomestayAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomestayAvailabilityRepository extends JpaRepository<HomestayAvailability, Long> {

    List<HomestayAvailability> findByHomestayIdAndBookingDateBetweenOrderByBookingDateAsc(
            Long homestayId, LocalDate startDate, LocalDate endDate);

    Optional<HomestayAvailability> findByHomestayIdAndBookingDate(Long homestayId, LocalDate bookingDate);
}
