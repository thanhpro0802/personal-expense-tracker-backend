package com.expensetracker.backend.security.jwt;

import com.expensetracker.backend.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * === PHƯƠNG THỨC QUAN TRỌNG NHẤT CẦN SỬA ===
     * Phương thức này phải tạo token với ID người dùng (UUID).
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // Lấy ID (là một đối tượng UUID) từ principal và chuyển nó thành chuỗi.
        String userIdAsString = userPrincipal.getId().toString();

        // Thêm log quan trọng để xác nhận code mới đang chạy
        logger.info("Attempting to generate token for user ID: {}", userIdAsString);

        return Jwts.builder()
                .setSubject(userIdAsString) // <-- Đảm bảo dòng này sử dụng ID, không phải getUsername()
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Phương thức này lấy ID người dùng từ một token đã có.
     */
    public String getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(authToken);
            return true;
        } catch (Exception e) {
            // Log lỗi nếu token không hợp lệ
            logger.error("Invalid JWT Token: {}", e.getMessage());
        }
        return false;
    }
}