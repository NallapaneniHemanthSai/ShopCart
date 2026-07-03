package com.shoppingcart.service;

import com.shoppingcart.dto.request.CheckoutRequest;
import com.shoppingcart.dto.response.OrderResponse;
import com.shoppingcart.entity.Order;

import java.util.List;

public interface OrderService {

    OrderResponse checkout(Long userId, CheckoutRequest request);

    List<OrderResponse> history(Long userId);

    OrderResponse getByInvoiceNumber(Long userId, boolean isAdmin, String invoiceNumber);

    Order requireOrderEntity(Long userId, boolean isAdmin, String invoiceNumber);
}
