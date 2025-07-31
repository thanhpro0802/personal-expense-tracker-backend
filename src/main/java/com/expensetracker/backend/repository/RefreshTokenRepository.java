package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.RefreshToken;
import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Tìm một RefreshToken dựa trên chuỗi token của nó.
     * @param token Chuỗi token duy nhất.
     * @return Optional chứa RefreshToken nếu tìm thấy.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * --- PHƯƠNG THỨC MỚI ĐƯỢC THÊM VÀO ---
     * Tìm một RefreshToken dựa trên đối tượng người dùng sở hữu nó.
     * Điều này rất hữu ích để quản lý token của người dùng, ví dụ như xóa token cũ khi tạo token mới.
     *
     * @param user Đối tượng người dùng.
     * @return Optional chứa RefreshToken nếu người dùng đó có token.
     */
    Optional<RefreshToken> findByUser(User user);

    /**
     * Xóa một RefreshToken dựa trên đối tượng người dùng.
     * Thường được sử dụng khi người dùng đăng xuất.
     * @param user Đối tượng người dùng.
     * @return Số lượng bản ghi đã được xóa.
     */
    @Modifying
    int deleteByUser(User user);
}