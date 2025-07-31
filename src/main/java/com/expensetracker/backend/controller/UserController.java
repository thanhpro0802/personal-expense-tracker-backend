package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.User;
import com.expensetracker.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController // Đánh dấu đây là một REST Controller
@RequestMapping("/api/users") // Định nghĩa đường dẫn cơ sở cho tất cả các API trong controller này
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Lấy tất cả người dùng
    @GetMapping("/all") // Đây là endpoint cho /api/users/all
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Lấy người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok) // Nếu tìm thấy, trả về 200 OK
                .orElse(ResponseEntity.notFound().build()); // Nếu không tìm thấy, trả về 404 Not Found
    }

    // Tạo người dùng mới (đăng ký)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser); // Trả về 201 Created
        } catch (RuntimeException e) {
            // Xử lý lỗi trùng lặp username/email
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser); // Trả về 200 OK
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    // Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build(); // Trả về 204 No Content
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }
}