package com.tamdao.data_modeling.controller;

import com.tamdao.data_modeling.entity.Event;
import com.tamdao.data_modeling.entity.TimeSlot;
import com.tamdao.data_modeling.repository.EventRepository;
import com.tamdao.data_modeling.repository.TimeSlotRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Calendar Events", description = "APIs quản lý Lịch biểu sự kiện lặp lại sử dụng cơ chế Pre-generated Time Slots")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Getter
    @Setter
    public static class CreateEventRequest {
        private String title;
        private String description;
        private String recurrence; // NONE, DAILY, WEEKLY
        private LocalDateTime startDateTime;
        private long durationMinutes;
        private String timezoneId = "Asia/Ho_Chi_Minh";
    }

    @PostMapping
    @Operation(summary = "Tạo mới sự kiện và tự động sinh trước các slot thời gian biểu lặp lại (10 lần lặp để chạy thử)")
    @Transactional
    public ResponseEntity<Event> createEvent(@RequestBody CreateEventRequest request) {
        if (request.getRecurrence() == null) {
            request.setRecurrence("NONE");
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setRecurrence(request.getRecurrence());
        Event savedEvent = eventRepository.save(event);

        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime firstStart = request.getStartDateTime() != null ? request.getStartDateTime() : LocalDateTime.now();
        long duration = request.getDurationMinutes() > 0 ? request.getDurationMinutes() : 60;
        String tz = request.getTimezoneId();

        int loopCount = 1;
        if ("DAILY".equalsIgnoreCase(event.getRecurrence())) {
            loopCount = 10; // sinh 10 ngày liên tiếp
        } else if ("WEEKLY".equalsIgnoreCase(event.getRecurrence())) {
            loopCount = 10; // sinh 10 tuần liên tiếp
        }

        for (int i = 0; i < loopCount; i++) {
            TimeSlot slot = new TimeSlot();
            slot.setEvent(savedEvent);
            
            LocalDateTime currentStart;
            if ("DAILY".equalsIgnoreCase(event.getRecurrence())) {
                currentStart = firstStart.plusDays(i);
            } else if ("WEEKLY".equalsIgnoreCase(event.getRecurrence())) {
                currentStart = firstStart.plusWeeks(i);
            } else {
                currentStart = firstStart;
            }

            slot.setBeginLocalTime(currentStart);
            slot.setEndLocalTime(currentStart.plusMinutes(duration));
            slot.setTimezoneId(tz);
            slot.setStatus("ACTIVE");
            slots.add(slot);
        }

        timeSlotRepository.saveAll(slots);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
    }

    @GetMapping("/slots")
    @Operation(summary = "Xem lịch biểu thời gian thực tế trong khoảng thời gian cụ thể (Query Index tối ưu)")
    public ResponseEntity<List<TimeSlot>> getSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start phải trước hoặc bằng end");
        }

        List<TimeSlot> slots = timeSlotRepository.findByBeginLocalTimeBetweenOrderByBeginLocalTimeAsc(start, end);
        return ResponseEntity.ok(slots);
    }

    @PutMapping("/slots/{slotId}")
    @Operation(summary = "Sửa đổi dời giờ hoặc hủy lịch (Cancel) chỉ duy nhất một slot cụ thể")
    @Transactional
    public ResponseEntity<TimeSlot> updateSingleSlot(
            @PathVariable Long slotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newBegin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEnd,
            @RequestParam(required = false) String status) {

        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy TimeSlot"));

        if (newBegin != null) {
            slot.setBeginLocalTime(newBegin);
        }
        if (newEnd != null) {
            slot.setEndLocalTime(newEnd);
        }
        if (status != null) {
            slot.setStatus(status); // Ví dụ: "CANCELLED" hoặc "ACTIVE"
        }

        TimeSlot updated = timeSlotRepository.save(slot);
        return ResponseEntity.ok(updated);
    }
}
