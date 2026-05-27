package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "todo_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Cột thứ tự kiểu số thực (DOUBLE):
     * - Khoảng cách ban đầu giữa các phần tử là 1000.0 (ví dụ: 1000.0, 2000.0, 3000.0...)
     * - Khi chèn giữa phần tử A và B: order_index = (A + B) / 2
     * - Nhờ kiểu DOUBLE, hệ thống có thể chia đôi khoảng trống vô số lần (ví dụ:
     *   1500.0 → 1250.0 → 1125.0 → 1062.5 → ...) mà KHÔNG cần cập nhật hàng loạt
     *   các phần tử lân cận như khi dùng INT.
     */
    @Column(name = "order_index", nullable = false)
    private Double orderIndex;

    @Column(name = "status")
    private String status;
}
