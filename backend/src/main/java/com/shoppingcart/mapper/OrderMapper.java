package com.shoppingcart.mapper;

import com.shoppingcart.dto.response.OrderItemResponse;
import com.shoppingcart.dto.response.OrderResponse;
import com.shoppingcart.entity.Order;
import com.shoppingcart.entity.OrderItem;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductSku(),
                item.getProductName(),
                item.getCategory(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderMapper::toResponse)
                .toList();
        return new OrderResponse(
                order.getInvoiceNumber(),
                items,
                order.getSubtotal(),
                order.getGstRate(),
                order.getGstAmount(),
                order.getCouponCode(),
                order.getDiscountAmount(),
                order.getPointsRedeemed(),
                order.getPointsEarned(),
                order.getDeliveryCharge(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getShippingName(),
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getCreatedAt()
        );
    }
}
