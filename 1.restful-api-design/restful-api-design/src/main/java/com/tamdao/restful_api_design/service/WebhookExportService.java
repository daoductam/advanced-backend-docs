package com.tamdao.restful_api_design.service;

import com.tamdao.restful_api_design.model.ExportJob;
import com.tamdao.restful_api_design.repository.ExportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SERVICE 2: Chỉ xử lý và phục vụ cho cơ chế WEBHOOK / CALLBACK
 * - Nhận yêu cầu kèm callbackUrl, chạy ngầm.
 * - Khi hoàn thành, Server tính toán Signature bảo mật HMAC-SHA256 rồi gửi POST callback đến Client.
 */
@Service
public class WebhookExportService {

    public static final String WEBHOOK_SECRET = "TamDaoSecretKeySignature2026";

    @Autowired
    private ExportJobRepository jobRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public ExportJob triggerWebhookJob(String callbackUrl) {
        ExportJob job = new ExportJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setStatus("PROCESSING");
        job.setCallbackUrl(callbackUrl);
        job.setIssuedAt(LocalDateTime.now());
        
        jobRepository.save(job);

        // Chạy ngầm tác vụ xuất file
        executeExportTask(job.getJobId());

        return job;
    }

    @Async
    @Transactional
    public void executeExportTask(String jobId) {
        try {
            // Giả lập tác vụ nặng mất 5 giây
            Thread.sleep(5000);

            ExportJob job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("DONE");
            job.setDownloadUrl("/api/v1/jobs/" + jobId + "/download");
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            System.out.println(">>> Webhook Service: Đã xuất file thành công cho Job ID: " + jobId);

            // Gửi Webhook nếu có callbackUrl
            if (job.getCallbackUrl() != null && !job.getCallbackUrl().isEmpty()) {
                sendWebhookNotification(job);
            }

        } catch (InterruptedException e) {
            ExportJob job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus("FAILED");
                jobRepository.save(job);
            }
            Thread.currentThread().interrupt();
        }
    }

    private void sendWebhookNotification(ExportJob job) {
        try {
            String payload = String.format("{\"jobId\":\"%s\",\"status\":\"%s\",\"downloadUrl\":\"%s\"}",
                    job.getJobId(), job.getStatus(), job.getDownloadUrl());

            // Tạo Signature HMAC-SHA256 bảo mật
            String signature = calculateHmacSha256(payload, WEBHOOK_SECRET);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Signature", signature);

            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            System.out.println(">>> Webhook Service: Đang phát Webhook tới URL: " + job.getCallbackUrl());
            System.out.println(">>> Webhook Service: Chữ ký gửi đi: " + signature);

            restTemplate.postForEntity(job.getCallbackUrl(), request, String.class);
            System.out.println(">>> Webhook Service: Đã gọi Callback URL thành công!");
        } catch (Exception e) {
            System.err.println(">>> Webhook Service: Gọi Callback thất bại: " + e.getMessage());
        }
    }

    public String calculateHmacSha256(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi sinh chữ ký", e);
        }
    }
}
