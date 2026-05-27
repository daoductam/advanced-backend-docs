package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "homestay_availabilities",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"homestay_id", "booking_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomestayAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Trạng thái ngày đó:
     * - AVAILABLE: Trống, có thể đặt
     * - BOOKED: Đã được đặt
     * - BLOCKED: Bị khóa bởi chủ nhà (để sửa chữa hoặc mục đích khác)
     */
    @Column(name = "status", nullable = false)
    private String status;
}
