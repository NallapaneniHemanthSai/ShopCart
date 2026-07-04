package com.shoppingcart.service;

import com.shoppingcart.dto.request.LoginRequest;
import com.shoppingcart.dto.request.RegisterRequest;
import com.shoppingcart.dto.response.AuthResponse;
import com.shoppingcart.dto.response.UserProfileResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getProfile(Long userId);
}
