package com.tamdao.restful_api_design.controller;

import com.tamdao.restful_api_design.model.ExportJob;
import com.tamdao.restful_api_design.service.ExportJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
public class ExportJobController {

    @Autowired
    private ExportJobService jobService;

    /**
     * Bước 1: Khởi tạo yêu cầu xuất file nặng
     * POST /api/v1/jobs/export
     */
    @PostMapping("/export")
    public ResponseEntity<ExportJob> triggerExport() {
        ExportJob job = jobService.triggerExportJob();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    /**
     * Bước 2: API thăm dò trạng thái (Polling Endpoint)
     * GET /api/v1/jobs/{jobId}
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ExportJob> getJobStatus(@PathVariable String jobId) {
        ExportJob job = jobService.getJobStatus(jobId);
        return ResponseEntity.ok(job);
    }

    /**
     * Bước 3: API tải file kết quả sau khi hoàn thành
     * GET /api/v1/jobs/{jobId}/download
     */
    @GetMapping("/{jobId}/download")
    public ResponseEntity<byte[]> downloadResult(@PathVariable String jobId) {
        ExportJob job = jobService.getJobStatus(jobId);

        if (!"DONE".equals(job.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Tác vụ chưa hoàn thành hoặc đã thất bại. Trạng thái hiện tại: " + job.getStatus()).getBytes());
        }

        // Giả lập nội dung file Excel/CSV xuất ra thành công
        String fileContent = "ID,Username,Email,CreatedAt\n" +
                "1,user_1,user1@tamdao.com,2026-05-26\n" +
                "2,user_2,user2@tamdao.com,2026-05-26\n" +
                "3,user_3,user3@tamdao.com,2026-05-26\n";

        byte[] outputBytes = fileContent.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users_report_" + jobId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(outputBytes);
    }
}
