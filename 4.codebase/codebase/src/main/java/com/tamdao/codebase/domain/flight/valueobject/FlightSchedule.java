package com.tamdao.codebase.domain.flight.valueobject;

import java.time.LocalDateTime;

/**
 * Value Object — Lịch trình bay.
 *
 * <p>Chứa thời gian khởi hành và hạ cánh.
 * Bất biến — khi cần thay đổi lịch bay, tạo {@code FlightSchedule} mới
 * và gán lại cho {@link com.tamdao.codebase.domain.flight.entity.Flight}.
 */
public class FlightSchedule {

    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;

    public FlightSchedule(LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
}
