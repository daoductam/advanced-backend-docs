package com.tamdao.codebase.domain.passenger.repository;

import com.tamdao.codebase.domain.passenger.entity.Passenger;

import java.util.Optional;

/**
 * Repository Interface (Port) — Kho dữ liệu hành khách.
 *
 * <p>Interface nằm trong tầng Domain — giống như {@code FlightRepository}
 * và {@code BookingRepository}, đây là "Cổng" (Port) để tầng Application
 * tương tác với tầng Infrastructure mà không vi phạm Dependency Inversion.
 */
public interface PassengerRepository {

    Optional<Passenger> findById(String passengerId);

    void save(Passenger passenger);

    void deleteById(String passengerId);
}
