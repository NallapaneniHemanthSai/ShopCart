package com.shoppingcart.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        int distinctItemCount,
        int totalQuantity,
        BigDecimal subtotal
) {
}
