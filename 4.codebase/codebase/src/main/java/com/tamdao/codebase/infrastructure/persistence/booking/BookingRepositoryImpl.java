package com.tamdao.codebase.infrastructure.persistence.booking;

import com.tamdao.codebase.domain.booking.entity.Booking;
import com.tamdao.codebase.domain.booking.repository.BookingRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Implementation (Adapter) — Hiện thực hóa lưu trữ đặt vé.
 *
 * <p>Implements {@link BookingRepository} (Interface ở tầng Domain).
 * Spring DI sẽ inject class này vào các UseCase lúc runtime.
 */
public class BookingRepositoryImpl implements BookingRepository {

    @Override
    public Optional<Booking> findById(String bookingId) {
        // TODO: Implement
        return Optional.empty();
    }

    @Override
    public List<Booking> findByPassengerId(String passengerId) {
        // TODO: Implement
        return List.of();
    }

    @Override
    public void save(Booking booking) {
        // TODO: Implement
    }

    @Override
    public void deleteById(String bookingId) {
        // TODO: Implement
    }
}
