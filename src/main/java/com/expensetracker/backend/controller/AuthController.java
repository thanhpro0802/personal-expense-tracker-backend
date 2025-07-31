package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.User;
import com.expensetracker.backend.payload.request.LoginRequest;
import com.expensetracker.backend.payload.request.SignupRequest;
import com.expensetracker.backend.payload.response.JwtResponse;
import com.expensetracker.backend.payload.response.MessageResponse;
import com.expensetracker.backend.security.jwt.JwtUtils;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173") // Đảm bảo CORS cho auth endpoints
@RestController
@RequestMapping("/api/auth") // Endpoint cho xác thực
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService; // Sử dụng UserService để tạo người dùng
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Thực hiện xác thực
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Đặt đối tượng Authentication vào SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,
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
            userService.createUser(user); // Gọi UserService để lưu người dùng
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}