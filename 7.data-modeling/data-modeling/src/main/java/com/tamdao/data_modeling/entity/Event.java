package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Kiểu lặp lại:
     * - NONE: Không lặp lại
     * - DAILY: Lặp hàng ngày
     * - WEEKLY: Lặp hàng tuần
     */
    @Column(name = "recurrence", nullable = false)
    private String recurrence;
}
