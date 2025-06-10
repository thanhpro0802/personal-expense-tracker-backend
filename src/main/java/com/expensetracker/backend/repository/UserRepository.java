package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // Đánh dấu đây là một Spring component
public interface UserRepository extends JpaRepository<User, UUID> {
    // JpaRepository cung cấp các phương thức CRUD cơ bản: save, findById, findAll, delete, v.v.

    // Thêm các phương thức truy vấn tùy chỉnh nếu cần
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}