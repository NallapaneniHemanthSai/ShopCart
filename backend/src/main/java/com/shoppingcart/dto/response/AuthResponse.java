package com.shoppingcart.dto.response;

import com.shoppingcart.entity.Role;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        Role role
) {
}
