package com.tamdao.codebase.domain.booking.valueobject;

import java.math.BigDecimal;

/**
 * Value Object — Thông tin thanh toán.
 *
 * <p>Bất biến (Immutable) — khi thanh toán thay đổi (ví dụ: hoàn tiền),
 * tạo đối tượng {@code PaymentInfo} mới thay vì sửa đối tượng cũ.
 *
 * <p>So sánh bằng giá trị: hai {@code PaymentInfo} giống nhau khi
 * tất cả thuộc tính (amount, currency, method) trùng khớp.
 */
public class PaymentInfo {

    private final BigDecimal amount;
    private final String currency;
    private final String paymentMethod;

    public PaymentInfo(BigDecimal amount, String currency, String paymentMethod) {
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
    }
}
