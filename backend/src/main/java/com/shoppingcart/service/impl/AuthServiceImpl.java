package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.LoginRequest;
import com.shoppingcart.dto.request.RegisterRequest;
import com.shoppingcart.dto.response.AuthResponse;
import com.shoppingcart.dto.response.UserProfileResponse;
import com.shoppingcart.entity.Role;
import com.shoppingcart.entity.User;
import com.shoppingcart.exception.DuplicateEmailException;
import com.shoppingcart.exception.InvalidCredentialsException;
import com.shoppingcart.repository.UserRepository;
import com.shoppingcart.security.CustomUserDetails;
import com.shoppingcart.security.JwtService;
import com.shoppingcart.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .phone(request.phone())
                .address(request.address())
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return buildAuthResponse(user);
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getLoyaltyPoints());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
