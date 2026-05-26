package com.tamdao.restful_api_design.controller;

import com.tamdao.restful_api_design.model.IdempotencyRecord;
import com.tamdao.restful_api_design.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private IdempotencyService idempotencyService;

    /**
     * API Giao dịch Thanh toán giả lập (Nghiệp vụ cực kỳ nhạy cảm)
     * POST /api/v1/payments
     * Bắt buộc truyền Header: Idempotency-Key
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Map<String, Object> paymentPayload) {

        // 1. Kiểm tra nếu Client không truyền Idempotency-Key
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Thiếu header bắt buộc: Idempotency-Key\"}");
        }

        // 2. Tra cứu xem Key này đã được gửi lên hệ thống và xử lý trước đó chưa
        Optional<IdempotencyRecord> existingRecord = idempotencyService.findRecord(idempotencyKey);
        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            System.out.println(">>> Payment API: Phát hiện request trùng lặp với Key: " + idempotencyKey);
            System.out.println(">>> Payment API: Trả ngay kết quả cũ mà không thực hiện lại giao dịch.");

            return ResponseEntity.status(record.getResponseStatus()).body(record.getResponseBody());
        }

        // 3. Tiến hành khóa key (Tránh trường hợp 2 request song song đồng thời)
        boolean isLocked = idempotencyService.lockKey(idempotencyKey);
        if (!isLocked) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\":\"Yêu cầu đang được xử lý, vui lòng không gửi lại liên tục!\"}");
        }

        try {
            // Giả lập logic xử lý trừ tiền thanh toán (Ví dụ tốn 2 giây)
            System.out.println(">>> Payment API: Đang thực hiện xử lý trừ tiền cho yêu cầu mới... Key: " + idempotencyKey);
            Thread.sleep(2000);

            // Giả lập kết quả trả về
            double amount = Double.parseDouble(paymentPayload.getOrDefault("amount", "0").toString());
            String responseBody = String.format("{\"status\":\"SUCCESS\",\"transactionId\":\"TXN-%s\",\"amount\":%.2f}",
                    idempotencyKey.substring(0, 8), amount);
            int successStatus = HttpStatus.OK.value();

            // 4. Lưu kết quả phản hồi cuối cùng vào DB để tái sử dụng
            idempotencyService.saveResult(idempotencyKey, successStatus, responseBody);

            return ResponseEntity.status(successStatus).body(responseBody);

        } catch (Exception e) {
            // Lỗi hệ thống: Giải phóng lock trên Redis ngay lập tức để Client có thể gửi lại lần sau
            idempotencyService.unlockKey(idempotencyKey);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Lỗi hệ thống khi xử lý thanh toán, vui lòng thử lại!\"}");
        }
    }
}
