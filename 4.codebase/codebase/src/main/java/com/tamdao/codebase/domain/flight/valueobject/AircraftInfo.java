package com.tamdao.codebase.domain.flight.valueobject;

/**
 * Value Object — Thông tin máy bay.
 *
 * <p>Mô tả đặc tính kỹ thuật của máy bay phục vụ chuyến bay.
 * Bất biến — hai {@code AircraftInfo} giống nhau khi tất cả thuộc tính trùng khớp.
 */
public class AircraftInfo {

    private final String aircraftModel;
    private final String airline;
    private final int totalSeats;

    public AircraftInfo(String aircraftModel, String airline, int totalSeats) {
        this.aircraftModel = aircraftModel;
        this.airline = airline;
        this.totalSeats = totalSeats;
    }
}
