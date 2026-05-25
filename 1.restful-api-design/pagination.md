# Phân trang trong RESTful API (Pagination)

---

## 1. Phân trang theo Page & Size

### Nguyên lý

Client truyền lên 2 tham số:
- `page`: chỉ số trang (bắt đầu từ `0`)
- `size`: số bản ghi tối đa trên một trang

**API mẫu:** `GET /api/v1/users/page-size?page=0&size=10`

### Cơ chế Database

Spring Data JPA tự động sinh ra **2 câu lệnh SQL** cho mỗi request:

```sql
-- Câu 1: Lấy dữ liệu trang hiện tại
SELECT * FROM users
ORDER BY created_at DESC, id DESC
LIMIT 10 OFFSET 0;

-- Câu 2: Đếm tổng số bản ghi (để tính totalPages, totalElements)
SELECT COUNT(*) FROM users;
```

### Response trả về

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

Spring tự động đóng gói `totalElements`, `totalPages`, `isLast`, `isFirst`... vào response.

### Use case phù hợp

- Hệ thống quản trị (Admin Dashboard, Management Portal).
- Giao diện cần hiển thị **tổng số trang** và cho phép người dùng **nhảy tới bất kỳ trang nào**.

### Code triển khai

```java
// Repository: dùng thẳng JpaRepository, không cần custom method
Page<User> findAll(Pageable pageable);

// Service
public Page<User> getUsersByPageSize(int page, int size) {
    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending().and(Sort.by("id").descending())
    );
    return userRepository.findAll(pageable);
}

// Controller
// GET /api/v1/users/page-size?page=0&size=10
@GetMapping("/page-size")
public Page<User> getUsersByPageSize(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return userService.getUsersByPageSize(page, size);
}
```

---

## 2. Phân trang theo Offset & Limit

### Nguyên lý

Client truyền lên 2 tham số:
- `offset`: số bản ghi cần **bỏ qua** tính từ đầu
- `limit`: số bản ghi tối đa cần lấy

**API mẫu:** `GET /api/v1/users/offset-limit?offset=0&limit=10`

### Công thức quy đổi

```
offset = page * limit        (nếu page bắt đầu từ 0)
offset = (page - 1) * limit  (nếu page bắt đầu từ 1)
```

| Trang | offset | limit | SQL thực thi |
|-------|--------|-------|--------------|
| 1     | 0      | 10    | `LIMIT 10 OFFSET 0`  |
| 2     | 10     | 10    | `LIMIT 10 OFFSET 10` |
| 3     | 20     | 10    | `LIMIT 10 OFFSET 20` |

### Cơ chế Database

```sql
SELECT * FROM users
ORDER BY created_at DESC, id DESC
LIMIT :limit OFFSET :offset;
```

Khác với Cách 1, Cách 2 **không** tự động sinh `COUNT(*)` vì không cần trả về `totalPages`.

### Use case phù hợp

- Danh sách cuộn vô hạn (Infinite Scroll).
- Newsfeed, dòng thời gian mạng xã hội.
- Ghi log sự kiện (Event Log).

### Code triển khai

```java
// Repository: dùng Native Query để truyền OFFSET trực tiếp
@Query(value = "SELECT * FROM users ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset",
       nativeQuery = true)
List<User> findAllWithOffset(@Param("offset") int offset, @Param("limit") int limit);

// Service
public List<User> getUsersByOffsetLimit(int offset, int limit) {
    return userRepository.findAllWithOffset(offset, limit);
}

// Controller
// GET /api/v1/users/offset-limit?offset=0&limit=10
@GetMapping("/offset-limit")
public List<User> getUsersByOffsetLimit(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "10") int limit) {
    return userService.getUsersByOffsetLimit(offset, limit);
}
```

> **Lưu ý:** Phải dùng `nativeQuery = true` vì JPQL không hỗ trợ truyền `OFFSET` dưới dạng tham số trực tiếp.

---

## ⚠️ Vấn đề của Cách 2: Hiệu năng giảm sâu khi dữ liệu lớn

### Nguyên nhân 1: `COUNT(*)` chậm (chỉ ảnh hưởng Cách 1)

Cách 1 sinh ra `COUNT(*)` tự động để tính `totalPages`. Trên bảng **10 triệu bản ghi**, câu lệnh này phải **full scan toàn bộ bảng** → tiêu tốn cực nhiều CPU và Disk I/O.

### Nguyên nhân 2: `OFFSET` lớn → scan và bỏ đi toàn bộ bản ghi trước đó

Đây là nguyên nhân nghiêm trọng hơn, ảnh hưởng cả 2 cách.

Giả sử bảng có 10 triệu bản ghi, Client đang ở trang 10.000:

```sql
SELECT * FROM users ORDER BY created_at DESC LIMIT 10 OFFSET 100000;
```

Bên trong Database thực thi theo thứ tự:

```
Bước 1 - ORDER BY: Sắp xếp 10,000,000 bản ghi
Bước 2 - OFFSET:   Đọc và NẠP VÀO RAM 100,000 bản ghi đầy đủ (id + username + email + bio...)
                   → Sau đó vứt bỏ toàn bộ 100,000 bản ghi đó đi
Bước 3 - LIMIT:    Chỉ giữ lại đúng 10 bản ghi cần thiết
```

**Kết quả:** 100,000 × (~700 byte/bản ghi) ≈ **~70 MB** được nạp vào RAM chỉ để bị xóa ngay.

---

## 3. Giải pháp Deferred Join (Liên kết trì hoãn)

### Nguyên lý

Tách truy vấn làm **2 bước**:

1. **Bước 1 (Subquery):** Chỉ quét trên **Index** của `id` để tìm ra **danh sách ID** cần lấy. Index gọn nhẹ (chỉ 8 byte/bản ghi), nằm trong RAM → cực nhanh.
2. **Bước 2 (JOIN):** Dùng danh sách ID tìm được ở Bước 1 để **JOIN** ngược lại bảng gốc, chỉ đọc đúng những bản ghi cần thiết.

### So sánh dung lượng đọc vào RAM

```
❌ Offset thông thường:
   100,000 bản ghi × (700 byte id + username + email + bio...) = ~70 MB

✅ Deferred Join:
   100,000 ID × (8 byte) = ~0.8 MB    ← nhỏ hơn ~87 lần!
   Sau đó chỉ đọc đúng 10 bản ghi đầy đủ
```

### Truy vấn SQL

```sql
-- ❌ Cách thông thường (chậm ở offset lớn)
SELECT * FROM users
ORDER BY id
LIMIT 10 OFFSET 100000;

-- ✅ Deferred Join (nhanh nhờ chỉ scan Index ở bước 1)
SELECT u.*
FROM users u
JOIN (
    SELECT id FROM users
    ORDER BY id
    LIMIT 10 OFFSET 100000  -- Bước 1: chỉ quét Index của id
) tmp ON u.id = tmp.id;     -- Bước 2: JOIN lấy full data đúng 10 bản ghi
```

### Code triển khai

```java
// Repository
@Query(value = """
        SELECT u.* FROM users u
        JOIN (
            SELECT id FROM users
            ORDER BY created_at DESC, id DESC
            LIMIT :limit OFFSET :offset
        ) tmp ON u.id = tmp.id
        """,
        nativeQuery = true)
List<User> findAllWithDeferredJoin(@Param("offset") int offset, @Param("limit") int limit);

// Service
public List<User> getUsersByDeferredJoin(int offset, int limit) {
    return userRepository.findAllWithDeferredJoin(offset, limit);
}

// Controller
// GET /api/v1/users/deferred-join?offset=0&limit=10
@GetMapping("/deferred-join")
public List<User> getUsersByDeferredJoin(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "10") int limit) {
    return userService.getUsersByDeferredJoin(offset, limit);
}
```

### So sánh tổng quan 3 cách (Dùng Offset)

| Tiêu chí | Page & Size | Offset & Limit | Deferred Join |
|---|---|---|---|
| **Tham số API** | `page`, `size` | `offset`, `limit` | `offset`, `limit` |
| **Trả về totalPages** | ✅ Có | ❌ Không | ❌ Không |
| **Hiệu năng offset lớn** | ❌ Chậm | ❌ Chậm | ✅ Nhanh |
| **COUNT(*) tự động** | ✅ Có (tốn tài nguyên) | ❌ Không | ❌ Không |
| **Độ phức tạp triển khai** | Thấp | Thấp | Trung bình |
| **Use case** | Admin, Portal | Infinite Scroll | Bảng dữ liệu rất lớn |

---

## 4. Phân trang dạng Con trỏ (Cursor-Based Pagination)

### Vấn đề: Trôi / Trùng lặp dữ liệu (Data Drifting) ở Cách 2 và Cách 3

Mặc dù **Deferred Join (Cách 3)** tối ưu hóa rất tốt vấn đề RAM & CPU khi quét qua `OFFSET` lớn, nhưng bản chất nó vẫn sử dụng `OFFSET`. Khi sử dụng `OFFSET`, cả Cách 2 và Cách 3 đều gặp phải lỗi nghiêm trọng về logic khi dữ liệu biến động liên tục:

* **Trường hợp thêm mới (Insert Drift):**
  1. Client lấy Trang 1 (gồm bản ghi 1 đến 10).
  2. Trong lúc người dùng đang đọc, có 2 bản ghi mới được thêm vào đầu bảng.
  3. Client yêu cầu Trang 2 (`OFFSET 10`). Lúc này DB sẽ bỏ qua 10 bản ghi đầu (bản ghi thứ 9 và 10 của lúc trước nay đã bị đẩy xuống vị trí 11 và 12).
  4. Hệ quả: Bản ghi 9 và 10 sẽ **bị lặp lại** ở Trang 2.
* **Trường hợp xóa bớt (Delete Drift):**
  1. Client lấy Trang 1 (bản ghi 1 đến 10).
  2. Có 2 bản ghi bất kỳ trong Trang 1 bị xóa đi. Bản ghi số 11 và 12 bị dịch (shift) lên vị trí 9 và 10.
  3. Client yêu cầu Trang 2 (`OFFSET 10`).
  4. Hệ quả: Bản ghi số 11 và 12 sẽ **bị bỏ sót hoàn toàn** (không bao giờ hiển thị).

### Giải pháp: Cursor-Based Pagination

Thay vì dùng `OFFSET` để bỏ qua $N$ phần tử không xác định, ta dùng một **"Con trỏ" (Cursor)** neo vào một bản ghi cụ thể (thường là khóa chính hoặc trường sắp xếp độc nhất của bản ghi cuối cùng của trang hiện tại).

**API mẫu:** `GET /api/v1/users/cursor?limit=10&cursor=1543` (với `1543` là ID của bản ghi cuối cùng ở trang trước).

#### Cơ chế Database (SQL)

Sắp xếp theo thứ tự giảm dần (`ORDER BY created_at DESC, id DESC`), câu lệnh SQL cho trang tiếp theo sẽ là:

```sql
SELECT * FROM users
WHERE (created_at < :last_created_at) 
   OR (created_at = :last_created_at AND id < :last_seen_id)
ORDER BY created_at DESC, id DESC
LIMIT :limit;
```
*(Nếu chỉ phân trang theo `id` tăng dần, câu lệnh sẽ đơn giản hơn: `WHERE id > :last_seen_id LIMIT :limit`).*

Bằng cách này:
1. **Không dùng OFFSET:** DB nhảy thẳng tới bản ghi tiếp theo dựa trên Index. Tốc độ là $O(1)$ so với $O(N)$ của offset.
2. **Không bị trôi dữ liệu:** Cho dù bản ghi ở trang trước có bị xóa hay thêm mới vào đầu bảng, vị trí của con trỏ (ID/Created At) vẫn cố định.

### ❓ Vì sao dùng Cursor-Based thay vì Deferred Join (Cách 3)?

| Đặc tính | Deferred Join (Cách 3) | Cursor-Based Pagination |
| :--- | :--- | :--- |
| **Cơ chế nhảy** | Nhảy theo vị trí tương đối (`OFFSET`) | Nhảy theo giá trị bản ghi cụ thể (`Cursor`) |
| **Trôi dữ liệu (Drifting)** | ❌ Bị (Trùng lặp hoặc bỏ sót dữ liệu) | ✅ Không bị ảnh hưởng khi thêm/xóa dữ liệu |
| **Độ phức tạp Client** | ✅ Rất đơn giản (chỉ cần truyền offset tăng dần) | ❌ Phải lưu lại Cursor từ API trước để gửi lên cho API sau |
| **Nhảy trang (Ad-hoc page)**| ✅ Nhảy được (Ví dụ: nhảy từ Trang 1 tới Trang 50) | ❌ Chỉ có thể đi tuần tự (Trang trước -> Trang sau) |
| **Hiệu năng cực lớn** | ⚠️ Tạm ổn (tốt hơn Cách 2 nhưng vẫn phải scan index ở offset lớn) | ✅ Cực nhanh (quét Index Range Scan thẳng từ điểm Cursor) |

> **Kết luận:** Dùng **Cursor-Based** khi bạn xây dựng **Newsfeed, Chat App, Infinite Scroll** có dữ liệu cập nhật liên tục và muốn tránh trôi dữ liệu tuyệt đối. Dùng **Deferred Join** khi bạn làm bảng dữ liệu lớn trong **Admin Dashboard** mà bắt buộc phải có chức năng nhảy trang tùy ý.

### Code triển khai Cursor-Based

```java
// DTO Response chứa Cursor
public class CommonPageResponse<T> {
    private List<T> data;
    private PagingMetadata paging;

    public static class PagingMetadata {
        private int limit;
        private Long nextCursor;
        private Boolean hasMore;
    }
}

// Repository
@Query(value = """
        SELECT * FROM users
        WHERE (:cursor IS NULL OR id < :cursor)
        ORDER BY id DESC
        LIMIT :limit
        """, nativeQuery = true)
List<User> findAllWithCursor(@Param("cursor") Long cursor, @Param("limit") int limit);
```

---

### 4.1. Giải pháp phân trang khi dùng Random UUID làm Khóa chính

Khi hệ thống sử dụng **Random UUID v4** làm khóa chính, UUID được sinh ra hoàn toàn ngẫu nhiên và không có tính chất tuần tự (unordered). 

#### ❌ Tại sao Cursor-Based thông thường thất bại với Random UUID?
Cursor-Based yêu cầu dữ liệu phải có tính chất **sắp xếp thứ tự một chiều xác định** (tăng dần hoặc giảm dần) để có thể dùng toán tử so sánh (ví dụ: `WHERE id < :cursor`). Nếu so sánh UUID ngẫu nhiên theo kiểu chuỗi ký tự (`uuid_string < last_seen_uuid`), thứ tự trả về sẽ là thứ tự bảng chữ cái (lexicographical order), không phản ánh đúng thứ tự bản ghi được chèn vào hệ thống (mất đi ý nghĩa của Newsfeed hay Timeline).

---

#### 💡 CÁC GIẢI PHÁP THAY THẾ

Khi dùng UUID, ta có 3 hướng giải quyết phổ biến như sau:

#### Cách A: Chuyển sang dùng Sequential UUID (UUID v7) - *Khuyên dùng*
*   **Nguyên lý:** Thay vì dùng UUID v4 (hoàn toàn ngẫu nhiên), ta đổi sang **UUID v7** (đã được chuẩn hóa trong RFC 9562). 
*   **Cơ chế:** UUID v7 có 48 bit đầu tiên chứa timestamp (mili-giây epoch), các bit sau mới là giá trị ngẫu nhiên.
*   **Đặc điểm:** UUID v7 **tự động có tính chất tăng dần theo thời gian**.
*   **Cách dùng Cursor:** Bạn có thể dùng trực tiếp UUID v7 làm Cursor như bình thường:
    ```sql
    SELECT * FROM users 
    WHERE (:cursor IS NULL OR id < :cursor) 
    ORDER BY id DESC LIMIT 10;
    ```
    *Độ chính xác cao, hiệu năng index cực tốt và không cần thay đổi cấu trúc bảng.*

#### Cách B: Kết hợp Cursor đa cột (Multi-column Cursor) với trường thời gian
Nếu bắt buộc phải dùng Random UUID v4 làm khóa chính, ta **không thể so sánh UUID** mà phải sử dụng trường thời gian (ví dụ: `created_at`) làm cursor chính, và dùng UUID làm "con trỏ phụ" để phân tách các bản ghi trùng lặp thời gian.

*   **Tham số Client gửi lên:** Cặp đôi gồm `(cursor_created_at, cursor_uuid)`
*   **Truy vấn SQL:**
    ```sql
    SELECT * FROM users
    WHERE (:cursor_created_at IS NULL) OR (
        created_at < :cursor_created_at
        OR (created_at = :cursor_created_at AND id < :cursor_uuid)
    )
    ORDER BY created_at DESC, id DESC
    LIMIT 10;
    ```
*   **Lợi ích:** Giải quyết được trôi dữ liệu và tối ưu index nếu ta tạo Composite Index trên bộ đôi `(created_at, id)`.

#### Cách C: Sử dụng Deferred Join (Cách 3) kết hợp `created_at`
Nếu nghiệp vụ yêu cầu nhảy trang ad-hoc (nhấn sang Trang 5, Trang 10) và dùng Random UUID, bạn nên quay lại dùng **Deferred Join**. 
*   Quét trên index của trường có thứ tự (như `created_at`) để lấy ra danh sách ID UUID, sau đó JOIN lấy dữ liệu đầy đủ.

---

## 5. Bảng hướng dẫn chọn giải pháp phân trang (Decision Matrix)

Để dễ dàng quyết định nên chọn giải pháp phân trang nào cho dự án, bạn có thể tham khảo bảng ma trận quyết định dưới đây:

| Yêu cầu nghiệp vụ | Loại khóa chính (Primary Key) | Kích thước dữ liệu | Giải pháp đề xuất | Giải thích lý do |
| :--- | :--- | :--- | :--- | :--- |
| **Nhảy trang bất kỳ** (Ad-hoc / Random Page Access) | Tự tăng (Auto Increment) hoặc UUID v7 | Dưới 100k dòng | **1. Page & Size** | Dễ viết, dữ liệu nhỏ thì `COUNT(*)` và `OFFSET` không ảnh hưởng lớn đến hiệu năng. |
| **Nhảy trang bất kỳ** (Ad-hoc / Random Page Access) | Tự tăng (Auto Increment) hoặc UUID v7 | Trên 1 triệu dòng | **3. Deferred Join** | Tối ưu hóa việc nạp dữ liệu bằng cách quét Index trước khi JOIN, giữ nguyên tính năng nhảy trang. |
| **Nhảy trang bất kỳ** (Ad-hoc / Random Page Access) | Ngẫu nhiên (Random UUID v4) | Trên 1 triệu dòng | **3. Deferred Join** kết hợp trường thời gian | Bắt buộc phải sắp xếp theo trường thời gian (ví dụ: `created_at`) để thực hiện phân trang, do UUID v4 không tuần tự. |
| **Cuộn vô hạn / Newsfeed** (Infinite Scroll / No page jump) | Tự tăng (Auto Increment) hoặc UUID v7 | Mọi kích thước | **4. Cursor-Based** | Hiệu năng đạt tối đa $O(1)$, loại bỏ hoàn toàn hiện tượng trùng/lọt dữ liệu do insert/delete đồng thời. |
| **Cuộn vô hạn / Newsfeed** (Infinite Scroll / No page jump) | Ngẫu nhiên (Random UUID v4) | Mọi kích thước | **4.1. Cursor đa cột** (Multi-column Cursor) | Sử dụng kết hợp `(created_at, uuid_v4)` làm con trỏ để giải quyết vấn đề UUID v4 không có tính tuần tự. |
| **Hệ thống tải log, dữ liệu lớn** (Export / Streaming) | Tự tăng / UUID v7 | Cực lớn (hàng chục triệu) | **4. Cursor-Based** | Không dùng offset giúp stream dữ liệu liên tục và nhanh nhất mà không gây quá tải tài nguyên DB. |



