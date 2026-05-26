package com.tamdao.codebase.application.booking.usecase;

import com.tamdao.codebase.application.booking.dto.BookingResponse;
import com.tamdao.codebase.application.booking.dto.CreateBookingRequest;
import com.tamdao.codebase.domain.booking.repository.BookingRepository;
import com.tamdao.codebase.domain.flight.repository.FlightRepository;

/**
 * Use Case — Tạo đơn đặt vé.
 *
 * <p>Điều phối luồng: nhận request → kiểm tra chuyến bay còn ghế
 * → tạo Booking (gọi Domain) → lưu qua Repository Interface → trả DTO.
 *
 * <p>Inject cả {@code BookingRepository} lẫn {@code FlightRepository}
 * (đều là Interface từ Domain) qua constructor — Dependency Injection.
 */
public class CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;

    public CreateBookingUseCase(BookingRepository bookingRepository,
                                FlightRepository flightRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
    }

    // public BookingResponse execute(CreateBookingRequest request) { ... }
}
