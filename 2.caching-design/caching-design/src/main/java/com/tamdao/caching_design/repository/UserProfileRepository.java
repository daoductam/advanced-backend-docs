package com.tamdao.caching_design.repository;

import com.tamdao.caching_design.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA tự động generate các câu SQL CRUD từ interface này.
 * Không cần viết SQL thủ công.
 *
 * JpaRepository<UserProfile, Long>:
 *   - UserProfile: Entity type
 *   - Long: Kiểu dữ liệu của Primary Key
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
