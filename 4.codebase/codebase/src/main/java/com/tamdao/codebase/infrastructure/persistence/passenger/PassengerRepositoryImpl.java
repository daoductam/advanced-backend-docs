package com.tamdao.codebase.infrastructure.persistence.passenger;

import com.tamdao.codebase.domain.passenger.entity.Passenger;
import com.tamdao.codebase.domain.passenger.repository.PassengerRepository;

import java.util.Optional;

/**
 * Repository Implementation (Adapter) — Hiện thực hóa lưu trữ hành khách.
 *
 * <p>Implements {@link PassengerRepository} (Interface ở tầng Domain).
 */
public class PassengerRepositoryImpl implements PassengerRepository {

    @Override
    public Optional<Passenger> findById(String passengerId) {
        // TODO: Implement
        return Optional.empty();
    }

    @Override
    public void save(Passenger passenger) {
        // TODO: Implement
    }

    @Override
    public void deleteById(String passengerId) {
        // TODO: Implement
    }
}
