package com.expensetracker.backend.security.jwt;

import com.expensetracker.backend.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // --- SỬA LỖI 1: Đồng bộ tên thuộc tính với application.properties ---
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    // --- CẢI TIẾN 2: Thêm thuộc tính cho Refresh Token ---
    @Value("${jwt.refresh.expiration.ms}")
    private long jwtRefreshExpirationMs;

    /**
     * Tạo khóa ký (signing key) một cách an toàn từ chuỗi secret.
     * @return Key đối tượng khóa để ký và xác thực.
     */
    private Key getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo Access Token cho người dùng sau khi đăng nhập thành công.
     * @param userPrincipal Thông tin chi tiết của người dùng đã được xác thực.
     * @return Chuỗi Access Token JWT.
     */
    public String generateAccessToken(UserDetailsImpl userPrincipal) {
        String userId = userPrincipal.getId().toString();
        logger.info("Generating access token for user ID: {}", userId);
        return buildToken(userId, jwtExpirationMs);
    }

    /**
     * Tạo Refresh Token cho người dùng.
     * Phương thức này sẽ được gọi từ RefreshTokenService.
     * @param userId ID của người dùng.
     * @return Chuỗi Refresh Token JWT.
     */
    public String generateRefreshToken(String userId) {
        logger.info("Generating refresh token for user ID: {}", userId);
        return buildToken(userId, jwtRefreshExpirationMs);
    }

    /**
     * --- CẢI TIẾN 3: Phương thức private để xây dựng token, chống trùng lặp code ---
     * Xây dựng một JWT với subject và thời gian hết hạn được cung cấp.
     *
     * @param subject Chủ thể của token (ở đây là User ID).
     * @param expirationMs Thời gian hết hạn tính bằng mili giây.
     * @return Chuỗi JWT đã được ký.
     */
    private String buildToken(String subject, long expirationMs) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Nâng cấp lên HS512 cho an toàn hơn
                .compact();
    }

    /**
     * Lấy User ID (dưới dạng chuỗi) từ một token.
     * @param token Chuỗi JWT.
     * @return User ID.
     */
    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Xác thực một chuỗi JWT.
     * @param authToken Chuỗi JWT cần xác thực.
     * @return true nếu token hợp lệ, false nếu không.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // Log ở mức TRACE hoặc INFO vì token hết hạn là chuyện bình thường, không phải lỗi hệ thống
            logger.trace("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}