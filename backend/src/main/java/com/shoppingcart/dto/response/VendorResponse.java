package com.shoppingcart.dto.response;

import java.math.BigDecimal;

public record VendorResponse(
        Long id,
        String name,
        String email,
        String phone,
        BigDecimal rating,
        boolean verified
) {
}
