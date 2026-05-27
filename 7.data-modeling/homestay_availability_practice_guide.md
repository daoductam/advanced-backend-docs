# Hướng dẫn thực hành: Đặt phòng Homestay (Homestay Availability Booking)

Tài liệu này giải thích chi tiết phương pháp thiết kế cơ sở dữ liệu và triển khai backend cho bài toán kiểm tra phòng trống và cấu hình giá linh hoạt theo từng ngày của Homestay (Room Availability & Dynamic Pricing).

---

## 1. Bài toán Đặt phòng Homestay

Trong các hệ thống Booking truyền thống (khách sạn, homestay, vé máy bay), chúng ta thường đối mặt với 2 vấn đề lớn:
1. **Kiểm tra phòng trống trong khoảng thời gian:** Xem phòng có trống từ ngày A đến ngày B không. Nếu dùng bảng `bookings` lưu `checkin_date` và `checkout_date`, ta phải viết câu lệnh query so sánh khoảng chồng lấn (Overlapping Interval Query). Câu query này sẽ ngày càng chậm khi số lượng đặt phòng tăng lên.
2. **Thay đổi giá linh hoạt (Dynamic Pricing):** Giá phòng thường không cố định. Cuối tuần thường đắt hơn ngày thường; mùa du lịch hoặc lễ Tết giá tăng mạnh. Nếu chỉ lưu một trường `price` tĩnh trong bảng `rooms`/`homestays`, ta không thể cấu hình giá động theo ngày.

---

## 2. Giải pháp: Pre-allocation (Tạo sẵn dòng theo ngày)

Thay vì tính toán động các khoảng thời gian chồng chéo từ danh sách Booking, ta **chia nhỏ thời gian thành các đơn vị ngày (daily slots)**.

### Thiết kế Database
*   **Bảng chính `homestays`:** Lưu thông tin cơ bản.
*   **Bảng lịch `homestay_availabilities`:** Mỗi dòng đại diện cho trạng thái và giá của **một homestay vào một ngày cụ thể**.

```sql
CREATE TABLE homestay_availabilities (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  homestay_id BIGINT NOT NULL,
  booking_date DATE NOT NULL,
  price DECIMAL(12, 2) NOT NULL,
  status VARCHAR(20) NOT NULL, -- AVAILABLE, BOOKED, BLOCKED
  UNIQUE KEY unique_homestay_date (homestay_id, booking_date)
);
```

### Cơ chế hoạt động
1. **Tự động tạo trước:** Khi chủ nhà tạo mới một Homestay, hệ thống tự động sinh trước **365 dòng** (tương ứng 365 ngày tiếp theo).
2. **Kiểm tra phòng trống (SELECT):** Lấy danh sách trạng thái của homestay đó trong khoảng ngày khách chọn. Nếu tất cả các ngày đều có `status = 'AVAILABLE'`, phòng trống!
3. **Đặt phòng (UPDATE):** Cập nhật trạng thái các ngày từ `AVAILABLE` sang `BOOKED` trong một Transaction. Nếu có bất kỳ ngày nào không trống, Transaction sẽ rollback để tránh overbooking.
4. **Cập nhật giá động:** Chỉ cần chạy 1 lệnh UPDATE giá của ngày muốn thay đổi.

---

## 3. Mã nguồn triển khai

### A. Thực thể (Entities)
*   **`Homestay.java`**: Lưu trữ thông tin cơ bản của Homestay và giá mặc định (`defaultPrice`).
*   **`HomestayAvailability.java`**: Lưu lịch ngày, giá động, và trạng thái phòng trống. Chứa Unique Constraint `(homestay_id, booking_date)` để bảo vệ dữ liệu ở mức DB.

### B. Controller (`HomestayController.java`)
Cung cấp các API:
*   `POST /api/homestays`: Tạo mới Homestay và pre-allocate 365 ngày trống.
*   `GET /api/homestays/{id}/availability`: Xem lịch và giá theo khoảng ngày.
*   `PUT /api/homestays/{id}/price`: Cập nhật giá một ngày bất kỳ.
*   `POST /api/homestays/{id}/book`: Đặt phòng (chuyển trạng thái sang `BOOKED` nếu tất cả các ngày trống).

---

## 4. Hướng dẫn thử nghiệm & Kiểm thử

### Bước 1: Xem lịch trống & Giá mặc định (Đã seed sẵn 30 ngày)
Gọi API lấy lịch trong vòng 10 ngày tới (lưu ý chỉnh ngày phù hợp với thời gian hiện tại của bạn):
```bash
# Thay thế yyyy-MM-dd bằng ngày thực tế
curl "http://localhost:8080/api/homestays/1/availability?startDate=2026-05-27&endDate=2026-06-05"
```
*   **Kết quả:** Bạn sẽ thấy giá của các ngày Thứ 7 và Chủ Nhật cao hơn 20% so với ngày thường nhờ cơ chế tính toán trong `DataSeeder.java`.

### Bước 2: Cập nhật giá động cho ngày cụ thể
Chủ nhà muốn tăng giá ngày `2026-06-01` lên `990,000 VND`:
```bash
curl -X PUT "http://localhost:8080/api/homestays/1/price?date=2026-06-01&price=990000"
```
*   Kiểm tra lại lịch availability của ngày đó để xác nhận giá đã đổi.

### Bước 3: Thực hiện đặt phòng thành công
Khách đặt phòng 3 ngày từ `2026-05-28` đến `2026-05-30`:
```bash
curl -X POST "http://localhost:8080/api/homestays/1/book?startDate=2026-05-28&endDate=2026-05-30"
```
*   **Kết quả mong đợi:** Trả về thông báo đặt phòng thành công và tính tổng số tiền tự động bằng cách cộng dồn giá của từng ngày cụ thể.

### Bước 4: Thử nghiệm Overbooking (Trùng phòng)
Thử đặt phòng gối đầu hoặc trùng ngày đã đặt ở trên (ví dụ đặt từ `2026-05-29` đến `2026-06-01`):
```bash
curl -X POST "http://localhost:8080/api/homestays/1/book?startDate=2026-05-29&endDate=2026-06-01"
```
*   **Kết quả mong đợi:** Hệ thống trả về lỗi `400 Bad Request` thông báo phòng không khả dụng vào ngày `2026-05-29` (hoặc `2026-05-30`) vì trạng thái của ngày đó đã là `BOOKED`.

---

## 5. Đánh giá Ưu điểm & Nhược điểm của giải pháp Pre-allocation

| Tiêu chí | Giải pháp Overlap Query (Tĩnh) | Giải pháp Pre-allocation (Động) |
| :--- | :--- | :--- |
| **Tốc độ truy vấn** | Chậm dần theo thời gian khi lượng booking tăng | Cực kỳ nhanh ($O(N)$ với $N$ là số ngày đặt, thường $N \le 30$) |
| **Độ phức tạp truy vấn** | Cao (so sánh chéo khoảng thời gian) | Thấp (chỉ là SELECT ... BETWEEN đơn giản) |
| **Dynamic Pricing** | Rất phức tạp (cần bảng cấu hình riêng biệt và logic kết hợp phức tạp) | Cực kỳ đơn giản (mỗi ngày là một bản ghi lưu giá trực tiếp) |
| **Dung lượng lưu trữ** | Nhẹ (chỉ lưu các đơn đặt thực tế) | Tốn tài nguyên hơn (phải tạo trước 365 bản ghi/năm cho mỗi homestay) |
| **Phạm vi áp dụng** | Thích hợp cho các thực thể số lượng rất lớn nhưng ít thay đổi giá động | Thích hợp nhất cho Homestay, Khách sạn, Phòng họp, Xe tự lái |
