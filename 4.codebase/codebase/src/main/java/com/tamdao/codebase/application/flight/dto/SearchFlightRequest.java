package com.tamdao.codebase.application.flight.dto;

import java.time.LocalDate;

/**
 * DTO — Yêu cầu tìm kiếm chuyến bay từ Client.
 *
 * <p>Đóng gói tham số tìm kiếm thành một đối tượng rõ ràng
 * thay vì truyền 4-5 tham số rời (tránh code smell: Long Parameter List).
 */
public class SearchFlightRequest {

    private String departureAirport;
    private String arrivalAirport;
    private LocalDate departureDate;
    private String seatClass;
}
