package com.tamdao.security.repository;

import com.tamdao.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    
    // Blind Index search methods
    Optional<User> findByEmailBlindIndex(String emailBlindIndex);
    Optional<User> findByPhoneBlindIndex(String phoneBlindIndex);
}
