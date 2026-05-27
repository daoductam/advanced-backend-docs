# 🧪 HƯỚNG DẪN THỰC HÀNH & NHẬT KÝ THIẾT KẾ ĐA NGÔN NGỮ (MULTIPLE LANGUAGES JSON)

Tài liệu này được thiết kế để bạn ghi lại quá trình thực hành, thử nghiệm và kiểm tra hiệu năng/truy vấn của thiết kế Đa ngôn ngữ (Multiple Languages) bằng cột JSON trong MySQL dựa trên hướng dẫn tại [README.md](file:///d:/backend_docs/7.data-modeling/README.md).

---

## 🛠️ Bước Chuẩn Bị: Khởi chạy dự án
Ứng dụng Spring Boot đã tích hợp sẵn cơ chế tự động kết nối và tạo cơ sở dữ liệu `db_data_modeling_demo` (nếu chưa tồn tại) thông qua thuộc tính `createDatabaseIfNotExist=true` trong chuỗi kết nối. Ngoài ra, lớp `DataSeeder` cũng tự động chèn sẵn dữ liệu mẫu khi khởi động.

Hãy mở terminal trong thư mục `d:\backend_docs\7.data-modeling\data-modeling\` và chạy lệnh:
```bash
.\mvnw spring-boot:run
```

Sau khi ứng dụng khởi chạy thành công, bạn cũng có thể truy cập giao diện trực quan **Swagger UI** để thực hành gọi API trực tiếp trên trình duyệt tại:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

---

## 📝 NHẬT KÝ THỰC HÀNH CỦA BẠN (Ghi chép kết quả vào đây)

*Hãy chạy thử nghiệm và điền kết quả đo đạc từ môi trường của bạn vào các mục bên dưới.*

### 🧪 BÀI THỰC HÀNH 1: Kiểm tra cấu trúc bảng & Dữ liệu mẫu (Data Seeder)

#### 1. Kiểm tra trực tiếp cấu trúc bảng trong MySQL Client:
Hãy mở MySQL Client của bạn (DBeaver, Navicat, Workbench...) và chạy lệnh:
```sql
DESCRIBE db_data_modeling_demo.articles;
```
*   **Kiểu dữ liệu của cột `title_translations`:** `................` (Ví dụ: `json`)

#### 2. Gọi API lấy danh sách bài viết để kiểm thử dữ liệu trả về:
```bash
curl http://localhost:8080/api/articles
```
*   **Danh sách các bản ghi nhận được (Kết quả JSON):**
```json
[
  // Copy kết quả trả về của bạn vào đây
]
```

---

### 🧪 BÀI THỰC HÀNH 2: Tìm kiếm theo giá trị nằm sâu trong trường JSON

Chúng ta sẽ kiểm tra chức năng tìm kiếm thông tin bài viết theo ngôn ngữ cụ thể được trích xuất trực tiếp từ cột JSON.

#### 📋 Câu truy vấn 2.1 (Tìm kiếm theo tiếng Anh - English):
Hãy gọi API sau từ terminal:
```bash
curl "http://localhost:8080/api/articles/search?lang=en&title=Software%20Engineer"
```
*   **Kết quả bài viết nhận được (Có đúng "Software Engineer" không?):** `................`
*   **Câu lệnh SQL do Hibernate sinh ra dưới Console (Hãy kiểm tra terminal chạy Spring Boot):**
```sql
-- Copy câu lệnh SQL log ra ở đây
```

#### 📋 Câu truy vấn 2.2 (Tìm kiếm theo tiếng Việt - Vietnamese):
Hãy gọi API sau từ terminal:
```bash
curl "http://localhost:8080/api/articles/search?lang=vi&title=K%E1%BB%B9%20S%C6%B0%20Ph%E1%BA%A7n%20M%E1%BB%81m"
```
*   **Kết quả bài viết nhận được:** `................`

---

### 🧪 BÀI THỰC HÀNH 3: Chứng minh tính linh hoạt (Thêm Ngôn ngữ mới không cần Sửa Lược đồ)

Một yêu cầu thực tế phát sinh: **Thêm bản dịch Tiếng Nhật (`jp`)** và **Tiếng Pháp (`fr`)** cho bài viết mới mà hoàn toàn không thay đổi bảng `articles`.

#### 1. Gửi request POST tạo bài viết chứa ngôn ngữ mới:
```bash
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -d "{\"titleTranslations\": {\"vi\": \"Nhà Phát Triển Backend\", \"en\": \"Backend Developer\", \"jp\": \"バックエンド開発者\", \"fr\": \"Développeur Backend\"}, \"status\": \"ACTIVE\"}"
```

#### 2. Thử nghiệm tìm kiếm bằng tiếng Nhật (`jp`) vừa thêm mới:
```bash
curl "http://localhost:8080/api/articles/search?lang=jp&title=%E3%83%90%E3%83%83%E3%82%AF%E3%82%A8%E3%83%B3%E3%83%89%E9%96%8B%E7%99%BA%E8%80%85"
```
*   **Có tìm thấy bài viết vừa tạo không?** `................` (Có / Không)
*   **Giải thích tại sao thiết kế JSON lại làm được việc này một cách dễ dàng:**
    `..........................................................................................`
    `..........................................................................................`
