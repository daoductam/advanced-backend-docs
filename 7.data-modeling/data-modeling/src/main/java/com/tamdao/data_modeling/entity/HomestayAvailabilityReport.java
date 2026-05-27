package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "v_homestay_availability_report")
@Immutable // Đánh dấu thực thể chỉ đọc, Hibernate sẽ không kiểm tra thay đổi hay sinh câu lệnh update
@Getter
@NoArgsConstructor
public class HomestayAvailabilityReport {

    @Id
    private Long id;

    @Column(name = "homestay_id")
    private Long homestayId;

    @Column(name = "homestay_name")
    private String homestayName;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "status")
    private String status;
}
