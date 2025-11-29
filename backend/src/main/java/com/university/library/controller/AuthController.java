package com.university.library.controller;

import com.university.library.dto.AuthResponse;
import com.university.library.dto.LoginRequest;
import com.university.library.dto.UserRequest;
import com.university.library.dto.UserResponse;
import com.university.library.entity.User;
import com.university.library.exception.BadRequestException;
import com.university.library.exception.ResourceNotFoundException;
import com.university.library.repository.UserRepository;
import com.university.library.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("用户名或密码错误"));
        if (!user.isActive()) {
            throw new BadRequestException("用户已停用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                UUID.randomUUID().toString()
        );
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }
}
