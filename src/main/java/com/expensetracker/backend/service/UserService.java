package com.expensetracker.backend.service;

import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder; // Sẽ dùng sau cho mã hóa mật khẩu

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service // Đánh dấu đây là một Spring Service component
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Sẽ inject sau khi cấu hình Spring Security

    // Constructor Injection để Spring tự động inject UserRepository
    // @Autowired là không cần thiết nếu chỉ có một constructor
    public UserService(UserRepository userRepository, Optional<PasswordEncoder> passwordEncoder) {
        this.userRepository = userRepository;
        // Inject PasswordEncoder nếu nó tồn tại (sau khi cấu hình Spring Security)
        this.passwordEncoder = passwordEncoder.orElse(null);
    }

    @Transactional(readOnly = true) // Đánh dấu phương thức này chỉ để đọc, tối ưu hóa giao dịch
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User createUser(User user) {
        // Kiểm tra username/email đã tồn tại chưa
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Mã hóa mật khẩu trước khi lưu (Sẽ kích hoạt sau khi cấu hình Spring Security)
        if (passwordEncoder != null && user.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        } else {
            // Cảnh báo hoặc xử lý nếu PasswordEncoder không có sẵn
            System.err.println("Warning: PasswordEncoder not available or passwordHash is null. Storing plaintext password.");
        }


        // Các trường createdAt và updatedAt đã được tự động xử lý bởi @PrePersist trong Entity
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        // Chỉ cập nhật passwordHash nếu nó được cung cấp và khác null
        if (userDetails.getPasswordHash() != null && !userDetails.getPasswordHash().isEmpty()) {
            if (passwordEncoder != null) {
                user.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
            } else {
                System.err.println("Warning: PasswordEncoder not available. Not updating password hash.");
            }
        }
        // updatedAt đã được tự động xử lý bởi @PreUpdate trong Entity
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}