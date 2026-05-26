package com.tamdao.codebase.domain.booking.repository;

import com.tamdao.codebase.domain.booking.entity.Booking;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface (Port) — Kho dữ liệu đặt vé.
 *
 * <p>Interface nằm trong tầng Domain.
 * Implementation thực tế ({@code BookingRepositoryImpl}) nằm ở tầng Infrastructure.
 *
 * <p>Minh họa nguyên lý <b>Dependency Inversion</b>:
 * UseCase (tầng Application) gọi method qua Interface này,
 * không biết và không quan tâm dữ liệu lưu ở MySQL, MongoDB hay bộ nhớ.
 */
public interface BookingRepository {

    Optional<Booking> findById(String bookingId);

    List<Booking> findByPassengerId(String passengerId);

    void save(Booking booking);

    void deleteById(String bookingId);
}
