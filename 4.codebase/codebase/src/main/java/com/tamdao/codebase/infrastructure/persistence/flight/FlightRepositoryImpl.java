package com.tamdao.codebase.infrastructure.persistence.flight;

import com.tamdao.codebase.domain.flight.entity.Flight;
import com.tamdao.codebase.domain.flight.repository.FlightRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Implementation (Adapter) — Hiện thực hóa lưu trữ chuyến bay.
 *
 * <p>Đây là "Adapter" trong kiến trúc Hexagonal, nằm ở tầng Infrastructure.
 * Implements {@link FlightRepository} Interface được khai báo ở tầng Domain.
 *
 * <p><b>Minh họa Dependency Inversion:</b>
 * <pre>
 *   Domain:         FlightRepository (Interface / Port)
 *                          ↑ implements
 *   Infrastructure: FlightRepositoryImpl (Class / Adapter)
 * </pre>
 *
 * <p>Khi cần đổi từ MySQL sang PostgreSQL hay MongoDB,
 * chỉ cần tạo Adapter mới implement cùng Interface — không sửa Domain.
 */
public class FlightRepositoryImpl implements FlightRepository {

    @Override
    public Optional<Flight> findById(String flightId) {
        // TODO: Implement với JPA/JDBC/MyBatis
        return Optional.empty();
    }

    @Override
    public List<Flight> findAll() {
        // TODO: Implement
        return List.of();
    }

    @Override
    public void save(Flight flight) {
        // TODO: Implement
    }

    @Override
    public void deleteById(String flightId) {
        // TODO: Implement
    }
}
