package com.expensetracker.backend.service;

import com.expensetracker.backend.exception.ResourceNotFoundException;
import com.expensetracker.backend.exception.TokenRefreshException;
import com.expensetracker.backend.model.RefreshToken;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.RefreshTokenRepository;
import com.expensetracker.backend.repository.UserRepository;
import com.expensetracker.backend.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // --- SỬA LỖI 3: Đồng bộ tên thuộc tính ---
    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    // --- CẢI TIẾN 1: Tích hợp JwtUtils ---
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Tìm một refresh token trong database dựa vào chuỗi token.
     * @param token Chuỗi token cần tìm.
     * @return Optional chứa RefreshToken nếu tìm thấy.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Tạo và lưu một Refresh Token mới cho người dùng.
     * Phương thức này kết hợp việc tạo JWT và lưu vào DB.
     * @param userId ID của người dùng cần tạo token.
     * @return Đối tượng RefreshToken đã được lưu.
     */
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        // --- SỬA LỖI 2: Xử lý an toàn, tránh .get() ---
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Xóa token cũ nếu có để đảm bảo mỗi user chỉ có 1 refresh token tại 1 thời điểm
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        // --- CẢI TIẾN 1: Sử dụng JwtUtils để tạo token ---
        String tokenValue = jwtUtils.generateRefreshToken(userId.toString());
        refreshToken.setToken(tokenValue);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Kiểm tra xem một token có hết hạn hay không.
     * Nếu hết hạn, xóa nó khỏi DB và ném ra ngoại lệ.
     * @param token Đối tượng RefreshToken cần kiểm tra.
     * @return Đối tượng RefreshToken nếu nó vẫn hợp lệ.
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign-in request.");
        }
        return token;
    }

    /**
     * Xóa Refresh Token của một người dùng, thường được dùng khi logout.
     * @param userId ID của người dùng.
     * @return Số lượng bản ghi đã được xóa (thường là 1 hoặc 0).
     */
    @Transactional
    public int deleteByUserId(UUID userId) {
        // --- SỬA LỖI 2: Xử lý an toàn, tránh .get() ---
        return userRepository.findById(userId)
                .map(refreshTokenRepository::deleteByUser)
                .orElse(0); // Nếu không tìm thấy user, không xóa gì cả, trả về 0.
    }
}