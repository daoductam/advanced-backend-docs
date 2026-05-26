package com.tamdao.codebase.domain.passenger.valueobject;

/**
 * Value Object — Thông tin liên lạc.
 *
 * <p>Bất biến — khi hành khách cập nhật email hoặc số điện thoại,
 * tạo đối tượng {@code ContactInfo} mới và gán lại.
 *
 * <p>So sánh bằng giá trị: hai {@code ContactInfo} giống nhau
 * khi cả {@code email} lẫn {@code phoneNumber} đều trùng khớp.
 */
public class ContactInfo {

    private final String email;
    private final String phoneNumber;

    public ContactInfo(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
