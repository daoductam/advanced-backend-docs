package com.tamdao.restful_api_design.controller;

import com.tamdao.restful_api_design.dto.CommonPageResponse;
import com.tamdao.restful_api_design.model.User;
import com.tamdao.restful_api_design.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * CÁCH 1: Phân trang theo Page và Size
     * Ví dụ: GET /api/v1/users/page-size?page=0&size=10
     * Trả về đầy đủ metadata: totalElements, totalPages, isLast...
     */
    @GetMapping("/page-size")
    public Page<User> getUsersByPageSize(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return userService.getUsersByPageSize(page, size);
    }

    /**
     * TÍCH HỢP SORTING: Phân trang Page & Size kết hợp Sắp xếp động an toàn
     * Ví dụ: GET /api/v1/users/page-size/sorted?page=0&size=10&sort=createdAt:desc,username:asc
     */
    @GetMapping("/page-size/sorted")
    public Page<User> getUsersSorted(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false) String sort) {
        return userService.getUsersSorted(page, size, sort);
    }

    /**
     * CÁCH 2 (ĐANG COMMENT): Phân trang theo Offset và Limit
     * Ví dụ: GET /api/v1/users/offset-limit?offset=0&limit=10
     * offset = số bản ghi cần bỏ qua, limit = số bản ghi tối đa cần lấy
     */
    @GetMapping("/offset-limit")
    public List<User> getUsersByOffsetLimit(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return userService.getUsersByOffsetLimit(offset, limit);
    }

    /**
     * CÁCH 3: Phân trang tối ưu hóa bằng Deferred Join (Liên kết trì hoãn)
     * Ví dụ: GET /api/v1/users/deferred-join?offset=0&limit=10
     * Giải pháp cải tiến hiệu năng vượt trội cho Offset lớn trên DB dữ liệu lớn.
     */
    @GetMapping("/deferred-join")
    public List<User> getUsersByDeferredJoin(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return userService.getUsersByDeferredJoin(offset, limit);
    }

    /**
     * CÁCH 4: Phân trang dạng Con trỏ (Cursor-Based Pagination)
     * Ví dụ: GET /api/v1/users/cursor?cursor=50&limit=10
     * Không sử dụng OFFSET, giải quyết triệt để vấn đề Data Drifting.
     */
    @GetMapping("/cursor")
    public CommonPageResponse<User> getUsersByCursor(
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return userService.getUsersByCursor(cursor, limit);
    }
}

