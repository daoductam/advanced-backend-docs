package com.tamdao.codebase.domain.booking.entity;

/**
 * Entity — Vé máy bay.
 *
 * <p>Là Entity con nằm trong Aggregate {@link Booking}.
 * Có {@code ticketId} riêng để định danh.
 * Mỗi Ticket gắn với 1 chuyến bay (flightId) và 1 ghế ngồi (seatId).
 *
 * <p>Quy tắc Aggregate: Không truy cập Ticket trực tiếp từ bên ngoài,
 * mọi thao tác phải đi qua {@link Booking} (Aggregate Root).
 */
public class Ticket {

    private String ticketId;
    private String flightId;
    private String seatId;
    private String passengerName;
}
