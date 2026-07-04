package com.shoppingcart.dto.response;

import com.shoppingcart.entity.OrderStatus;
import com.shoppingcart.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String invoiceNumber,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal gstRate,
        BigDecimal gstAmount,
        String couponCode,
        BigDecimal discountAmount,
        Integer pointsRedeemed,
        Integer pointsEarned,
        BigDecimal deliveryCharge,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        OrderStatus status,
        String shippingName,
        String shippingPhone,
        String shippingAddress,
        Instant createdAt
) {
}
