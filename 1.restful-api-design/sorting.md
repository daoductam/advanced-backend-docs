# Sắp xếp dữ liệu trong RESTful API (Sorting)

---

Khi thiết kế API hỗ trợ sắp xếp, Client cần truyền tham số lên Server để chỉ định:
1. Trường thông tin muốn sắp xếp (ví dụ: `price`, `created_at`).
2. Chiều sắp xếp: Tăng dần (`ASC`) hoặc Giảm dần (`DESC`).

## 1. Các định dạng truyền tham số phổ biến trên URL

Dưới đây là 3 kiểu thiết kế URL Query Parameter thường gặp nhất trong thực tế:

### Kiểu 1: Sử dụng dấu hai chấm (hoặc gạch dưới) làm phân cách
*   **URL:** `GET /api/v1/users?sort=createdAt:desc,username:asc`
*   **Ưu điểm:** Rõ ràng, dễ đọc, không bị lỗi mã hóa ký tự đặc biệt trên URL. Dễ parse ở phía Backend bằng cách split dấu phẩy `,` và dấu hai chấm `:`.

### Kiểu 2: Sử dụng ký tự đặc biệt (`+` và `-`)
*   **URL:** `GET /api/v1/users?sort=-createdAt,+username`
*   **Lưu ý quan trọng:** Ký tự `+` khi truyền trên query parameter thường bị trình duyệt hoặc các thư viện HTTP tự động mã hóa (URL Encode) thành khoảng trắng (` `). Ở Backend, khi nhận giá trị cần xử lý thay thế khoảng trắng thành dấu `+` trước khi phân tích cú pháp.
*   **Ý nghĩa:** `-` đại diện cho `DESC` (giảm dần), `+` đại diện cho `ASC` (tăng dần).

### Kiểu 3: Sử dụng cú pháp SQL trực tiếp
*   **URL:** `GET /api/v1/users?sort=createdAt desc,username asc`
*   **Ưu điểm:** Trực quan vì giống hệt SQL. Tuy nhiên cần cẩn thận lỗi bảo mật nếu truyền trực tiếp chuỗi này vào database.

---

## ⚠️ Nguyên tắc Bảo mật cực kỳ quan trọng: White-list Sorting

> [!WARNING]
> **TUYỆT ĐỐI KHÔNG** nối trực tiếp chuỗi `sort` từ client gửi lên vào câu lệnh SQL của Database. Việc này sẽ dẫn đến 2 thảm họa:
> 1. **SQL Injection (Bảo mật):** Kẻ tấn công có thể chèn các cú pháp phá hoại hệ thống qua tham số `sort` (ví dụ: `?sort=id; DROP TABLE users;--`).
> 2. **Sập hiệu năng DB (Performance):** Nếu người dùng cố tình sắp xếp theo các trường không được đánh chỉ mục (Index) như `bio`, `description`, DB sẽ phải quét toàn bộ bảng và thực hiện sắp xếp tạm trên bộ nhớ ngoài (Filesort), gây nghẽn và sập DB khi dữ liệu lớn.

### Giải pháp: Sử dụng White-list (Danh sách trắng)
Backend phải định nghĩa một danh sách các trường **HỢP LỆ** và **ĐÃ ĐƯỢC ĐÁNH INDEX** được phép sắp xếp (ví dụ: chỉ cho phép sort theo `id`, `createdAt`, `username`). 
Bất kỳ yêu cầu sắp xếp theo trường nào nằm ngoài danh sách này sẽ bị Backend từ chối ngay lập tức (ném lỗi `400 Bad Request`).

---

## 2. Thiết kế và Triển khai code trong Spring Boot

Chúng ta sẽ triển khai **Kiểu 1** (`sort=createdAt:desc,username:asc`) vì nó an toàn và trực quan nhất.

### 2.1. Code parse tham số Sort an toàn (White-list Validation)

Chúng ta sử dụng Spring Data `Sort` để tạo ra đối tượng sắp xếp động nhưng có kiểm tra bảo mật nghiêm ngặt.

#### Cập nhật `UserService.java`

Thêm phương thức hỗ trợ phân tích tham số `sort` một cách an toàn:

```java
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 1. Định nghĩa White-list: Chỉ cho phép sắp xếp theo các trường này
private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "createdAt", "username", "email");

public Sort parseSortParameters(String sortParam) {
    if (sortParam == null || sortParam.trim().isEmpty()) {
        return Sort.unsorted();
    }

    List<Order> orders = new ArrayList<>();
    
    // Tách các trường sắp xếp bằng dấu phẩy ',' (ví dụ: createdAt:desc,username:asc)
    String[] sortFields = sortParam.split(",");
    
    for (String fieldParam : sortFields) {
        // Tách trường và hướng bằng dấu hai chấm ':'
        String[] parts = fieldParam.split(":");
        String field = parts[0].trim();
        
        // Kiểm tra bảo mật: Trường gửi lên có nằm trong White-list không?
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Trường sắp xếp không hợp lệ hoặc không được hỗ trợ: " + field);
        }
        
        // Xác định chiều sắp xếp (mặc định là ASC)
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String dirString = parts[1].trim().toLowerCase();
            if ("desc".equals(dirString)) {
                direction = Sort.Direction.DESC;
            }
        }
        
        orders.add(new Order(direction, field));
    }
    
    return Sort.by(orders);
}
```

### 2.2. Tích hợp Sorting vào API Page & Size (Cách 1)

#### Trong `UserService.java`:
```java
public Page<User> getUsersSorted(int page, int size, String sortParam) {
    // Gọi hàm parse an toàn ở trên để lấy đối tượng Sort
    Sort sort = parseSortParameters(sortParam);
    
    // Nếu Client không truyền sort, áp dụng sắp xếp mặc định để tránh kết quả ngẫu nhiên
    if (sort.isUnsorted()) {
        sort = Sort.by("createdAt").descending().and(Sort.by("id").descending());
    }
    
    Pageable pageable = PageRequest.of(page, size, sort);
    return userRepository.findAll(pageable);
}
```

#### Trong `UserController.java`:
```java
@GetMapping("/page-size/sorted")
public Page<User> getUsersSorted(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String sort) {
    return userService.getUsersSorted(page, size, sort);
}
```

---

## 3. So sánh các giải pháp và Cách lựa chọn

| Giải pháp truyền tham số | Khi nào nên dùng | Lưu ý triển khai |
| :--- | :--- | :--- |
| **`sort=createdAt:desc`** | **Khuyên dùng** cho hầu hết API | Dễ xử lý chuỗi ở backend nhất, ít lỗi phát sinh do encoding. |
| **`sort=-createdAt`** | Thích hợp khi muốn URL ngắn gọn | Cần parse thay thế ký tự trống thành `+` trước khi trích xuất giá trị `ASC`. |
| **White-list Validation** | **Bắt buộc** cho tất cả các giải pháp | Phải khai báo biến Static chứa danh sách các cột Database được index và cho phép sắp xếp. |
