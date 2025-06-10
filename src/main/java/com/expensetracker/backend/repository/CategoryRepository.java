package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Category;
import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // Tìm tất cả các danh mục mặc định (user_id IS NULL)
    List<Category> findByUserIsNull();

    // Tìm tất cả các danh mục của một người dùng cụ thể
    List<Category> findByUser(User user);

    // Tìm một danh mục cụ thể theo ID và user_id (để đảm bảo quyền sở hữu)
    Optional<Category> findByIdAndUser(UUID id, User user);

    // Tìm một danh mục cụ thể theo ID và user_id IS NULL (dành cho danh mục mặc định)
    Optional<Category> findByIdAndUserIsNull(UUID id);

    // Tìm một danh mục theo tên (không phân biệt hoa thường) và người dùng (bao gồm cả NULL)
    Optional<Category> findByNameIgnoreCaseAndUser(String name, User user);

    // Tìm một danh mục mặc định theo tên (không phân biệt hoa thường)
    Optional<Category> findByNameIgnoreCaseAndUserIsNull(String name);
}