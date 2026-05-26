package com.tamdao.codebase.presentation.booking;

import com.tamdao.codebase.application.booking.usecase.CancelBookingUseCase;
import com.tamdao.codebase.application.booking.usecase.CreateBookingUseCase;

/**
 * Controller (Presentation Layer) — API đặt vé.
 *
 * <p>Inject các UseCase cần thiết, mỗi UseCase đảm nhận một hành động cụ thể
 * (Single Responsibility).
 */
public class BookingController {

    private final CreateBookingUseCase createBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;

    public BookingController(CreateBookingUseCase createBookingUseCase,
                             CancelBookingUseCase cancelBookingUseCase) {
        this.createBookingUseCase = createBookingUseCase;
        this.cancelBookingUseCase = cancelBookingUseCase;
    }

    // @PostMapping("/bookings")
    // public BookingResponse createBooking(CreateBookingRequest request) { ... }

    // @PostMapping("/bookings/cancel")
    // public void cancelBooking(CancelBookingRequest request) { ... }
}
