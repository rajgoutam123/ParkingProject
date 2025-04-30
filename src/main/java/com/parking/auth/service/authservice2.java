package com.parking.auth.service;

import com.parking.auth.entity.User;
import com.parking.auth.repository.UserRepository;
import com.parking.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        log.info("User registered: {}", user.getUsername());
        return "User registered successfully!";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(password, user.getPassword())) {
            log.info("User logged in: {}", username);
            return jwtUtil.generateToken(username);
        } else {
            log.warn("Invalid login attempt for user: {}", username);
            throw new RuntimeException("Invalid credentials");
        }
    }
}
