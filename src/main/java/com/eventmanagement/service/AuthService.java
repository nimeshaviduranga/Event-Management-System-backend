package com.eventmanagement.service;

import com.eventmanagement.dto.auth.AuthResponse;
import com.eventmanagement.dto.auth.LoginRequest;
import com.eventmanagement.dto.auth.RegisterRequest;
import com.eventmanagement.entity.User;
import com.eventmanagement.mapper.AuthMapper;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.security.CustomUserDetails;
import com.eventmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    /**
     *Handle User Registration
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        User savedUser = userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        return createAuthResponse(token, savedUser);
    }

    /**
     * Handle User login
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not fount with the email"));

        return createAuthResponse(token, user);
    }


    private AuthResponse createAuthResponse(String token, User user) {
        AuthResponse.UserInfo userInfo = authMapper.toUserInfo(user);
        return new AuthResponse(token, userInfo);
    }
}