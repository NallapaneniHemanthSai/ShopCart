package com.shoppingcart.controller;

import com.shoppingcart.dto.response.UserProfileResponse;
import com.shoppingcart.security.CustomUserDetails;
import com.shoppingcart.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(authService.getProfile(principal.getId()));
    }
}
