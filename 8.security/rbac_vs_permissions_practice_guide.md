# Hướng Dẫn Thực Hành: Phân Quyền RBAC vs Permission-Based AC

Tài liệu này hướng dẫn bạn thực hành chi tiết về **Case Study 3: Phân quyền RBAC vs Permission-Based AC** dựa trên lý thuyết tại [README.md](file:///d:/backend_docs/8.security/README.md).

---

## 1. So Sánh Lý Thuyết & Hạn Chế

### 1.1. Role-Based Access Control (RBAC) - Phân quyền theo Vai trò
*   **Cách hoạt động**: API kiểm tra trực tiếp người dùng có vai trò gì (Ví dụ: `ROLE_ADMIN`, `ROLE_USER`).
*   **Hạn chế**: Cứng nhắc. Khi xuất hiện vai trò mới (ví dụ: `ROLE_EDITOR`, `ROLE_MANAGER`), ta phải sửa code kiểm tra phân quyền trên API và triển khai lại (deploy) hệ thống.

### 1.2. Permission-Based Access Control - Phân quyền theo Quyền hạn
*   **Cách hoạt động**:
    *   API chỉ quan tâm đến quyền hạn cụ thể (ví dụ: `DOCUMENT_READ`, `DOCUMENT_WRITE`).
    *   Người dùng có các Vai trò. Mỗi vai trò là một tập hợp các Quyền hạn cụ thể lưu trong Database.
*   **Ưu điểm**: Linh hoạt tối đa. Người quản trị có thể thêm bớt quyền hạn cho các vai trò thông qua trang quản trị (Admin Dashboard) tác động trực tiếp vào DB, hệ thống tự động cập nhật lập tức mà không cần sửa một dòng code nào hay khởi động lại Server.

---

## 2. Các Thành Phần Thực Hành Trong Project

1.  **[Role.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/entity/Role.java)**: Entity chứa tên Role và tập hợp danh sách các `permissions` (`DOCUMENT_READ`, `DOCUMENT_WRITE`,...).
2.  **[DataInitializer.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/config/DataInitializer.java)**: Khởi tạo sẵn dữ liệu khi ứng dụng chạy:
    *   Tài khoản `admin` (mật khẩu: `adminPass123`) có vai trò `ROLE_ADMIN` sở hữu các quyền: `DOCUMENT_READ`, `DOCUMENT_WRITE`, `DOCUMENT_DELETE`.
    *   Tài khoản `user1` (mật khẩu: `userPass123`) có vai trò `ROLE_USER` sở hữu quyền: `DOCUMENT_READ`.
3.  **[AuthorizationDemoController.java](file:///d:/backend_docs/8.security/security/src/main/java/com/tamdao/security/controller/AuthorizationDemoController.java)**: Expose các API để kiểm chứng.

---

## 3. Các Bước Thực Hành Kiểm Chứng

### Khởi chạy dự án
Mở Terminal tại thư mục `d:\backend_docs\8.security\security\` và chạy:
```bash
.\mvnw spring-boot:run
```

---

### Bước 1: Kiểm chứng Role-Based Access Control (RBAC)
Endpoint `/api/authz/rbac/admin-only` yêu cầu trực tiếp vai trò `ROLE_ADMIN`.

#### A. Truy cập bằng tài khoản `admin`:
```bash
curl -H "X-Auth-User: admin" http://localhost:8080/api/authz/rbac/admin-only
```
**Kết quả**: `200 OK`
`Chào Admin (System Administrator)! Bạn đã truy cập thành công vào khu vực bảo mật vai trò ADMIN.`

#### B. Truy cập bằng tài khoản `user1`:
```bash
curl -H "X-Auth-User: user1" http://localhost:8080/api/authz/rbac/admin-only
```
**Kết quả**: `403 Forbidden`
`Từ chối truy cập (RBAC)! API này yêu cầu vai trò ROLE_ADMIN. Vai trò hiện tại của bạn: ROLE_USER`

---

### Bước 2: Kiểm chứng Permission-Based Access Control
Endpoint đọc tài liệu yêu cầu quyền `DOCUMENT_READ`. Endpoint tạo tài liệu yêu cầu quyền `DOCUMENT_WRITE`.

#### A. Kiểm tra quyền Đọc tài liệu (Cả hai tài khoản đều có quyền):
```bash
# Thử với user1
curl -H "X-Auth-User: user1" http://localhost:8080/api/authz/permissions/document

# Thử với admin
curl -H "X-Auth-User: admin" http://localhost:8080/api/authz/permissions/document
```
**Kết quả**: Cả hai đều đọc thành công `200 OK`.

#### B. Kiểm tra quyền Ghi tài liệu (Chỉ admin có quyền, user1 bị chặn):
```bash
# Thử với user1 (Vai trò ROLE_USER ban đầu không có quyền DOCUMENT_WRITE)
curl -X POST -H "X-Auth-User: user1" http://localhost:8080/api/authz/permissions/document
```
**Kết quả**: `403 Forbidden`
`Từ chối truy cập (Permission-based)! Bạn thiếu quyền 'DOCUMENT_WRITE'.`

---

### Bước 3: Nâng quyền cho vai trò năng động (Dynamic Permission Elevation)
Đây là phần thú vị nhất! Chúng ta sẽ nâng quyền cho vai trò `ROLE_USER` bằng cách thêm quyền `DOCUMENT_WRITE` trực tiếp thông qua API Admin (cập nhật DB) mà **không sửa code** và **không restart Server**.

Gửi yêu cầu cập nhật danh sách quyền cho vai trò `ROLE_USER` gồm: `DOCUMENT_READ` và `DOCUMENT_WRITE`:

```bash
curl -X PUT http://localhost:8080/api/authz/admin/roles/ROLE_USER/permissions \
  -H "Content-Type: application/json" \
  -d "[\"DOCUMENT_READ\", \"DOCUMENT_WRITE\"]"
```
**Kết quả phản hồi**:
`CẬP NHẬT THÀNH CÔNG! Đã cập nhật danh sách quyền cho vai trò ROLE_USER thành: [DOCUMENT_READ, DOCUMENT_WRITE]`

---

### Bước 4: Kiểm tra lại quyền Ghi tài liệu của `user1`
Ngay lập tức, không cần khởi động lại máy chủ, gửi lại yêu cầu tạo tài liệu bằng tài khoản `user1`:

```bash
curl -X POST -H "X-Auth-User: user1" http://localhost:8080/api/authz/permissions/document
```

**Kết quả**: `200 OK`
`Tạo tài liệu mới thành công! (Normal User có quyền DOCUMENT_WRITE)`

> [!TIP]
> **Nhận xét**: Bạn thấy rõ lợi ích của Permission-based Access Control. Chỉ cần cập nhật phân quyền trong CSDL, lập tức tài khoản `user1` được hưởng quyền hạn mới mà nhà phát triển không cần phải can thiệp sửa đổi mã nguồn `@PreAuthorize("hasRole('ADMIN')")` hoặc viết lại logic kiểm tra.
