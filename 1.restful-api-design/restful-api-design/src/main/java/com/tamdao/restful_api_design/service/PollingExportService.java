package com.tamdao.restful_api_design.service;

import com.tamdao.restful_api_design.model.ExportJob;
import com.tamdao.restful_api_design.repository.ExportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SERVICE 1: Chỉ xử lý và phục vụ cho cơ chế POLLING
 * - Nhận yêu cầu, chạy ngầm, cập nhật DONE/FAILED vào DB.
 * - Frontend tự động Polling GET API để lấy trạng thái.
 */
@Service
public class PollingExportService {

    @Autowired
    private ExportJobRepository jobRepository;

    @Transactional
    public ExportJob triggerPollingJob() {
        ExportJob job = new ExportJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setStatus("PROCESSING");
        job.setIssuedAt(LocalDateTime.now());
        
        jobRepository.save(job);

        // Chạy ngầm tác vụ xuất file
        executeExportTask(job.getJobId());

        return job;
    }

    @Transactional(readOnly = true)
    public ExportJob getJobStatus(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Job ID: " + jobId));
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

            System.out.println(">>> Polling Service: Đã xuất file thành công cho Job ID: " + jobId);
        } catch (InterruptedException e) {
            ExportJob job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus("FAILED");
                jobRepository.save(job);
            }
            Thread.currentThread().interrupt();
        }
    }
}
