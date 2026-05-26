package com.tamdao.codebase.presentation.flight;

import com.tamdao.codebase.application.flight.usecase.SearchFlightUseCase;

/**
 * Controller (Presentation Layer) — API chuyến bay.
 *
 * <p>Nằm ở tầng ngoài cùng, nhận HTTP request từ Client.
 * <b>Chỉ import từ tầng Application</b> (UseCase + DTO),
 * không bao giờ import trực tiếp từ Domain hay Infrastructure.
 *
 * <p>Luồng xử lý:
 * <pre>
 *   Client → Controller → UseCase → Domain (qua Repository Interface)
 *                                           ↑ implements
 *                                   Infrastructure (Adapter thực tế)
 * </pre>
 */
public class FlightController {

    private final SearchFlightUseCase searchFlightUseCase;

    public FlightController(SearchFlightUseCase searchFlightUseCase) {
        this.searchFlightUseCase = searchFlightUseCase;
    }

    // @GetMapping("/flights/search")
    // public List<FlightResponse> searchFlights(SearchFlightRequest request) { ... }
}
