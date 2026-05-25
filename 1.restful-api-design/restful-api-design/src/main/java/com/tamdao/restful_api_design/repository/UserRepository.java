package com.tamdao.restful_api_design.repository;

import com.tamdao.restful_api_design.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Phục vụ CÁCH 2: Offset & Limit
    // Native Query trực tiếp truyền OFFSET và LIMIT xuống DB mà không cần Spring Pageable
    // ⚠️ Nhược điểm: Khi offset lớn (ví dụ: OFFSET 100000), DB phải đọc và nạp vào RAM toàn bộ bản ghi
    // trước khi vứt bỏ chúng để lấy 10 dòng (LIMIT).
    @Query(value = "SELECT * FROM users ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<User> findAllWithOffset(@Param("offset") int offset, @Param("limit") int limit);

    // Phục vụ CÁCH 3: Deferred Join (Liên kết trì hoãn - Cải tiến từ Cách 2)
    // Cách hoạt động: 
    //   - Subquery (SELECT id FROM users ...) chỉ quét trên INDEX (created_at, id) để lấy ra các ID cần thiết.
    //     Do chỉ lấy ID nên dung lượng dữ liệu nạp vào RAM rất nhỏ (vài trăm KB thay vì hàng chục MB).
    //   - Sau đó JOIN lại bảng users bằng ID để lấy toàn bộ thông tin chi tiết của chỉ đúng LIMIT bản ghi đó.
    @Query(value = """
            SELECT u.* FROM users u
            JOIN (
                SELECT id FROM users
                ORDER BY created_at DESC, id DESC
                LIMIT :limit OFFSET :offset
            ) tmp ON u.id = tmp.id
            ORDER BY u.created_at DESC, u.id DESC
            """, nativeQuery = true)
    List<User> findAllWithDeferredJoin(@Param("offset") int offset, @Param("limit") int limit);

    // Phục vụ CÁCH 4: Cursor-Based Pagination (Khắc phục triệt để vấn đề Data Drifting)
    // Cách hoạt động:
    //   - Client truyền lên cursor (ở đây sử dụng ID của bản ghi cuối cùng đã xem ở trang trước).
    //   - Query sử dụng WHERE id < :cursor (sắp xếp giảm dần) để lấy tiếp dữ liệu tiếp theo.
    //   - Tận dụng Primary Key Index cực kỳ nhanh và không bao giờ bị trùng lặp/bỏ sót dữ liệu khi có Insert/Delete.
    //   - Chúng ta lấy limit + 1 để kiểm tra xem có trang tiếp theo (hasMore) hay không.
    @Query(value = """
            SELECT * FROM users
            WHERE (:cursor IS NULL OR id < :cursor)
            ORDER BY id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<User> findAllWithCursor(@Param("cursor") Long cursor, @Param("limit") int limit);
}


