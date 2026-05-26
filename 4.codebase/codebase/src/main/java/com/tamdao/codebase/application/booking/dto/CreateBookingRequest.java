package com.tamdao.codebase.application.booking.dto;

import java.util.List;

/**
 * DTO — Yêu cầu tạo đơn đặt vé từ Client.
 */
public class CreateBookingRequest {

    private String passengerId;
    private List<TicketRequest> tickets;

    /**
     * DTO lồng — Thông tin mỗi vé trong đơn đặt.
     */
    public static class TicketRequest {
        private String flightId;
        private String seatId;
    }
}
