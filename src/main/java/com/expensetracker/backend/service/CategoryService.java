package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Category;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.CategoryRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository; // Cần để lấy đối tượng User từ ID

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(UUID id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> getDefaultCategories() {
        return categoryRepository.findByUserIsNull();
    }

    @Transactional(readOnly = true)
    public List<Category> getUserCategories(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        List<Category> userCustomCategories = categoryRepository.findByUser(user);
        List<Category> defaultCategories = categoryRepository.findByUserIsNull();

        // Kết hợp danh mục mặc định và danh mục tùy chỉnh của người dùng
        userCustomCategories.addAll(defaultCategories);
        return userCustomCategories;
    }

    @Transactional
    public Category createCategory(Category category) {
        // Kiểm tra xem danh mục có phải của người dùng cụ thể không
        if (category.getUser() != null && category.getUser().getId() != null) {
            // Đảm bảo rằng User tồn tại trước khi liên kết
            User existingUser = userRepository.findById(category.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + category.getUser().getId()));
            category.setUser(existingUser); // Thiết lập đối tượng User được quản lý bởi JPA

            // Kiểm tra trùng lặp cho danh mục tùy chỉnh của người dùng đó
            if (categoryRepository.findByNameIgnoreCaseAndUser(category.getName(), existingUser).isPresent()) {
                throw new RuntimeException("Category '" + category.getName() + "' already exists for this user.");
            }
        } else {
            // Đây là danh mục mặc định, đảm bảo user là NULL
            category.setUser(null);
            // Kiểm tra trùng lặp cho danh mục mặc định
            if (categoryRepository.findByNameIgnoreCaseAndUserIsNull(category.getName()).isPresent()) {
                throw new RuntimeException("Default category '" + category.getName() + "' already exists.");
            }
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(UUID id, Category categoryDetails, UUID userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Đảm bảo chỉ người dùng sở hữu hoặc không có user_id mới được cập nhật
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to update this category.");
        }
        // Nếu là danh mục mặc định, không cho phép người dùng bình thường cập nhật
        if (category.getUser() == null && userId != null) {
            throw new RuntimeException("Access Denied: Cannot update default categories.");
        }

        category.setName(categoryDetails.getName());
        category.setType(categoryDetails.getType());
        category.setDescription(categoryDetails.getDescription());
        category.setIconName(categoryDetails.getIconName());

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID id, UUID userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Đảm bảo chỉ người dùng sở hữu mới được xóa danh mục tùy chỉnh của họ
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to delete this category.");
        }
        // Không cho phép người dùng bình thường xóa danh mục mặc định
        if (category.getUser() == null) {
            throw new RuntimeException("Access Denied: Cannot delete default categories.");
        }

        categoryRepository.delete(category);
    }
}