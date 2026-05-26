package com.tamdao.codebase.application.flight.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) — Dữ liệu chuyến bay trả về cho Client.
 *
 * <p><b>Tại sao cần DTO thay vì trả thẳng Entity?</b>
 * <ul>
 *   <li>Nếu trả trực tiếp Entity {@code Flight} ra API → thay đổi cấu trúc
 *       bảng DB sẽ trực tiếp vỡ JSON response cho Frontend.</li>
 *   <li>DTO giúp kiểm soát chính xác những gì Client nhận được,
 *       giấu đi các chi tiết nội bộ của Domain.</li>
 *   <li>DTO nằm ở tầng Application — ranh giới giữa trong và ngoài.</li>
 * </ul>
 */
public class FlightResponse {

    private String flightId;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int availableSeats;
}
