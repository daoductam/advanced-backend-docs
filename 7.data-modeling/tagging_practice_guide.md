# Hướng dẫn thực hành: Quản lý nhãn dán (Tagging / Labeling)

Tài liệu này hướng dẫn chi tiết cách quản lý thẻ (Tags) cho thực thể bằng cách sử dụng kiểu dữ liệu JSON Array và cách tối ưu hóa hiệu năng bằng chỉ mục đa trị (Multi-Valued Index) trong MySQL.

---

## 1. Bài toán Quản lý nhãn dán (Tagging)

Bài viết (Article/Post) có thể có nhiều thẻ (Tags). Các thao tác truy vấn thường thấy:
*   **Truy vấn 1 (Lấy toàn bộ thẻ của bài viết):** Xuất hiện liên tục mỗi khi hiển thị chi tiết hoặc danh sách bài viết.
*   **Truy vấn 2 (Lấy toàn bộ bài viết theo một thẻ):** Dùng khi người dùng bấm vào một nhãn (ví dụ: tag "Backend") để lọc danh sách.

Yêu cầu thực hành đòi hỏi tối ưu cho **Truy vấn 1** chạy siêu nhanh mà không cần JOIN bảng trung gian nhiều-nhiều (N-M), đồng thời **Truy vấn 2** vẫn phải nhanh bằng cách sử dụng cơ chế chỉ mục thích hợp.

---

## 2. Giải pháp: Lưu JSON Array trực tiếp + Multi-Valued Index

### Lưu JSON Array
Chúng ta lưu trữ danh sách nhãn dán trực tiếp trong trường `tags` kiểu `JSON` dưới dạng một mảng các chuỗi, ví dụ: `["Java", "Backend", "Spring Boot"]` ngay trong bảng `articles`.

*   **Truy vấn 1:** Đạt tốc độ tối đa $O(1)$ vì các nhãn dán nằm ngay trong bản ghi bài viết. Không cần thực hiện JOIN.
*   **Truy vấn 2:** Để tránh Table Scan khi lọc theo tag, ta sử dụng tính năng **Multi-Valued Index** của MySQL.

### Thiết lập Multi-Valued Index trong MySQL
Với cột JSON chứa mảng, chỉ mục thông thường B-Tree không hoạt động. MySQL cung cấp cơ chế tạo chỉ mục trên các giá trị nằm bên trong mảng JSON bằng cú pháp:
```sql
ALTER TABLE articles ADD INDEX idx_tags ( (CAST(tags AS CHAR(32) ARRAY)) );
```
Cú pháp này sẽ trích xuất từng phần tử trong mảng `tags` ra và lưu vào B-Tree index riêng, giúp tìm kiếm theo phần tử mảng cực kỳ nhanh.

### Cách truy vấn trong JPA/SQL
Để tìm kiếm các bài viết chứa tag `:tag`, ta sử dụng toán tử native `MEMBER OF` của MySQL:
```sql
SELECT * FROM articles WHERE :tag MEMBER OF (tags);
```
Database Engine sẽ tự động nhận diện và sử dụng index `idx_tags` để lọc bản ghi, tránh hoàn toàn việc scan toàn bộ bảng.

---

## 3. Hướng dẫn thử nghiệm & Kiểm thử

### Bước 1: Xem danh sách bài viết mẫu đã seed kèm tags
```bash
curl http://localhost:8080/api/articles
```
*   **Kết quả:** Bạn sẽ thấy trường `tags` chứa mảng các chuỗi tương ứng với mỗi bài viết (Ví dụ bài 1 chứa `"Java", "Backend", "Spring Boot"`).

### Bước 2: Tìm kiếm bài viết theo nhãn dán (Tag)
Tìm kiếm các bài viết có chứa nhãn dán `"Backend"`:
```bash
curl "http://localhost:8080/api/articles/by-tag?tag=Backend"
```
*   **Kết quả mong đợi:** Trả về Bài viết 1 và Bài viết 3 vì cả hai đều chứa tag `"Backend"` trong trường `tags`.

### Bước 3: Tạo bài viết mới kèm theo Tags
```bash
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -d "{\"titleTranslations\": {\"vi\": \"Lập trình Python\", \"en\": \"Python Programming\"}, \"tags\": [\"Python\", \"Programming\", \"Backend\"], \"status\": \"ACTIVE\"}"
```
*   Sau khi tạo, thử gọi lại API ở Bước 2 để kiểm tra xem bài viết mới có tự động xuất hiện trong bộ lọc tag `"Backend"` không.

---

## 4. Phân tích so sánh hiệu năng

| Tiêu chí | Sử dụng Bảng Trung gian (N-M JOIN) | Sử dụng JSON Array + Multi-Valued Index |
| :--- | :--- | :--- |
| **Lấy tags của bài viết** | Phức tạp (Cần JOIN 3 bảng) | Siêu nhanh ($O(1)$ - Không cần JOIN) |
| **Tìm bài viết theo tag** | Tận dụng index B-Tree chuẩn trên khóa ngoại | Tận dụng Multi-Valued Index của MySQL 8.0+ |
| **Thêm/Sửa tags** | Cần INSERT/DELETE trên bảng trung gian | Chỉ cần UPDATE 1 trường JSON trên dòng của bài viết |
| **Tính toàn vẹn dữ liệu** | Rất cao nhờ ràng buộc khóa ngoại (Foreign Key) | Thấp hơn (do dữ liệu tag lưu dạng chuỗi tự do trong JSON) |
