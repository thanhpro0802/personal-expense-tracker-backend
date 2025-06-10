package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.Category;
import com.expensetracker.backend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Sử dụng @CrossOrigin để cho phép frontend từ một domain/port khác truy cập API này
// Trong môi trường production, bạn nên cấu hình chi tiết hơn hoặc thông qua Security Config
@CrossOrigin(origins = "http://localhost:3000") // Ví dụ cho React frontend chạy ở cổng 3000
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Lấy tất cả danh mục (có thể bao gồm cả mặc định và của người dùng)
    @GetMapping("/all") // Một endpoint khác để lấy tất cả, nếu cần
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Lấy danh mục mặc định (user_id IS NULL)
    @GetMapping("/default")
    public ResponseEntity<List<Category>> getDefaultCategories() {
        List<Category> defaultCategories = categoryService.getDefaultCategories();
        return ResponseEntity.ok(defaultCategories);
    }

    // Lấy tất cả danh mục của một người dùng cụ thể (bao gồm cả mặc định và tùy chỉnh của họ)
    // Sẽ cần xác thực người dùng sau, tạm thời dùng userId trong path/request param
    // Trong thực tế, userId sẽ được lấy từ thông tin xác thực của người dùng hiện tại
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(@PathVariable UUID userId) {
        try {
            List<Category> userCategories = categoryService.getUserCategories(userId);
            return ResponseEntity.ok(userCategories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }
    }

    // Lấy danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable UUID id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tạo danh mục mới
    // Cần biết user_id nếu là danh mục tùy chỉnh
    @PostMapping("/{userId}") // Giả định userId được truyền vào để tạo danh mục cho người dùng đó
    public ResponseEntity<Category> createCategory(@PathVariable UUID userId, @RequestBody Category category) {
        try {
            // Thiết lập user cho category nếu nó là custom category
            if (userId != null) {
                // Tạo một đối tượng User "giả" chỉ với ID để liên kết
                category.setUser(new com.expensetracker.backend.model.User());
                category.getUser().setId(userId);
            } else {
                // Nếu userId là null, đây là danh mục mặc định
                category.setUser(null);
            }

            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (RuntimeException e) {
            // Xử lý lỗi trùng lặp hoặc user không tồn tại
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
            }
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request cho các lỗi khác
        }
    }


    // Cập nhật danh mục
    @PutMapping("/{id}/user/{userId}") // Cần userId để kiểm tra quyền sở hữu
    public ResponseEntity<Category> updateCategory(@PathVariable UUID id, @RequestBody Category categoryDetails, @PathVariable UUID userId) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDetails, userId);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa danh mục
    @DeleteMapping("/{id}/user/{userId}") // Cần userId để kiểm tra quyền sở hữu
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id, @PathVariable UUID userId) {
        try {
            categoryService.deleteCategory(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}