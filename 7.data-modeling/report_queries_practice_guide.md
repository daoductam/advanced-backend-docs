# Hướng dẫn thực hành: Thống kê & Báo cáo (Report Queries)

Tài liệu này giải thích chi tiết phương pháp thiết kế cơ sở dữ liệu và triển khai backend cho các tác vụ thống kê lượng truy cập lớn (ví dụ: click quảng cáo, lượt xem bài viết, lượt tải trang) với tốc độ phản hồi tối ưu dưới dạng **Pre-aggregation (Tổng hợp dữ liệu trước)**.

---

## 1. Bài toán Thống kê lượng lớn (High-volume Analytics)

Trong các hệ thống thực tế như thống kê click quảng cáo (Ad Clicks) hoặc lượt xem video:
*   Mỗi giây có hàng ngàn lượt tương tác được ghi nhận. Dữ liệu thô (Raw Logs) tăng lên cực kỳ nhanh.
*   **Vấn đề:** Khi quản trị viên truy vấn xem "Quảng cáo ID 101 ngày hôm qua được click bao nhiêu lần?", DB phải chạy câu lệnh `SELECT COUNT(*) FROM ad_click_logs WHERE ad_id = 101 AND date = '2026-05-26'`. Phép toán quét bảng log hàng triệu bản ghi sẽ rất chậm và có thể làm sập DB.

---

## 2. Giải pháp: Pre-aggregation (Tổng hợp dữ liệu qua Scheduled Job)

Để giải quyết bài toán hiệu năng, ta áp dụng mô hình phân tách:
1.  **Write Path (Đường Ghi):** Người dùng click -> Hệ thống INSERT cực nhanh vào bảng log chi tiết `ad_click_logs` (Không tính toán gì thêm).
2.  **Scheduled Job (Tiến trình chạy ngầm):** Định kỳ chạy ngầm (ví dụ: cuối ngày hoặc mỗi giờ) quét bảng log, cộng dồn tổng số lượt click cho từng quảng cáo, rồi lưu kết quả vào bảng tổng hợp `ad_click_summaries` (`ad_id`, `click_date`, `click_count`).
3.  **Read Path (Đường Đọc):** Khi người dùng xem báo cáo -> Hệ thống SELECT trực tiếp từ bảng `ad_click_summaries` theo `ad_id` và `click_date`. Thời gian phản hồi gần như $O(1)$ (chỉ chứa 1 dòng kết quả duy nhất).

---

## 3. Hướng dẫn thử nghiệm & Kiểm thử

### Bước 1: Xem báo cáo click ngày hôm qua (Đã được Seeder chạy trước)
Gọi API lấy số lượt click của Quảng cáo ID `101` vào ngày hôm qua (chú ý điền đúng ngày hôm qua, ví dụ: `2026-05-26`):
```bash
# Thay thế yyyy-MM-dd bằng ngày hôm qua thực tế
curl "http://localhost:8080/api/ads/101/report?date=2026-05-26"
```
*   **Kết quả:** Trả về ngay lập tức với `clickCount: 25` được lấy từ bảng `ad_click_summaries` (không cần quét bảng logs).

### Bước 2: Tạo một số clicks mới cho ngày hôm nay
Giả sử có 3 lượt click mới phát sinh hôm nay:
```bash
curl -X POST "http://localhost:8080/api/ads/101/click?ip=192.168.1.100"
curl -X POST "http://localhost:8080/api/ads/101/click?ip=192.168.1.101"
curl -X POST "http://localhost:8080/api/ads/101/click?ip=192.168.1.102"
```

### Bước 3: Xem báo cáo ngày hôm nay (Trước khi tổng hợp)
```bash
# Thay thế yyyy-MM-dd bằng ngày hôm nay thực tế
curl "http://localhost:8080/api/ads/101/report?date=2026-05-27"
```
*   **Kết quả:** Trả về `clickCount: 0` vì tiến trình chạy ngầm tổng hợp chưa chạy.

### Bước 4: Kích hoạt thủ công tiến trình tổng hợp ngày hôm nay
Để thử nghiệm thay vì chờ Cron chạy qua đêm, ta kích hoạt thủ công tiến trình tổng hợp cho ngày hôm nay:
```bash
# Thay thế yyyy-MM-dd bằng ngày hôm nay thực tế
curl -X POST "http://localhost:8080/api/ads/trigger-aggregation?date=2026-05-27"
```

### Bước 5: Xem lại báo cáo ngày hôm nay (Sau khi tổng hợp)
Gửi lại request ở Bước 3:
```bash
curl "http://localhost:8080/api/ads/101/report?date=2026-05-27"
```
*   **Kết quả:** Trả về chính xác `clickCount: 3` ngay lập tức!

---

## 4. Giải pháp thay thế: Ước lượng với HyperLogLog (Redis)
Nếu hệ thống có thể chấp nhận sai số rất nhỏ (khoảng ~1%) nhưng đòi hỏi dữ liệu thống kê đếm duy nhất (Unique Visitors) thời gian thực mà không cần chạy Job cuối ngày:
*   Chúng ta sử dụng cấu trúc dữ liệu **HyperLogLog** tích hợp sẵn trong Redis.
*   Khi có click: `PFADD ad:clicks:101:2026-05-27 visitor_ip`
*   Khi lấy số lượng: `PFCOUNT ad:clicks:101:2026-05-27` (Thời gian phản hồi $O(1)$ và tốn cực kỳ ít bộ nhớ, chỉ tối đa 12KB cho mỗi key để chứa hàng triệu phần tử).
