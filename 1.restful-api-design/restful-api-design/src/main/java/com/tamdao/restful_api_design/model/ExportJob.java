package com.tamdao.restful_api_design.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "export_jobs")
@Getter
@Setter
public class ExportJob {

    @Id
    private String jobId;

    private String status; // PROCESSING, DONE, FAILED

    private String downloadUrl;

    private String callbackUrl;

    private LocalDateTime issuedAt;

    private LocalDateTime completedAt;
}
