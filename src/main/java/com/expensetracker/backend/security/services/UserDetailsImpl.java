package com.expensetracker.backend.security.services;

import com.expensetracker.backend.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections; // Để trả về empty list cho authorities
import java.util.Objects;
import java.util.UUID;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String username;
    private String email;

    @JsonIgnore // Không hiển thị password_hash khi serialize thành JSON
    private String password;

    // Hiện tại chúng ta không có vai trò (roles) trong thiết kế database
    // Nhưng UserDetails cần phương thức này.
    // Nếu sau này bạn thêm Roles, bạn sẽ cần triển khai nó tại đây.
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(UUID id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        // Hiện tại không có authorities/roles, nên trả về empty list
        this.authorities = Collections.emptyList();
    }

    // Phương thức tĩnh để xây dựng UserDetailsImpl từ đối tượng User
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash());
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không hết hạn (luôn true trong ứng dụng này)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Tài khoản không bị khóa (luôn true)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Thông tin đăng nhập không hết hạn (luôn true)
    }

    @Override
    public boolean isEnabled() {
        return true; // Tài khoản được kích hoạt (luôn true)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}