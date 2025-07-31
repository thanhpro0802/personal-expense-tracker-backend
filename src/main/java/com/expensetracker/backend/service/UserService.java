package com.expensetracker.backend.service;

import com.expensetracker.backend.exception.BadRequestException;
import com.expensetracker.backend.exception.ResourceNotFoundException;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.payload.request.SignupRequest;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
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

    /**
     * --- ĐÃ SỬA LỖI ---
     * Phương thức này giờ đây nhận vào SignupRequest (DTO) từ Controller.
     * Trách nhiệm chuyển đổi từ DTO sang Entity được thực hiện tại đây.
     *
     * @param signupRequest DTO chứa thông tin đăng ký của người dùng.
     * @return Đối tượng User đã được tạo và lưu vào DB.
     */
    @Transactional
    public User createUser(SignupRequest signupRequest) {
        // Kiểm tra username/email đã tồn tại chưa
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Tạo một đối tượng User mới từ SignupRequest
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getName());

        // Mã hóa mật khẩu trước khi lưu
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Cập nhật các trường...
        user.setName(userDetails.getName());
        // (Thêm các trường khác nếu cần)

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}