package com.tamdao.codebase.domain.booking.service;

/**
 * Domain Service — Dịch vụ miền cho đặt vé.
 *
 * <p>Chứa logic nghiệp vụ phức tạp liên quan đến đặt vé mà
 * <b>không thuộc trách nhiệm</b> riêng của {@code Booking} hay {@code Ticket}.
 *
 * <p>Ví dụ: tính giá vé dựa trên hạng ghế + tuyến bay + thời điểm đặt,
 * kiểm tra tính hợp lệ của đơn đặt vé (ghế còn trống, hành khách hợp lệ).
 *
 * <p><b>Stateless</b> — không lưu trạng thái nội bộ.
 */
public class BookingDomainService {

    // Ví dụ: tính giá vé
    // public BigDecimal calculateTicketPrice(Flight flight, String seatClass) { ... }

    // Ví dụ: kiểm tra đặt vé hợp lệ
    // public boolean isBookingValid(Booking booking, Flight flight) { ... }
}
