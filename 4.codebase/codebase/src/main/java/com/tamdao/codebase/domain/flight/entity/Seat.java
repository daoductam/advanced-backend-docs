package com.tamdao.codebase.domain.flight.entity;

/**
 * Entity — Ghế ngồi trên chuyến bay.
 *
 * <p>Là Entity con nằm trong Aggregate {@link Flight}.
 * Có {@code seatId} riêng để định danh.
 * Thuộc tính {@code isBooked} có thể thay đổi khi hành khách đặt/huỷ vé.
 *
 * <p>Không được truy cập trực tiếp từ bên ngoài Aggregate —
 * mọi thao tác phải thông qua {@link Flight} (Aggregate Root).
 */
public class Seat {

    private String seatId;
    private String seatNumber;
    private String seatClass;    // ECONOMY, BUSINESS, FIRST_CLASS
    private boolean isBooked;
}
