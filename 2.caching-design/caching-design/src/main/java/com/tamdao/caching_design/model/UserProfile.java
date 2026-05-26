package com.tamdao.caching_design.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Entity đại diện cho bảng USER_PROFILE trong Database.
 *
 * implements Serializable là BẮT BUỘC khi lưu object Java vào Redis.
 * Redis lưu dữ liệu dưới dạng byte[] nên object phải được serialize.
 */
@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserProfile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * Đây là trường chúng ta sẽ dùng để demo Race Condition.
     * Kịch bản trong README 2.7:
     *   - Cache chứa age = 1 (cũ)
     *   - DB được cập nhật thành age = 2 (mới)
     */
    @Column(nullable = false)
    private int age;

    @Column
    private String email;
}
