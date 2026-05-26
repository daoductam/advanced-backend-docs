package com.tamdao.codebase.domain.booking.entity;

import com.tamdao.codebase.domain.booking.valueobject.BookingStatus;
import com.tamdao.codebase.domain.booking.valueobject.PaymentInfo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregate Root — Đơn đặt vé.
 *
 * <p>Là điểm truy cập duy nhất cho toàn bộ Aggregate "Booking".
 * Quản lý danh sách {@link Ticket} bên trong — hệ thống bên ngoài
 * <b>không được phép</b> truy vấn hay sửa đổi trực tiếp Ticket
 * mà phải thông qua Booking.
 *
 * <p>Đòi hỏi tính nhất quán giao dịch (Transactional Consistency):
 * khi tạo/huỷ Booking, tất cả Ticket bên trong phải đồng bộ trạng thái.
 */
public class Booking {

    private String bookingId;
    private String passengerId;
    private List<Ticket> tickets;
    private BookingStatus status;
    private PaymentInfo paymentInfo;
    private LocalDateTime bookingDate;
}
