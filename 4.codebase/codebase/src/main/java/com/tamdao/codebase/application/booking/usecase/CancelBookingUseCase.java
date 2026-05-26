package com.tamdao.codebase.application.booking.usecase;

import com.tamdao.codebase.application.booking.dto.CancelBookingRequest;
import com.tamdao.codebase.domain.booking.repository.BookingRepository;

/**
 * Use Case — Huỷ đơn đặt vé.
 *
 * <p>Điều phối: nhận request → tìm Booking → cập nhật status → lưu lại.
 */
public class CancelBookingUseCase {

    private final BookingRepository bookingRepository;

    public CancelBookingUseCase(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // public void execute(CancelBookingRequest request) { ... }
}
