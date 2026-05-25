package com.tamdao.restful_api_design.service;

import com.tamdao.restful_api_design.dto.CommonPageResponse;
import com.tamdao.restful_api_design.model.User;
import com.tamdao.restful_api_design.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * CÁCH 1: Phân trang theo Page và Size (Page & Size parameters)
     * API: GET /api/v1/users/page-size?page=0&size=10
     * Use case: Hệ thống quản trị, Admin Dashboard - hỗ trợ nhảy tới bất kỳ trang nào.
     * Lưu ý: page bắt đầu từ 0 (0-indexed).
     */
    public Page<User> getUsersByPageSize(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending().and(Sort.by("id").descending())
        );
        return userRepository.findAll(pageable);
    }

    /**
     * CÁCH 2 (ĐANG COMMENT - ĐỌC ĐỂ HIỂU): Phân trang theo Offset và Limit (Offset & Limit parameters)
     * API: GET /api/v1/users/offset-limit?offset=0&limit=10
     * Use case: Infinite scroll (cuộn vô hạn), Newsfeed, Ghi log sự kiện.
     * Cách tính: offset = (page - 1) * limit (nếu page bắt đầu từ 1)
     *            offset = page * limit      (nếu page bắt đầu từ 0)
     *
     * ⚠️ Nhược điểm của Offset & Limit:
     *   1. Hiệu năng giảm khi offset rất lớn (DB phải scan và bỏ qua hàng triệu dòng).
     *   2. Dữ liệu bị trôi (Data Drifting) nếu có insert/delete trong lúc phân trang.
     */
    public List<User> getUsersByOffsetLimit(int offset, int limit) {
        return userRepository.findAllWithOffset(offset, limit);
    }

    /**
     * CÁCH 3: Liên kết trì hoãn (Deferred Join) - Cải tiến trực tiếp từ Cách 2
     * API: GET /api/v1/users/deferred-join?offset=0&limit=10
     * 
     * Khắc phục nhược điểm của Cách 2:
     *   - Thay vì nạp toàn bộ các cột của (offset + limit) bản ghi vào RAM rồi discard,
     *     Cách 3 sử dụng subquery chỉ SELECT ID và tận dụng Cover Index để tìm ra các ID thỏa mãn.
     *   - Sau khi có danh sách ID, DB thực hiện INNER JOIN lại bảng chính để lấy full cột.
     */
    public List<User> getUsersByDeferredJoin(int offset, int limit) {
        return userRepository.findAllWithDeferredJoin(offset, limit);
    }

    /**
     * CÁCH 4: Cursor-Based Pagination
     * API: GET /api/v1/users/cursor?cursor=1543&limit=10
     * 
     * Lợi ích vượt trội:
     *   - Loại bỏ hoàn toàn vấn đề trôi/trùng lặp dữ liệu (Data Drifting) do Insert/Delete đồng thời.
     *   - Hiệu năng cực cao O(1) do nhảy trực tiếp qua Index Key, không quét lại các trang cũ (No OFFSET).
     * 
     * Cách thiết kế "Has More" (Có trang tiếp theo không):
     *   - Truy vấn SQL với số lượng = limit + 1.
     *   - Nếu kết quả trả về bằng limit + 1: Tức là vẫn còn trang sau -> hasMore = true.
     *   - Sau đó ta bỏ phần tử thứ limit + 1 đi để trả về đúng size client yêu cầu,
     *     và lấy ID của phần tử cuối cùng làm nextCursor.
     */
    public CommonPageResponse<User> getUsersByCursor(Long cursor, int limit) {
        // Lấy dư ra 1 bản ghi (limit + 1) để xác định xem có trang tiếp theo hay không
        List<User> users = userRepository.findAllWithCursor(cursor, limit + 1);

        boolean hasMore = false;
        Long nextCursor = null;

        if (users.size() > limit) {
            hasMore = true;
            // Xóa đi bản ghi dư thừa đó
            users.remove(limit);
            // Lấy ID của bản ghi cuối cùng trong list đã lọc làm con trỏ cho trang tiếp theo
            nextCursor = users.get(users.size() - 1).getId();
        }

        CommonPageResponse.PagingMetadata metadata = CommonPageResponse.PagingMetadata.builder()
                .limit(limit)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();

        return CommonPageResponse.<User>builder()
                .data(users)
                .paging(metadata)
                .build();
    }
}

