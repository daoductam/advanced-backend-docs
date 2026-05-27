# Hướng Dẫn Thực Hành: Mã Hóa Dữ Liệu Cá Nhân & Chỉ Mục Mù (Personal Data Encryption & Blind Index)

Tài liệu này hướng dẫn bạn thực hành chi tiết về **Case Study 2: Mã hóa dữ liệu cá nhân (Personal Data Encryption)** dựa trên lý thuyết tại [README.md](file:///d:/backend_docs/8.security/README.md).

---

## 1. Lý Thuyết Cốt Lõi (Core Theory)

### 1.1. Mã hóa dữ liệu ở đâu? (Where to Encrypt?)
Chúng ta lựa chọn mã hóa ở **tầng Backend (Server-Side)**:
*   Mã hóa và giải mã được xử lý trực tiếp trên ứng dụng Java trước khi lưu xuống CSDL hoặc khi trả dữ liệu về.
*   Thuật toán khuyến nghị: **AES-256-GCM** (đây là thuật toán mã hóa đối xứng cực kỳ an toàn, có hỗ trợ kiểm tra tính toàn vẹn của dữ liệu mã hóa để tránh tấn công giả mạo).

### 1.2. Định dạng lưu trữ: Binary vs Base64 String
*   Dữ liệu mã hóa nhị phân được lưu trữ dưới dạng byte thô (**Binary**). Trong JPA, trường được khai báo là `byte[]` và đánh dấu bằng `@Lob`.
*   Việc này giúp tối ưu hóa dung lượng lưu trữ tốt hơn ~33% so với việc chuyển sang dạng chuỗi Base64 trước khi lưu.

### 1.3. Thách thức: Tìm kiếm dữ liệu đã mã hóa
Khi dữ liệu (như email `"test@gmail.com"`) bị mã hóa bằng AES-256-GCM với một vector khởi tạo ngẫu nhiên (IV), mỗi lần băm sẽ sinh ra một chuỗi nhị phân khác nhau hoàn toàn. Do đó, chúng ta **không thể** thực hiện truy vấn bằng câu lệnh SQL thông thường:
```sql
SELECT * FROM users WHERE email = 'test@gmail.com' -- KHÔNG HOẠT ĐỘNG!
```

### 1.4. Giải pháp: Chỉ Mục Mù (Blind Index)
Để giải quyết bài toán tìm kiếm, ta áp dụng kỹ thuật **Blind Index**:
1.  **Cột phụ**: Tạo thêm cột `email_blind_index` trong CSDL và đánh index thông thường.
2.  **Hàm băm bảo mật (HMAC-SHA256)**: Khi lưu trữ, ta tính toán một giá trị băm cố định dựa trên khóa bí mật và email gốc:
    $$\text{email\_blind\_index} = \text{HMAC-SHA256}(\text{"test@gmail.com"}, \text{BlindIndexKey})$$
3.  **Tìm kiếm**: Khi tìm kiếm người dùng theo email, ta tính toán HMAC-SHA256 của email cần tìm rồi chạy truy vấn SQL:
    ```sql
    SELECT * FROM users WHERE email_blind_index = 'Chuỗi-HMAC-Tính-Được'
    ```
4.  **Kết quả**: Tìm kiếm chính xác với độ phức tạp $O(1)$ mà CSDL không bao giờ biết được email thực sự là gì.

---

## 2. Các File Mã Nguồn Đã Triển Khai

1.  **[EncryptionService.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/service/EncryptionService.java)**:
    *   Sử dụng thuật toán `AES/GCM/NoPadding` từ JCA (Java Cryptography Architecture).
    *   Tự động sinh ngẫu nhiên 12-byte IV cho mỗi lần mã hóa và ghép IV vào trước ciphertext.
    *   Hàm `generateBlindIndex(...)` chuẩn hóa chuỗi đầu vào (lowercase, trim) và sinh mã HMAC-SHA256.
2.  **[User.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/entity/User.java)**:
    *   Chứa cột `emailEncrypted` (`byte[]`) và `emailBlindIndex` (`String`).
    *   Chứa cột `phoneEncrypted` (`byte[]`) và `phoneBlindIndex` (`String`).
3.  **[UserServiceImpl.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/service/UserServiceImpl.java)**:
    *   Khi `register`: Băm mật khẩu, mã hóa email/phone bằng AES, đồng thời sinh blind index để lưu trữ.
    *   Khi `login` hoặc `searchByEmail`: Tính toán blind index từ email tìm kiếm, truy vấn trong DB và giải mã (`decrypt`) dữ liệu nhị phân về plaintext để trả về cho người dùng.
4.  **[UserController.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/controller/UserController.java)**:
    *   Expose API tìm kiếm: `GET /api/users/search?email=...`

---

## 3. Thực Hành Kiểm Chứng API

### Bước 1: Khởi chạy Spring Boot
Mở Terminal tại thư mục `d:\backend_docs\8.security\security\` và khởi động server:
```bash
.\mvnw spring-boot:run
```

### Bước 2: Đăng ký tài khoản (kèm Email và Phone)
Sử dụng cURL gửi request đăng ký:

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"khiem\", \"password\": \"securePass456!\", \"displayName\": \"Nguyen Khiem\", \"email\": \"khiem@gmail.com\", \"phone\": \"0987654321\"}"
```

**Kết quả trả về (Response):**
```json
{
  "id": 2,
  "username": "khiem",
  "displayName": "Nguyen Khiem",
  "passwordHash": "$argon2id$v=19$...",
  "email": "khiem@gmail.com",
  "phone": "0987654321",
  "emailEncryptedBase64": "v+x0qS7N4K4/vXm3h6D9...",
  "emailBlindIndex": "a8fHj1lQ9d2pKszm...",
  "statusMessage": "User registered successfully! Personal data encrypted, Blind Index generated."
}
```
> [!IMPORTANT]
> Lưu ý rằng trong cơ sở dữ liệu bảng `users`, cột `email_encrypted` đang lưu trữ một chuỗi nhị phân (chính là byte gốc của chuỗi Base64 `"emailEncryptedBase64"` kia). Cột `email_blind_index` lưu chuỗi hash HMAC đại diện.

---

### Bước 3: Tìm kiếm người dùng bằng Blind Index
Gửi yêu cầu tìm kiếm người dùng theo email plaintext gốc:

```bash
curl -G http://localhost:8080/api/users/search --data-urlencode "email=khiem@gmail.com"
```

**Kết quả trả về:**
```json
{
  "id": 2,
  "username": "khiem",
  "displayName": "Nguyen Khiem",
  "passwordHash": "$argon2id$v=19$...",
  "email": "khiem@gmail.com",
  "phone": "0987654321",
  "emailEncryptedBase64": "v+x0qS7N4K4/vXm3h6D9...",
  "emailBlindIndex": "a8fHj1lQ9d2pKszm...",
  "statusMessage": "User found! Decrypted data successfully via Blind Index search."
}
```
> [!NOTE]
> Hệ thống tính toán Blind Index của `"khiem@gmail.com"` thành `"a8fHj1lQ9d2pKszm..."`, sau đó dùng chuỗi này để truy vấn CSDL và tìm thấy bản ghi cực kỳ nhanh chóng mà không cần giải mã tất cả các hàng dữ liệu trong DB!
