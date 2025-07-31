package com.expensetracker.backend.controller;

import com.expensetracker.backend.exception.TokenRefreshException;
import com.expensetracker.backend.model.RefreshToken;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.payload.request.LoginRequest;
import com.expensetracker.backend.payload.request.SignupRequest;
import com.expensetracker.backend.payload.request.TokenRefreshRequest;
import com.expensetracker.backend.payload.response.JwtResponse;
import com.expensetracker.backend.payload.response.MessageResponse;
import com.expensetracker.backend.payload.response.TokenRefreshResponse;
import com.expensetracker.backend.security.jwt.JwtUtils;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.RefreshTokenService;
import com.expensetracker.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // --- SỬA LỖI 1: Gọi đúng tên phương thức đã được đổi tên ---
        // Tên mới là 'generateAccessToken' để rõ ràng hơn.
        String accessToken = jwtUtils.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(
                accessToken,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userService.getUserByUsername(signupRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userService.getUserByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Logic tạo user đã được chuyển vào UserService để tuân thủ SOLID
        try {
            userService.createUser(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An unexpected error occurred during registration."));
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // --- SỬA LỖI 2: Gọi đúng phương thức để tạo Access Token mới ---
                    // Chúng ta cần tạo một đối tượng UserDetailsImpl từ User để tạo token
                    UserDetailsImpl userDetails = UserDetailsImpl.build(user);
                    String newAccessToken = jwtUtils.generateAccessToken(userDetails);

                    // Trả về access token mới và refresh token cũ (hoặc có thể tạo mới cả refresh token)
                    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        // Lấy thông tin user từ SecurityContext
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            // Xóa refresh token khỏi DB để vô hiệu hóa nó
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}