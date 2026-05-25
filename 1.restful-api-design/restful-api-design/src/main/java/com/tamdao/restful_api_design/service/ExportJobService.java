package com.tamdao.restful_api_design.service;

import com.tamdao.restful_api_design.model.ExportJob;
import com.tamdao.restful_api_design.repository.ExportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ExportJobService {

    @Autowired
    private ExportJobRepository jobRepository;

    /**
     * Khởi tạo một Export Job mới và đưa vào hàng chờ xử lý bất đồng bộ
     */
    @Transactional
    public ExportJob triggerExportJob() {
        ExportJob job = new ExportJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setStatus("PROCESSING");
        job.setIssuedAt(LocalDateTime.now());
        
        jobRepository.save(job);

        // Kích hoạt tiến trình ngầm (Background Task)
        executeExportTask(job.getJobId());

        return job;
    }

    /**
     * Lấy thông tin trạng thái hiện tại của Job
     */
    @Transactional(readOnly = true)
    public ExportJob getJobStatus(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Job ID: " + jobId));
    }

    /**
     * Logic xuất dữ liệu giả lập chạy bất đồng bộ dưới nền tốn 5 giây
     */
    @Async
    @Transactional
    public void executeExportTask(String jobId) {
        try {
            // Giả lập tác vụ nặng (ví dụ: truy vấn DB lớn, build Excel)
            Thread.sleep(5000);

            ExportJob job = jobRepository.findById(jobId).orElseThrow();
            job.setStatus("DONE");
            // Đường dẫn giả lập tải file kết quả
            job.setDownloadUrl("/api/v1/jobs/" + jobId + "/download");
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

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
