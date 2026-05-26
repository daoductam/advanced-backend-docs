package com.tamdao.codebase.application.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO — Dữ liệu đơn đặt vé trả về cho Client.
 *
 * <p>Chỉ chứa những thông tin Client cần biết,
 * giấu đi chi tiết nội bộ của Entity {@code Booking}.
 */
public class BookingResponse {

    private String bookingId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime bookingDate;
    private int ticketCount;
}
