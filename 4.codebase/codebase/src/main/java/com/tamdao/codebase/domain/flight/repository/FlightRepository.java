package com.tamdao.codebase.domain.flight.repository;

import com.tamdao.codebase.domain.flight.entity.Flight;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface (Port) — Kho dữ liệu chuyến bay.
 *
 * <p>Đây là <b>Interface</b> nằm trong tầng Domain, đóng vai trò là "Cổng" (Port)
 * theo kiến trúc Hexagonal / Clean Architecture.
 *
 * <p><b>Tại sao Interface nằm ở Domain?</b>
 * <ul>
 *   <li>Để Domain không phụ thuộc vào Infrastructure (Dependency Inversion).</li>
 *   <li>Lớp Application gọi lưu trữ thông qua Interface này.</li>
 *   <li>Lớp Infrastructure cung cấp Implementation thực tế (Adapter).</li>
 *   <li>Spring DI sẽ inject đúng implementation lúc runtime.</li>
 * </ul>
 */
public interface FlightRepository {

    Optional<Flight> findById(String flightId);

    List<Flight> findAll();

    void save(Flight flight);

    void deleteById(String flightId);
}
