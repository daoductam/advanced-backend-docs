# Hướng Dẫn Thực Hành: Tấn Công Phổ Biến & Cách Phòng Chống (Common Attacks & Defenses)

Tài liệu này hướng dẫn bạn thực hành các phương pháp tấn công phổ biến gồm **SQL Injection (SQLi)**, **Cross-Site Scripting (XSS)**, **CSRF** và **Rate Limiting** dựa trên lý thuyết tại [README.md](file:///d:/backend_docs/8.security/README.md).

---

## 1. Tấn Công Tiêm Lệnh SQL (SQL Injection - SQLi)

### 1.1. Cơ chế & Mã nguồn
Khi lập trình viên cộng chuỗi đầu vào trực tiếp vào câu lệnh SQL, CSDL sẽ biên dịch và thực thi chuỗi đó dưới dạng mã lệnh thay vì dữ liệu.

*   **Vulnerable Controller ([VulnerabilityDemoController.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/controller/VulnerabilityDemoController.java))**:
    ```java
    // NGUY HIỂM: Cộng chuỗi trực tiếp đầu vào của client
    String sqlQuery = "SELECT * FROM users WHERE username = '" + username + "'";
    List<User> results = entityManager.createNativeQuery(sqlQuery, User.class).getResultList();
    ```

*   **Secure Controller ([VulnerabilityDemoController.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/controller/VulnerabilityDemoController.java))**:
    ```java
    // AN TOÀN: Sử dụng Parameterized Queries (Ràng buộc tham số)
    String sqlQuery = "SELECT * FROM users WHERE username = :username";
    List<User> results = entityManager.createNativeQuery(sqlQuery, User.class)
            .setParameter("username", username)
            .getResultList();
    ```

---

### 1.2. Thực hành kiểm chứng SQLi

Khởi động dự án của bạn bằng lệnh:
```bash
.\mvnw spring-boot:run
```

#### A. Thực hiện tấn công thử nghiệm trên API bị lỗi (Vulnerable):
Gửi yêu cầu tìm kiếm bằng cách truyền vào chuỗi SQL Injection phá vỡ logic (`' or 1=1; --`):

```bash
# Lưu ý: Chuỗi truyền vào là: ' or 1=1; --
curl -G http://localhost:8080/api/vuln/sqli --data-urlencode "username=' or 1=1; --"
```

**Kết quả:** Hệ thống sẽ trả về **danh sách toàn bộ người dùng** trong CSDL H2 thay vì trả về rỗng. Điều kiện `1=1` luôn đúng và ký tự `--` đã chú thích (vô hiệu hóa) phần kiểm tra phía sau.

#### B. Thử nghiệm trên API an toàn (Secure):
Chạy lệnh tương tự trên endpoint an toàn:

```bash
curl -G http://localhost:8080/api/secure/sqli --data-urlencode "username=' or 1=1; --"
```

**Kết quả:** Trả về danh sách rỗng `[]`. Hệ thống coi toàn bộ chuỗi `' or 1=1; --` chỉ là một giá trị username thuần túy chứ không biên dịch nó thành lệnh SQL nữa.

---

## 2. Tấn Công Chèn Mã Kịch Bản Chéo Trang (Cross-Site Scripting - XSS)

### 2.1. Cơ chế & Mã nguồn
Khi dữ liệu nhập từ người dùng được trả về hiển thị trực tiếp lên trình duyệt mà không được mã hóa các ký tự đặc biệt như `<`, `>`, `"`. Trình duyệt của các nạn nhân khác sẽ thực thi nó dưới dạng mã Javascript động.

*   **Vulnerable Controller**:
    ```java
    // NGUY HIỂM: Trả nguyên văn chuỗi nhận từ client làm HTML
    return "<html><body><p>Nội dung: <b>" + message + "</b></p></body></html>";
    ```

*   **Secure Controller**:
    ```java
    // AN TOÀN: Mã hóa HTML Entity (HTML Escape) trước khi hiển thị
    String safeMessage = HtmlUtils.htmlEscape(message);
    return "<html><body><p>Nội dung: <b>" + safeMessage + "</b></p></body></html>";
    ```

---

### 2.2. Thực hành kiểm chứng XSS

#### A. Gửi payload XSS lên endpoint bị lỗi (Vulnerable):
Chúng ta gửi chuỗi payload `<script>alert('hacked')</script>`:

```bash
curl -G http://localhost:8080/api/vuln/xss --data-urlencode "message=<script>alert('hacked')</script>"
```

**Kết quả:** Trả về HTML nguyên bản:
```html
<html><body><h3>Đánh giá phản hồi (XSS Vulnerable)</h3><p>Nội dung phản hồi của bạn: <b><script>alert('hacked')</script></b></p></body></html>
```
*(Nếu mở link này trên trình duyệt Chrome/Firefox, một hộp thoại alert "hacked" sẽ nhảy lên lập tức!)*

#### B. Gửi payload XSS lên endpoint an toàn (Secure):
```bash
curl -G http://localhost:8080/api/secure/xss --data-urlencode "message=<script>alert('hacked')</script>"
```

**Kết quả:** Trả về HTML đã được mã hóa ký tự đặc biệt:
```html
<html><body><h3>Đánh giá phản hồi (XSS Secured)</h3><p>Nội dung phản hồi của bạn: <b>&lt;script&gt;alert(&#39;hacked&#39;)&lt;/script&gt;</b></p></body></html>
```
*(Khi mở trên trình duyệt, nó chỉ hiển thị chữ thô là `<script>alert('hacked')</script>` mà hoàn toàn không thực thi script)*

---

## 3. Tấn Công Giả Mạo Yêu Cầu Chéo Trang (CSRF)

### 3.1. Cơ chế & Mã nguồn
Kẻ tấn công lợi dụng việc trình duyệt tự động gửi Session Cookie của người dùng để thực thi các hành động trái phép (ví dụ: chuyển tiền) từ một website độc hại của bên thứ ba.

*   **Vulnerable Controller**:
    ```java
    // NGUY HIỂM: Chỉ kiểm tra Cookie SESSION_ID hợp lệ là cho thực hiện giao dịch chuyển tiền.
    // Nếu người dùng bấm link độc hại từ web khác, trình duyệt vẫn tự gửi Cookie này kèm theo.
    @PostMapping("/vuln/transfer")
    public ResponseEntity<String> transferVulnerable(...) { ... }
    ```

*   **Secure Controller**:
    ```java
    // AN TOÀN: Bắt buộc client phải truyền thêm token bí mật trong Request Header (X-CSRF-TOKEN).
    // Website bên thứ ba (cross-site) không thể đọc được token này để giả mạo.
    if (csrfTokenHeader == null || !csrfTokenHeader.equals(expectedCsrfToken)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Giao dịch thất bại! Phát hiện CSRF.");
    }
    ```

---

### 3.2. Thực hành kiểm chứng CSRF

#### Bước A: Đăng nhập nhận Session Cookie và CSRF Token
Đăng nhập tài khoản `tamdao` đã được tạo sẵn trong cơ sở dữ liệu để nhận Cookie phiên:

```bash
# Lệnh đăng nhập (nhận cookie lưu vào file cookies.txt và lấy CSRF token trong phản hồi)
curl -c cookies.txt "http://localhost:8080/api/auth/login-cookie?username=tamdao"
```
**Kết quả:** Sẽ phản hồi mã session và trả về:
`... CSRF Token của bạn là: CSRF-xxxxxx`

#### Bước B: Mô phỏng tấn công CSRF (Vulnerable)
Mô phỏng cuộc tấn công từ trang web khác bằng cách chỉ gửi Session Cookie (`cookies.txt`) để thực hiện giao dịch mà không có CSRF Token:

```bash
# Gửi giao dịch chỉ dùng Session Cookie (Tương tự cách trình duyệt tự động đính kèm cookie)
curl -b cookies.txt -X POST "http://localhost:8080/api/vuln/transfer?toAccount=attacker&amount=1000"
```
**Kết quả:**
`Giao dịch VULNERABLE hoàn thành! Đã chuyển 1000 từ tài khoản tamdao sang attacker. Số dư còn lại: 9000`
*(Giao dịch thành công mặc dù người dùng không hề chủ động thực hiện từ giao diện chính thức. Rất nguy hiểm!)*

#### Bước C: Kiểm chứng cơ chế phòng thủ CSRF (Secure)
Thử thực hiện giao dịch trên API được bảo vệ:

1.  **Nếu không gửi kèm Header `X-CSRF-TOKEN`**:
    ```bash
    curl -b cookies.txt -X POST "http://localhost:8080/api/secure/transfer?toAccount=attacker&amount=1000"
    ```
    **Kết quả:** Nhận lỗi `403 Forbidden` cùng thông báo:
    `Giao dịch thất bại! Phát hiện tấn công CSRF (CSRF Token không hợp lệ hoặc bị thiếu).`

2.  **Nếu gửi kèm Header `X-CSRF-TOKEN` chính xác** (Thay thế `CSRF-xxxxxx` bằng token bạn nhận được ở Bước A):
    ```bash
    curl -b cookies.txt -H "X-CSRF-TOKEN: CSRF-xxxxxx" -X POST "http://localhost:8080/api/secure/transfer?toAccount=attacker&amount=1000"
    ```
    **Kết quả:** Giao dịch thành công hoàn tất!

---

## 4. Rate Limiting (Phòng Chống DDoS / API Abuse)

### 4.1. Cơ chế & Mã nguồn
Ứng dụng sử dụng một Interceptor để theo dõi số lượng Request gửi lên từ mỗi địa chỉ IP của Client.

*   **Interceptor ([RateLimitInterceptor.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/interceptor/RateLimitInterceptor.java))**:
    Theo dõi IP trong 60 giây. Nếu một IP gửi vượt quá **5 requests/phút**, hệ thống chặn đứng và trả về mã lỗi HTTP **429 Too Many Requests**.

---

### 4.2. Thực hành kiểm chứng Rate Limiting

Gửi liên tục 6 requests đăng nhập hoặc tìm kiếm trong thời gian ngắn (chưa đầy 10 giây):

```bash
# Thực thi request liên tục (5 lần đầu thành công bình thường)
curl -i -X POST http://localhost:8080/api/users/login -H "Content-Type: application/json" -d "{\"username\": \"tamdao\", \"password\": \"mySecretPassword123!\"}"
```

**Khi gọi tới lần thứ 6:** bạn sẽ nhận được phản hồi lỗi ngay lập tức:

```text
HTTP/1.1 429 Too Many Requests
Content-Type: text/plain; charset=UTF-8
Content-Length: 79

Too Many Requests! (Hành động bị giới hạn Rate Limit: tối đa 5 requests/phút)
```

Đồng thời, bạn có thể kiểm tra các HTTP Header được trả về từ server ở các request thành công để thấy trạng thái giới hạn:
*   `X-Rate-Limit-Limit: 5` (Giới hạn tối đa)
*   `X-Rate-Limit-Remaining: 0` (Số request còn lại)
