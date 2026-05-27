package com.tamdao.data_modeling.controller;

import com.tamdao.data_modeling.entity.HomestayAvailabilityReport;
import com.tamdao.data_modeling.repository.HomestayAvailabilityReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/homestay-availabilities")
@Tag(name = "Database View Report", description = "APIs truy vấn báo cáo từ Database View (v_homestay_availability_report)")
public class HomestayReportController {

    @Autowired
    private HomestayAvailabilityReportRepository reportRepository;

    @GetMapping
    @Operation(summary = "Lấy báo cáo chi tiết giá và lịch trống của Homestays từ Database View (MERGE Join ở DB)")
    public ResponseEntity<List<HomestayAvailabilityReport>> getReport(
            @RequestParam(required = false) String name) {

        List<HomestayAvailabilityReport> results;
        if (name != null && !name.trim().isEmpty()) {
            results = reportRepository.findByHomestayNameContainingIgnoreCase(name);
        } else {
            results = reportRepository.findAll();
        }
        return ResponseEntity.ok(results);
    }
}
