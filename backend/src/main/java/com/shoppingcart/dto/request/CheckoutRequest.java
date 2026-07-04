package com.shoppingcart.dto.request;

import com.shoppingcart.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        String couponCode,
        @Min(value = 0, message = "Redeemed points cannot be negative") Integer redeemPoints,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        @NotBlank(message = "Shipping name is required") String shippingName,
        @NotBlank(message = "Shipping phone is required") String shippingPhone,
        @NotBlank(message = "Shipping address is required") String shippingAddress
) {
}
