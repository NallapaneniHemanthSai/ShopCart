package com.shoppingcart.dto.response;

import com.shoppingcart.entity.Role;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        Role role,
        Integer loyaltyPoints
) {
}
