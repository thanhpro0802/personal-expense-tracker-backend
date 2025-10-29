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
import com.expensetracker.backend.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173") // Đảm bảo CORS cho auth endpoints
@RestController
@RequestMapping("/api/auth") // Endpoint cho xác thực
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService; // Sử dụng UserService để tạo người dùng
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final WalletService walletService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, 
                         JwtUtils jwtUtils, RefreshTokenService refreshTokenService,
                         WalletService walletService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.walletService = walletService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Thực hiện xác thực
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Đặt đối tượng Authentication vào SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT access token
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Tạo refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Kiểm tra username và email đã tồn tại chưa
        if (userService.getUserByUsername(signupRequest.getUsername()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userService.getUserByEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Tạo người dùng mới
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getName());
        user.setPasswordHash(signupRequest.getPassword()); // Mật khẩu sẽ được mã hóa trong UserService

        try {
            User newUser = userService.createUser(user); // Gọi UserService để lưu người dùng
            
            // Tạo ví mặc định cho người dùng mới
            walletService.createWallet("Ví cá nhân", newUser.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUserId(user.getId().toString());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails != null) {
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}