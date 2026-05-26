package com.tamdao.codebase.domain.passenger.valueobject;

import java.time.LocalDate;

/**
 * Value Object — Thông tin hộ chiếu.
 *
 * <p>Bất biến — khi hành khách đổi hộ chiếu, tạo đối tượng
 * {@code PassportInfo} mới hoàn toàn và gán lại cho {@code Passenger}.
 *
 * <p>Đây là ví dụ điển hình cho Value Object trong DDD:
 * không có ID riêng, so sánh bằng giá trị tất cả thuộc tính.
 */
public class PassportInfo {

    private final String passportNumber;
    private final String nationality;
    private final LocalDate expiryDate;

    public PassportInfo(String passportNumber, String nationality, LocalDate expiryDate) {
        this.passportNumber = passportNumber;
        this.nationality = nationality;
        this.expiryDate = expiryDate;
    }
}
