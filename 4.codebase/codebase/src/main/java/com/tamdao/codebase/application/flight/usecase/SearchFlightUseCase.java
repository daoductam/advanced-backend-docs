package com.tamdao.codebase.application.flight.usecase;

import com.tamdao.codebase.application.flight.dto.FlightResponse;
import com.tamdao.codebase.application.flight.dto.SearchFlightRequest;
import com.tamdao.codebase.domain.flight.repository.FlightRepository;

import java.util.List;

/**
 * Use Case — Tìm kiếm chuyến bay.
 *
 * <p>Tầng Application chỉ <b>điều phối</b> (orchestrate) công việc:
 * nhận Request DTO → gọi Domain (qua Repository Interface) → trả Response DTO.
 * <b>Không chứa</b> quy tắc nghiệp vụ (Business Rules) — đó là việc của Domain.
 *
 * <p>Phụ thuộc: chỉ import từ Domain ({@code FlightRepository} Interface)
 * và các DTO nội bộ của Application. <b>Không</b> import Infrastructure.
 */
public class SearchFlightUseCase {

    private final FlightRepository flightRepository;

    public SearchFlightUseCase(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    // public List<FlightResponse> execute(SearchFlightRequest request) { ... }
}
