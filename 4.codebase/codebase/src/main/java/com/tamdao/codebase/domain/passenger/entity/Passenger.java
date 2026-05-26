package com.tamdao.codebase.domain.passenger.entity;

import com.tamdao.codebase.domain.passenger.valueobject.ContactInfo;
import com.tamdao.codebase.domain.passenger.valueobject.PassportInfo;

/**
 * Aggregate Root — Hành khách.
 *
 * <p>Quản lý thông tin cá nhân, hộ chiếu và liên lạc của hành khách.
 * Là điểm truy cập duy nhất cho Aggregate "Passenger".
 *
 * <p>Lưu ý: {@code PassportInfo} và {@code ContactInfo} là Value Object,
 * khi cần cập nhật → tạo đối tượng mới và gán lại, không sửa trực tiếp.
 */
public class Passenger {

    private String passengerId;
    private String firstName;
    private String lastName;
    private PassportInfo passportInfo;
    private ContactInfo contactInfo;
}
