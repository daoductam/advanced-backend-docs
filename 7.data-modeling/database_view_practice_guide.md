# Hướng dẫn thực hành: Database View (Bảng ảo)

Tài liệu này giải thích cách định nghĩa Database View trong SQL, ánh xạ nó vào một thực thể trong Spring Boot (Read-only Entity), và cách kiểm tra hiệu năng bằng lệnh giải trình `EXPLAIN`.

---

## 1. Khái niệm Database View (Bảng ảo)

Một **View** là một bảng ảo không tự lưu trữ dữ liệu vật lý trên ổ đĩa. Dữ liệu thực sự của View luôn được lấy từ các bảng gốc bên dưới tại thời điểm truy vấn.

### Ưu điểm
1.  **Đơn giản hóa truy vấn phức tạp:** Đóng gói các câu lệnh JOIN nặng nề giữa nhiều bảng, lọc dữ liệu phức tạp thành một "bảng" ảo duy nhất.
2.  **Bảo mật thông tin:** Cấp quyền truy cập cho người dùng/ứng dụng vào View thay vì bảng gốc, từ đó che giấu các cột nhạy cảm (ví dụ: lương, mật khẩu).
3.  **Tái sử dụng cao:** Logic JOIN được viết một lần ở tầng DB và có thể gọi từ nhiều API, service khác nhau.

---

## 2. Giải pháp Ánh xạ View trong Spring Boot

Thông thường, Spring Data JPA ánh xạ thực thể (Entity) với bảng vật lý. Đối với Database View, chúng ta áp dụng các chú thích sau để đảm bảo tối ưu hóa:

### Thực thể Read-Only với `@Immutable`
*   Do View thường chỉ dùng để đọc, chúng ta gắn chú thích `@Immutable` của Hibernate lên thực thể. Chú thích này báo cho JPA biết rằng không cần thực hiện kiểm tra thay đổi trạng thái (dirty checking) và chặn mọi câu lệnh cập nhật (UPDATE/DELETE) giúp cải thiện hiệu năng.
*   Sử dụng `@Table(name = "v_homestay_availability_report")` để liên kết với View.

```java
@Entity
@Table(name = "v_homestay_availability_report")
@Immutable
@Getter
@NoArgsConstructor
public class HomestayAvailabilityReport {
    @Id
    private Long id; // ID của bản ghi lấy từ bảng lịch
    private Long homestayId;
    private String homestayName;
    private LocalDate bookingDate;
    private BigDecimal price;
    private String status;
}
```

---

## 3. Hướng dẫn thử nghiệm & Kiểm thử

### Bước 1: Xem báo cáo tổng hợp từ View ảo
Gửi request truy vấn báo cáo tất cả các Homestay có từ khóa `"Da Lat"` trong tên:
```bash
curl -s "http://localhost:8080/api/reports/homestay-availabilities?name=Da%20Lat"
```
*   **Kết quả:** Trả về danh sách chi tiết ngày trống và giá của homestay đi kèm tên homestay `"Da Lat Pine Hill Homestay"` trực quan mà không cần viết lệnh JOIN ở tầng Java.

### Bước 2: Kiểm tra Execution Plan (Giải trình SQL)
Để đảm bảo Database Engine đang tối ưu truy vấn thông qua cơ chế **MERGE** (gộp câu query của ta với định nghĩa View và tận dụng index bảng gốc), hãy chạy lệnh EXPLAIN trong MySQL Client:
```sql
EXPLAIN SELECT * FROM v_homestay_availability_report WHERE homestay_name LIKE '%Da Lat%';
```
*   **Kết quả mong đợi:** Cột `select_type` hiển thị là `SIMPLE` (không có `DERIVED` hay `TEMPORARY`), cho thấy MySQL đã gộp thành công định nghĩa của View vào câu lệnh SQL chính và thực thi trực tiếp trên các bảng gốc `homestays` và `homestay_availabilities`.
