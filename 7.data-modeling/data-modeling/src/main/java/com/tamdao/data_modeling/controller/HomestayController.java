package com.tamdao.data_modeling.controller;

import com.tamdao.data_modeling.entity.Homestay;
import com.tamdao.data_modeling.entity.HomestayAvailability;
import com.tamdao.data_modeling.repository.HomestayAvailabilityRepository;
import com.tamdao.data_modeling.repository.HomestayRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/homestays")
@Tag(name = "Homestay Availability Booking", description = "APIs quản lý đặt phòng Homestay với cơ chế Pre-allocation")
public class HomestayController {

    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private HomestayAvailabilityRepository availabilityRepository;

    @PostMapping
    @Operation(summary = "Tạo mới Homestay và tự động chạy tác vụ khởi tạo trước 365 ngày trống")
    @Transactional
    public ResponseEntity<Homestay> createHomestay(@RequestBody Homestay homestay) {
        if (homestay.getDefaultPrice() == null) {
            homestay.setDefaultPrice(BigDecimal.valueOf(500000)); // 500k mặc định
        }
        Homestay savedHomestay = homestayRepository.save(homestay);

        // Pre-allocate 365 ngày cho Homestay vừa tạo kể từ ngày hôm nay
        List<HomestayAvailability> availabilities = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 365; i++) {
            HomestayAvailability availability = new HomestayAvailability();
            availability.setHomestay(savedHomestay);
            availability.setBookingDate(today.plusDays(i));
            availability.setPrice(savedHomestay.getDefaultPrice());
            availability.setStatus("AVAILABLE");
            availabilities.add(availability);
        }
        availabilityRepository.saveAll(availabilities);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedHomestay);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Xem lịch trống và giá phòng trong khoảng thời gian cụ thể")
    public ResponseEntity<List<HomestayAvailability>> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate phải trước hoặc bằng endDate");
        }

        List<HomestayAvailability> list = availabilityRepository
                .findByHomestayIdAndBookingDateBetweenOrderByBookingDateAsc(id, startDate, endDate);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/price")
    @Operation(summary = "Cập nhật giá phòng linh hoạt cho một ngày cụ thể")
    @Transactional
    public ResponseEntity<HomestayAvailability> updatePrice(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam BigDecimal price) {

        HomestayAvailability availability = availabilityRepository.findByHomestayIdAndBookingDate(id, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Không tìm thấy thông tin lịch phòng của ngày này. Có thể chưa được khởi tạo."));

        availability.setPrice(price);
        HomestayAvailability updated = availabilityRepository.save(availability);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/book")
    @Operation(summary = "Đặt phòng từ ngày startDate đến endDate (kiểm tra phòng trống cực nhanh và cập nhật trạng thái)")
    @Transactional
    public ResponseEntity<String> bookHomestay(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate phải trước hoặc bằng endDate");
        }

        // Lấy tất cả các ngày trong khoảng yêu cầu đặt
        List<HomestayAvailability> list = availabilityRepository
                .findByHomestayIdAndBookingDateBetweenOrderByBookingDateAsc(id, startDate, endDate);

        long expectedDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (list.size() < expectedDays) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Có một số ngày trong khoảng yêu cầu chưa được khởi tạo lịch trống. Vui lòng liên hệ Admin.");
        }

        // Kiểm tra xem tất cả các ngày trong khoảng này có AVAILABLE không
        for (HomestayAvailability day : list) {
            if (!"AVAILABLE".equals(day.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        String.format("Phòng không khả dụng vào ngày %s (Trạng thái hiện tại: %s)", 
                                day.getBookingDate(), day.getStatus()));
            }
        }

        // Chuyển toàn bộ các dòng này sang trạng thái BOOKED
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (HomestayAvailability day : list) {
            day.setStatus("BOOKED");
            totalAmount = totalAmount.add(day.getPrice());
        }

        availabilityRepository.saveAll(list);

        return ResponseEntity.ok(String.format("Đặt phòng thành công từ ngày %s đến %s. Tổng số tiền: %,.2f VND", 
                startDate, endDate, totalAmount));
    }
}
