package com.tamdao.codebase.domain.flight.entity;

import com.tamdao.codebase.domain.flight.valueobject.AircraftInfo;
import com.tamdao.codebase.domain.flight.valueobject.FlightSchedule;
import com.tamdao.codebase.domain.flight.valueobject.Route;

import java.util.List;

/**
 * Aggregate Root — Chuyến bay.
 *
 * <p>Là điểm truy cập duy nhất cho toàn bộ Aggregate "Flight".
 * Mọi thao tác với {@link Seat} bên trong phải đi qua {@code Flight}.
 *
 * <p>Định danh bằng {@code flightId} (duy nhất, không thay đổi).
 */
public class Flight {

    private String flightId;
    private String flightNumber;
    private Route route;
    private FlightSchedule schedule;
    private AircraftInfo aircraftInfo;
    private List<Seat> seats;
}
