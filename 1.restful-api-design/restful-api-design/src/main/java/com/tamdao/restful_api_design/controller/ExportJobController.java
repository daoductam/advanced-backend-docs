package com.tamdao.restful_api_design.controller;

import com.tamdao.restful_api_design.model.ExportJob;
import com.tamdao.restful_api_design.service.PollingExportService;
import com.tamdao.restful_api_design.service.WebhookExportService;
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
    private PollingExportService pollingExportService;

    @Autowired
    private WebhookExportService webhookExportService;

    /**
     * CÁCH 1 (POLLING): Khởi tạo yêu cầu xuất file theo cơ chế POLLING
     * POST /api/v1/jobs/export-polling
     */
    @PostMapping("/export-polling")
    public ResponseEntity<ExportJob> triggerPollingExport() {
        ExportJob job = pollingExportService.triggerPollingJob();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    /**
     * CÁCH 1 (POLLING): API thăm dò trạng thái (dùng cho Polling)
     * GET /api/v1/jobs/{jobId}
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ExportJob> getJobStatus(@PathVariable String jobId) {
        ExportJob job = pollingExportService.getJobStatus(jobId);
        return ResponseEntity.ok(job);
    }

    /**
     * CÁCH 2 (WEBHOOK): Khởi tạo yêu cầu xuất file theo cơ chế WEBHOOK
     * POST /api/v1/jobs/export-webhook?callbackUrl=http://localhost:8080/api/v1/jobs/receive-webhook
     */
    @PostMapping("/export-webhook")
    public ResponseEntity<ExportJob> triggerWebhookExport(@RequestParam String callbackUrl) {
        ExportJob job = webhookExportService.triggerWebhookJob(callbackUrl);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    /**
     * CÁCH 2 (WEBHOOK): Endpoint thu nhận webhook (để kiểm tra xem webhook bắn về đúng không)
     * POST /api/v1/jobs/receive-webhook
     */
    @PostMapping("/receive-webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Webhook-Signature") String signature) {
        
        System.out.println("<<< Client Receiver: Nhận được Webhook payload: " + payload);
        System.out.println("<<< Client Receiver: Nhận được Signature Header: " + signature);

        // Kiểm tra tính bảo mật chữ ký HMAC-SHA256
        String expectedSignature = webhookExportService.calculateHmacSha256(payload, WebhookExportService.WEBHOOK_SECRET);

        if (expectedSignature.equals(signature)) {
            System.out.println("<<< Client Receiver: Chữ ký hợp lệ! Đã xử lý webhook thành công.");
            return ResponseEntity.ok("Webhook processed successfully");
        } else {
            System.err.println("<<< Client Receiver: Cảnh báo! Sai chữ ký. Từ chối xử lý.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
    }

    /**
     * TẢI KẾT QUẢ FILE CHUNG (cho cả 2 cách)
     * GET /api/v1/jobs/{jobId}/download
     */
    @GetMapping("/{jobId}/download")
    public ResponseEntity<byte[]> downloadResult(@PathVariable String jobId) {
        ExportJob job = pollingExportService.getJobStatus(jobId);

        if (!"DONE".equals(job.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Tác vụ chưa hoàn thành hoặc đã thất bại. Trạng thái hiện tại: " + job.getStatus()).getBytes());
        }

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
