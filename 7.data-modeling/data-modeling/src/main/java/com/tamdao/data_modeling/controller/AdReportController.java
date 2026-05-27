package com.tamdao.data_modeling.controller;

import com.tamdao.data_modeling.entity.AdClickLog;
import com.tamdao.data_modeling.entity.AdClickSummary;
import com.tamdao.data_modeling.repository.AdClickLogRepository;
import com.tamdao.data_modeling.repository.AdClickSummaryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ads")
@Tag(name = "Report Queries", description = "APIs thu thập log click và chạy tác vụ Scheduled Job tổng hợp báo cáo (Pre-aggregation)")
public class AdReportController {

    @Autowired
    private AdClickLogRepository logRepository;

    @Autowired
    private AdClickSummaryRepository summaryRepository;

    @PostMapping("/{adId}/click")
    @Operation(summary = "Ghi nhận 1 lượt click quảng cáo mới (Ghi vào bảng Log chi tiết)")
    @Transactional
    public ResponseEntity<AdClickLog> recordClick(
            @PathVariable Long adId,
            @RequestParam(required = false, defaultValue = "0.0.0.0") String ip) {

        AdClickLog log = new AdClickLog();
        log.setAdId(adId);
        log.setClickedAt(LocalDateTime.now());
        log.setVisitorIp(ip);

        AdClickLog savedLog = logRepository.save(log);
        return ResponseEntity.ok(savedLog);
    }

    @GetMapping("/{adId}/report")
    @Operation(summary = "Xem báo cáo số lượt click theo ngày (Query O(1) từ bảng tổng hợp summaries)")
    public ResponseEntity<AdClickSummary> getReport(
            @PathVariable Long adId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Optional<AdClickSummary> summaryOpt = summaryRepository.findByAdIdAndClickDate(adId, date);
        
        // Nếu chưa được tổng hợp trong summaries, trả về kết quả 0 clicks thay vì lỗi
        AdClickSummary summary = summaryOpt.orElseGet(() -> {
            AdClickSummary s = new AdClickSummary();
            s.setAdId(adId);
            s.setClickDate(date);
            s.setClickCount(0L);
            return s;
        });

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/trigger-aggregation")
    @Operation(summary = "Kích hoạt thủ công tiến trình chạy ngầm (Scheduled Job) tổng hợp log click của một ngày thành báo cáo")
    @Transactional
    public ResponseEntity<String> triggerAggregation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999999);

        // Lấy toàn bộ các Ad ID có phát sinh click trong ngày
        List<Long> adIds = logRepository.findDistinctAdIdsWithClicksBetween(start, end);

        int updatedCount = 0;
        for (Long adId : adIds) {
            // Đếm tổng số click log trong ngày của Ad ID này
            long clicksCount = logRepository.countByAdIdAndClickedAtBetween(adId, start, end);

            // Cập nhật hoặc tạo mới bản ghi báo cáo
            AdClickSummary summary = summaryRepository.findByAdIdAndClickDate(adId, date)
                    .orElse(new AdClickSummary());
            
            summary.setAdId(adId);
            summary.setClickDate(date);
            summary.setClickCount(clicksCount);

            summaryRepository.save(summary);
            updatedCount++;
        }

        return ResponseEntity.ok(String.format("Tổng hợp dữ liệu ngày %s thành công. Đã cập nhật %d mẫu quảng cáo.", 
                date, updatedCount));
    }
}
