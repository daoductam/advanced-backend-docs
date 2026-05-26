package com.tamdao.restful_api_design.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
@Getter
@Setter
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "response_body", length = 1000)
    private String responseBody;

    @Column(name = "response_status")
    private int responseStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
