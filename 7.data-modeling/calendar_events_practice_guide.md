# Hướng dẫn thực hành: Lịch biểu sự kiện lặp lại (Calendar Events)

Tài liệu này giải thích chi tiết phương pháp thiết kế cơ sở dữ liệu và triển khai backend cho hệ thống Calendar có tính chất lặp lại (Recurrent Calendar Events).

---

## 1. Bài toán Lịch biểu lặp lại

Trong các hệ thống đặt lịch hoặc quản lý thời gian biểu (như Google Calendar, Microsoft Outlook, lịch học, lịch làm việc):
*   Một sự kiện có thể lặp lại nhiều lần (Ví dụ: "Họp Giao Ban Công Ty lúc 9:00 sáng Thứ Hai hàng tuần").
*   Khi người dùng xem lịch biểu của **tuần này** hoặc **tháng này**, hệ thống cần tìm ra tất cả các sự kiện diễn ra trong khoảng thời gian đó.

Nếu hệ thống lưu trữ luật lặp lại dưới dạng một chuỗi cấu hình (như RFC 5545 RRULE: `FREQ=WEEKLY;BYDAY=MO`), việc thực hiện query trực tiếp trên SQL sẽ cực kỳ chậm và phức tạp vì hệ thống phải parse logic cho mọi bản ghi để tính ngày lặp lại.

---

## 2. Giải pháp: Pre-generated Time Slots (Tạo sẵn các bản ghi thời gian)

Thay vì tính toán lịch lặp lại trong thời gian chạy (Runtime Calculation), chúng ta **sinh sẵn trước các slot thời gian biểu cụ thể (Time Slots/Occurrences)** trong một khoảng thời gian nhất định (ví dụ: sinh trước 1 năm hoặc 10 tuần).

### Thiết kế Database
*   **Bảng chính `events`:** Lưu cấu hình sự kiện gốc (Tiêu đề, mô tả, chu kỳ lặp lại).
*   **Bảng phụ `time_slots`:** Lưu chi tiết từng phiên/buổi diễn ra thực tế.

```sql
CREATE TABLE time_slots (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  begin_local_time DATETIME NOT NULL,
  end_local_time DATETIME NOT NULL,
  timezone_id VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL, -- ACTIVE, CANCELLED
  FOREIGN KEY (event_id) REFERENCES events(id)
);
```

### Ưu điểm vượt trội
1. **Truy vấn siêu tốc ($O(1)$/Index-friendly):** Câu query xem lịch tuần vô cùng đơn giản:
   ```sql
   SELECT * FROM time_slots 
   WHERE begin_local_time BETWEEN :start AND :end;
   ```
2. **Khả năng chỉnh sửa slot độc lập:** Nếu một buổi họp cụ thể trong chuỗi lặp bị hủy hoặc đổi giờ (ví dụ: tuần sau trùng lịch nghỉ lễ nên dời từ thứ Hai sang thứ Ba), ta chỉ việc cập nhật bản ghi `TimeSlot` cụ thể đó mà không ảnh hưởng tới toàn bộ cấu hình sự kiện lặp lại gốc.

---

## 3. Hướng dẫn thử nghiệm & Kiểm thử

### Bước 1: Xem lịch biểu tuần tới (Đã seed sẵn dữ liệu mẫu)
Gọi API lấy tất cả các slot thời gian biểu trong 2 tuần tới (lưu ý thay đổi `start` và `end` tương ứng với ngày hiện tại của bạn):
```bash
# Thay thế yyyy-MM-ddTHH:mm:ss bằng ngày thực tế
curl -s "http://localhost:8080/api/events/slots?start=2026-05-25T00:00:00&end=2026-06-08T23:59:59"
```
*   **Kết quả:** Bạn sẽ thấy 1 slot của "Họp Dự án A" (không lặp lại) và các slot của "Họp Giao Ban Công Ty" (lặp lại vào thứ Hai hàng tuần).

### Bước 2: Tạo sự kiện lặp lại mới hàng tuần
Tạo sự kiện "Học Tiếng Anh" vào tối thứ Tư hàng tuần lúc 18:30:
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"title": "Học Tiếng Anh hàng tuần", "description": "Lớp giao tiếp", "recurrence": "WEEKLY", "startDateTime": "2026-05-27T18:30:00", "durationMinutes": 90}'
```
*   **Kết quả:** Trả về thông tin Event vừa tạo. Đồng thời kiểm tra database hoặc gọi API `/api/events/slots` cho thấy 10 slot của lớp học đã được tự động sinh sẵn cho 10 tuần liên tiếp.

### Bước 3: Chỉnh sửa hoặc Hủy một slot lịch cụ thể
Khách hàng hoặc người chủ trì muốn hủy buổi học ngày `2026-06-03` (Slot ID 2):
```bash
curl -X PUT "http://localhost:8080/api/events/slots/2?status=CANCELLED"
```
Hoặc muốn dời giờ học của slot 3 sang 19:00 thay vì 18:30:
```bash
curl -X PUT "http://localhost:8080/api/events/slots/3?newBegin=2026-06-10T19:00:00&newEnd=2026-06-10T20:30:00"
```
*   **Kết quả:** Các slot khác của chuỗi lặp vẫn được giữ nguyên giờ cũ và trạng thái `ACTIVE`. Điều này chứng tỏ sự linh hoạt vượt trội của thiết kế Pre-generated Time Slots.
