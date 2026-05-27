package com.tamdao.data_modeling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(
    name = "ad_click_summaries",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ad_id", "click_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdClickSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad_id", nullable = false)
    private Long adId;

    @Column(name = "click_date", nullable = false)
    private LocalDate clickDate;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;
}
