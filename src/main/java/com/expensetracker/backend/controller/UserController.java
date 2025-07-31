package com.expensetracker.backend.controller;

import com.expensetracker.backend.exception.ResourceNotFoundException;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller để quản lý người dùng.
 * Các endpoint này thường yêu cầu quyền ADMIN.
 * User registration được xử lý bởi AuthController.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600) // Thêm CORS cho nhất quán
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lấy danh sách tất cả người dùng.
     * @return ResponseEntity chứa danh sách User.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Lấy thông tin một người dùng cụ thể bằng ID.
     * @param id UUID của người dùng.
     * @return ResponseEntity chứa User hoặc 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        // Tận dụng Exception Handler, code gọn hơn rất nhiều
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    /**
     * --- ĐÃ XÓA ---
     * Phương thức createUser đã được loại bỏ khỏi controller này.
     * Việc tạo người dùng (đăng ký) được xử lý duy nhất tại AuthController
     * thông qua endpoint /api/auth/signup để đảm bảo an toàn và nhất quán.
     */

    /**
     * Cập nhật thông tin người dùng.
     * @param id UUID của người dùng cần cập nhật.
     * @param userDetails Đối tượng User chứa thông tin mới.
     * @return ResponseEntity chứa User đã được cập nhật.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        // Service layer sẽ tự ném ra ResourceNotFoundException nếu không tìm thấy user.
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Xóa một người dùng.
     * @param id UUID của người dùng cần xóa.
     * @return ResponseEntity với trạng thái 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        // Service layer sẽ tự ném ra ResourceNotFoundException nếu không tìm thấy user.
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}