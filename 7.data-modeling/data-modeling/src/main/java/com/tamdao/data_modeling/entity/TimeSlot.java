package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "begin_local_time", nullable = false)
    private LocalDateTime beginLocalTime;

    @Column(name = "end_local_time", nullable = false)
    private LocalDateTime endLocalTime;

    @Column(name = "timezone_id", nullable = false)
    private String timezoneId;

    /**
     * Trạng thái cụ thể cho buổi này:
     * - ACTIVE: Vẫn diễn ra bình thường
     * - CANCELLED: Đã bị hủy riêng lẻ
     */
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
}
