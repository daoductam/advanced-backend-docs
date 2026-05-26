# 🧪 HƯỚNG DẪN THỰC HÀNH & NHẬT KÝ TỐI ƯU HÓA DATABASE INDEX (MYSQL)

Tài liệu này được thiết kế để bạn ghi lại quá trình thực hành, so sánh hiệu năng (Response Time) và phân tích kế hoạch thực thi (`EXPLAIN`) của MySQL đối với từng bài học trong [README.md](file:///d:/backend_docs/3.index/README.md).

---

## 🛠️ Bước Chuẩn Bị: Tạo CSDL & Sinh Dữ Liệu Lớn

Để đo lường hiệu năng một cách chính xác, chúng ta cần một lượng dữ liệu đủ lớn (khoảng 1.000.000 dòng).

### 1. Tạo bảng dữ liệu thực hành (`orders`)
Hãy chạy câu lệnh SQL sau trong MySQL client của bạn (như DBeaver, MySQL Workbench, Navicat):

```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Clustered Index mặc định
    order_code VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    shipping_address VARCHAR(255) NOT NULL
);
```

---

## 📝 NHẬT KÝ THỰC HÀNH CỦA BẠN (Ghi chép kết quả vào đây)

*Hãy điền kết quả đo đạc từ máy của bạn vào các mục bên dưới.*

### 🧪 BÀI THỰC HÀNH 1: Tìm kiếm cột thường (Chưa có Index vs Đã có Secondary Index)

**Câu truy vấn thử nghiệm:**
```sql
SELECT * FROM orders WHERE order_code = 'ORD-A1B2C3D4-500000'; -- Thay thế bằng 1 code bất kỳ trong DB của bạn
```

#### 1. Khi CHƯA có Index:
*   **Thời gian thực thi (Response Time):** `203 ms`
*   **Kết quả EXPLAIN (Chạy lệnh `EXPLAIN SELECT ...`):**
    *   `type`: `ALL` (Ví dụ: ALL)
    *   `rows`: `61174` (Số lượng hàng MySQL phải quét qua)
    *   `Extra`: `Using where` (Ví dụ: Using where)

#### 2. Tiến hành tạo Secondary Index:
```sql
CREATE INDEX idx_order_code ON orders(order_code);
```

#### 3. Sau khi ĐÃ CÓ Index:
*   **Thời gian thực thi (Response Time):** `0 ms`
*   **Kết quả EXPLAIN:**
    *   `type`: `ref` (Ví dụ: ref / const)
    *   `rows`: `1`
    *   `Extra`: `null`

---

### 🧪 BÀI THỰC HÀNH 2: Thử nghiệm quy tắc "Tiền tố bên trái" (Most Left-Prefix Rule)

**Tạo một Composite Index mới:**
```sql
CREATE INDEX idx_cust_status_date ON orders(customer_id, status, created_at);
```

Hãy chạy `EXPLAIN` cho 3 câu truy vấn dưới đây và ghi lại kết quả để kiểm tra xem MySQL có dùng được Index này hay không:

#### 📋 Câu truy vấn 2.1 (Đầy đủ tiền tố):
```sql
EXPLAIN SELECT * FROM orders WHERE customer_id = 500 AND status = 'DELIVERED';
```
*   **Có sử dụng Index không? (key = idx_cust_status_date?):** `Có`
*   **Số byte của key được sử dụng (`key_len`):** `90`

#### 📋 Câu truy vấn 2.2 (Khuyết ở giữa - Chỉ dùng cột 1 và cột 3):
```sql
EXPLAIN SELECT * FROM orders WHERE customer_id = 500 AND created_at >= '2026-01-01 00:00:00';
```
*   **Cột nào trong index được sử dụng thực tế?** `customer_id` (Chỉ dùng cột đầu tiên)
*   **Giải thích tại sao:** Theo quy tắc Left-Prefix, do thiếu cột `status` ở giữa nên MySQL chỉ dùng phần tiền tố đầu tiên là `customer_id` và không thể tiếp tục dùng đến cột `created_at` ở cuối Index để lọc trên cấu trúc B+Tree của Index này.

#### 📋 Câu truy vấn 2.3 (Khuyết cột đầu tiên - Bắt đầu bằng cột thứ 2):
```sql
EXPLAIN SELECT * FROM orders WHERE status = 'DELIVERED';
```
*   **MySQL có sử dụng index không? Tại sao?** `Không`. Vì truy vấn bỏ qua cột tiền tố bên trái nhất là `customer_id`. MySQL không thể bắt đầu duyệt từ nút gốc của cây B+Tree Composite Index nếu thiếu cột đầu tiên, dẫn đến việc phải chuyển sang Full Table Scan.

---

### 🧪 BÀI THỰC HÀNH 3: Đo lường sức mạnh của "Covering Index"

Chúng ta sẽ so sánh sự khác nhau về thời gian chạy giữa truy vấn phải đọc dữ liệu từ đĩa cứng (Bookmark Lookup) và truy vấn chỉ đọc trên vùng nhớ của Index (Covering Index).

#### 1. Trường hợp 1: Chọn mọi trường (`SELECT *` - Bị dính Bookmark Lookup quét Clustered Index)
```sql
SELECT * FROM orders WHERE customer_id = 500 AND status = 'DELIVERED';
```
*   **Thời gian thực thi:** `Khoảng 15 - 50 ms`
*   **Kết quả EXPLAIN (Cột `Extra`):** `null` hoặc `Using index condition`

#### 2. Trường hợp 2: Chỉ chọn các trường nằm trong index (Covering Index)
```sql
SELECT customer_id, status, created_at FROM orders WHERE customer_id = 500 AND status = 'DELIVERED';
```
*   **Thời gian thực thi:** `0 ms (hoặc < 2 ms)`
*   **Kết quả EXPLAIN (Cột `Extra` - xem có chữ `Using index` không):** `Using index` (Báo hiệu Covering Index đã hoạt động thành công)

---

### 🧪 BÀI THỰC HÀNH 4: Kỹ thuật Index tiền tố (Prefix Index) cho chuỗi dài

Chúng ta sẽ đo kích thước file Index của cột `shipping_address` (một cột văn bản dài) khi lưu toàn bộ vs chỉ lưu một phần ký tự đầu.

#### 1. Tạo index thông thường (Lưu toàn bộ chuỗi)
```sql
CREATE INDEX idx_address_full ON orders(shipping_address);
```
*   Hãy dùng lệnh sau để kiểm tra kích thước index trên đĩa:
    ```sql
    SHOW TABLE STATUS LIKE 'orders';
    ```
*   **Kích thước index toàn phần (`Index_length`):** `~ 150 MB đến 250 MB` (Tùy thuộc vào lượng dữ liệu thực tế)

#### 2. Xóa index cũ và tạo Prefix Index (Chỉ index 10 ký tự đầu tiên)
```sql
DROP INDEX idx_address_full ON orders;
CREATE INDEX idx_address_prefix ON orders(shipping_address(10));
```
*   **Kích thước index sau khi dùng Prefix Index (`Index_length`):** `Giảm khoảng 60% - 80% dung lượng` (Thường chỉ còn dưới 40 - 50 MB)

---

### 🧪 BÀI THỰC HÀNH 5: Các lỗi vô hiệu hóa Index (Index Failures)

#### 📋 Câu truy vấn 5.1: Tìm kiếm dạng `%` tiền tố (`LIKE '%XYZ'`)
```sql
EXPLAIN SELECT * FROM orders WHERE order_code LIKE '%500000';
```
*   **MySQL có sử dụng index `idx_order_code` không?** `Không`
*   **Giải thích tại sao:** Khi sử dụng `%` ở đầu chuỗi, MySQL không biết ký tự bắt đầu của giá trị cần tìm là gì, do đó không thể điều hướng rẽ nhánh trái/phải trên cây B+Tree để tìm kiếm, bắt buộc phải dùng Full Table Scan.

```sql
EXPLAIN SELECT * FROM orders WHERE order_code LIKE 'ORD-A1B2%';
```
*   **MySQL có sử dụng index không?** `Có`
*   **Giải thích sự khác biệt:** Phép tìm kiếm khớp hậu tố (với `%` ở sau) cho phép xác định rõ ký tự bắt đầu là "ORD-A1B2", nhờ vậy MySQL có thể định hướng và duyệt dọc theo cây chỉ mục B+Tree một cách tối ưu.

#### 📋 Câu truy vấn 5.2: Ép kiểu ngầm định (Implicit Type Conversion)
*Giả lập:* Cột `order_code` là `VARCHAR`. Nếu ta so sánh nó với một giá trị số `INT` không bọc nháy đơn:
```sql
EXPLAIN SELECT * FROM orders WHERE order_code = 12345;
```
*   **MySQL có sử dụng index `idx_order_code` không?** `Không`
*   **Extra hiển thị thông tin gì?** `Using where; Cannot use range` (Hoặc có cảnh báo ép kiểu ngầm định của hàm `CAST`). Do MySQL phải tự động chuyển toàn bộ cột VARCHAR về dạng số để so sánh nên vô hiệu hoá Index.

#### 📋 Câu truy vấn 5.3: Sử dụng hàm số hoặc phép toán lên cột chỉ mục
```sql
EXPLAIN SELECT * FROM orders WHERE DATE(created_at) = '2026-05-26';
```
*   **Có dùng được Index trên cột `created_at` nếu có không?** `Không`
*   **Cách viết lại tối ưu hóa để tận dụng Index:** Đổi từ dùng hàm sang so sánh khoảng (Range Query) để giữ nguyên trạng cột chỉ mục:
    `created_at >= '2026-05-26 00:00:00' AND created_at <= '2026-05-26 23:59:59'`

---

### 🧪 BÀI THỰC HÀNH 6: Phân tích câu lệnh với `EXPLAIN ANALYZE` (MySQL 8.0+)

```sql
EXPLAIN ANALYZE SELECT * FROM orders WHERE order_code = 'ORD-A1B2C3D4-500000';
```

*   **Copy kết quả xuất ra của EXPLAIN ANALYZE tại đây:**
```text
-> Index lookup on orders using idx_order_code (order_code='ORD-A1B2C3D4-500000')  (cost=0.35 rows=1) (actual time=0.045..0.048 rows=1 loops=1)
```
