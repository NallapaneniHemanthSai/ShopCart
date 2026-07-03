package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.CheckoutRequest;
import com.shoppingcart.dto.response.OrderResponse;
import com.shoppingcart.entity.CartItem;
import com.shoppingcart.entity.Coupon;
import com.shoppingcart.entity.Order;
import com.shoppingcart.entity.OrderItem;
import com.shoppingcart.entity.OrderStatus;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.User;
import com.shoppingcart.exception.EmptyCartException;
import com.shoppingcart.exception.InsufficientStockException;
import com.shoppingcart.exception.OrderCancellationNotAllowedException;
import com.shoppingcart.exception.OrderNotFoundException;
import com.shoppingcart.mapper.OrderMapper;
import com.shoppingcart.repository.CartItemRepository;
import com.shoppingcart.repository.OrderRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.UserRepository;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.CouponService;
import com.shoppingcart.service.GstService;
import com.shoppingcart.service.OrderService;
import com.shoppingcart.service.pricing.DiscountStrategyFactory;
import com.shoppingcart.util.InvoiceNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final GstService gstService;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    @Value("${app.cart.delivery-charge}")
    private BigDecimal deliveryChargeFlat;

    @Value("${app.cart.free-delivery-threshold}")
    private BigDecimal freeDeliveryThreshold;

    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedAtAsc(userId);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        // Re-validate stock at checkout time in case it changed since items were added to the cart.
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStock()) {
                throw new InsufficientStockException(product.getSku(), item.getQuantity(), product.getStock());
            }
        }

        BigDecimal subtotal = cartItems.stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Coupon coupon = couponService.requireValidCoupon(request.couponCode(), subtotal);
            discountAmount = discountStrategyFactory.get(coupon.getDiscountType())
                    .calculate(subtotal, coupon.getValue());
            appliedCouponCode = coupon.getCode();
        }

        BigDecimal taxableAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        BigDecimal gstRate = gstService.currentRate();
        BigDecimal gstAmount = taxableAmount.multiply(gstRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal deliveryCharge = taxableAmount.compareTo(freeDeliveryThreshold) >= 0
                ? BigDecimal.ZERO
                : deliveryChargeFlat;

        BigDecimal totalAmount = taxableAmount.add(gstAmount).add(deliveryCharge);

        User userRef = userRepository.getReferenceById(userId);
        Order order = Order.builder()
                .invoiceNumber(invoiceNumberGenerator.next())
                .user(userRef)
                .subtotal(subtotal)
                .gstRate(gstRate)
                .gstAmount(gstAmount)
                .couponCode(appliedCouponCode)
                .discountAmount(discountAmount)
                .deliveryCharge(deliveryCharge)
                .totalAmount(totalAmount)
                .paymentMethod(request.paymentMethod())
                .status(OrderStatus.COMPLETED)
                .shippingName(request.shippingName())
                .shippingPhone(request.shippingPhone())
                .shippingAddress(request.shippingAddress())
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .category(product.getCategory())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .lineTotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
            order.getItems().add(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);
        cartItemRepository.deleteByUserId(userId);
        cartService.clearHistory(userId);

        return OrderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long userId, boolean isAdmin, String invoiceNumber) {
        Order order = requireOrderEntity(userId, isAdmin, invoiceNumber);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationNotAllowedException(invoiceNumber);
        }

        for (OrderItem item : order.getItems()) {
            productRepository.findBySku(item.getProductSku()).ifPresent(product -> {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return OrderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponse> history(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getByInvoiceNumber(Long userId, boolean isAdmin, String invoiceNumber) {
        return OrderMapper.toResponse(requireOrderEntity(userId, isAdmin, invoiceNumber));
    }

    @Override
    public Order requireOrderEntity(Long userId, boolean isAdmin, String invoiceNumber) {
        Order order = orderRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new OrderNotFoundException(invoiceNumber));
        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException(invoiceNumber);
        }
        return order;
    }
}
