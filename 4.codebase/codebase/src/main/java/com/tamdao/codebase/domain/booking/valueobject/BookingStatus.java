package com.tamdao.codebase.domain.booking.valueobject;

/**
 * Enum (Value Object) — Trạng thái đơn đặt vé.
 *
 * <p>Sử dụng Enum thay vì String thô để đảm bảo type-safety
 * và tránh lỗi magic string (ví dụ: gõ nhầm "COMFIRMED").
 *
 * <p>Vòng đời trạng thái:
 * {@code PENDING → CONFIRMED → COMPLETED}
 *              ↘ {@code CANCELLED}
 */
public enum BookingStatus {

    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
