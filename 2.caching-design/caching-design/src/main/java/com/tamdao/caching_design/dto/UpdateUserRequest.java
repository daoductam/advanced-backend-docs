package com.tamdao.caching_design.dto;

/**
 * DTO (Data Transfer Object) cho API request cập nhật UserProfile.
 *
 * Tách DTO khỏi Entity để:
 *  1. Không expose toàn bộ field của Entity ra ngoài
 *  2. Dễ thêm validation annotation sau này
 *  3. Tránh client truyền id hoặc field nhạy cảm bừa bãi
 */
public record UpdateUserRequest(
        String name,
        int age,
        String email
) {}
