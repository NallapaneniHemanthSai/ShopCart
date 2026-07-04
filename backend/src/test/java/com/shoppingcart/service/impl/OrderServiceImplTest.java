package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.CheckoutRequest;
import com.shoppingcart.dto.response.OrderResponse;
import com.shoppingcart.entity.*;
import com.shoppingcart.exception.EmptyCartException;
import com.shoppingcart.exception.InsufficientStockException;
import com.shoppingcart.exception.OrderCancellationNotAllowedException;
import java.util.Optional;
import com.shoppingcart.repository.CartItemRepository;
import com.shoppingcart.repository.OrderRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.UserRepository;
import com.shoppingcart.service.CartService;
import com.shoppingcart.service.CouponService;
import com.shoppingcart.service.GstService;
import com.shoppingcart.service.pricing.DiscountStrategyFactory;
import com.shoppingcart.service.pricing.PercentageDiscountStrategy;
import com.shoppingcart.util.InvoiceNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CartService cartService;
    @Mock private CouponService couponService;
    @Mock private GstService gstService;
    @Mock private InvoiceNumberGenerator invoiceNumberGenerator;

    private OrderServiceImpl orderService;

    private Product mouse;
    private User user;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                cartItemRepository, productRepository, userRepository, orderRepository,
                cartService, couponService, gstService,
                new DiscountStrategyFactory(new PercentageDiscountStrategy(), new com.shoppingcart.service.pricing.FlatDiscountStrategy()),
                invoiceNumberGenerator
        );
        ReflectionTestUtils.setField(orderService, "deliveryChargeFlat", new BigDecimal("49.00"));
        ReflectionTestUtils.setField(orderService, "freeDeliveryThreshold", new BigDecimal("999.00"));

        mouse = Product.builder().id(10L).sku("ELE-001").name("Wireless Mouse")
                .category(Category.ELECTRONICS).price(new BigDecimal("699.00")).stock(50).active(true).build();
        user = User.builder().id(USER_ID).name("Test User").loyaltyPoints(0).build();

        lenient().when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        lenient().when(invoiceNumberGenerator.next()).thenReturn("INV-20260704-000001");
        lenient().when(gstService.currentRate()).thenReturn(new BigDecimal("18.00"));
    }

    private CartItem cartItemOf(Product product, int quantity) {
        return CartItem.builder().id(200L).user(user).product(product).quantity(quantity).addedAt(Instant.now()).build();
    }

    private CheckoutRequest checkoutRequest(String couponCode) {
        return checkoutRequest(couponCode, null);
    }

    private CheckoutRequest checkoutRequest(String couponCode, Integer redeemPoints) {
        return new CheckoutRequest(couponCode, redeemPoints, PaymentMethod.UPI, "Test User", "9876543210", "123 Main St");
    }

    @Test
    void checkout_withEmptyCart_throwsEmptyCartException() {
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout(USER_ID, checkoutRequest(null)))
                .isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_stockShrankSinceAddedToCart_throwsInsufficientStock() {
        mouse.setStock(1);
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(mouse, 3)));

        assertThatThrownBy(() -> orderService.checkout(USER_ID, checkoutRequest(null)))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_aboveFreeDeliveryThreshold_wavesDeliveryCharge() {
        // 699.00 x 2 = 1398.00, comfortably above the 999.00 free-delivery threshold
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(mouse, 2)));

        OrderResponse response = orderService.checkout(USER_ID, checkoutRequest(null));

        assertThat(response.subtotal()).isEqualByComparingTo("1398.00");
        assertThat(response.deliveryCharge()).isEqualByComparingTo("0.00");
        assertThat(response.gstAmount()).isEqualByComparingTo("251.64"); // 1398 * 18%
        assertThat(response.totalAmount()).isEqualByComparingTo("1649.64");
        assertThat(response.couponCode()).isNull();

        verify(productRepository).save(argThat(p -> p.getStock() == 48));
        verify(cartItemRepository).deleteByUserId(USER_ID);
        verify(cartService).clearHistory(USER_ID);
    }

    @Test
    void checkout_belowFreeDeliveryThreshold_appliesFlatDeliveryCharge() {
        Product cheapItem = Product.builder().id(11L).sku("BOO-001").name("Atomic Habits")
                .category(Category.BOOKS).price(new BigDecimal("100.00")).stock(10).active(true).build();
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(cheapItem, 1)));

        OrderResponse response = orderService.checkout(USER_ID, checkoutRequest(null));

        assertThat(response.subtotal()).isEqualByComparingTo("100.00");
        assertThat(response.deliveryCharge()).isEqualByComparingTo("49.00");
        assertThat(response.gstAmount()).isEqualByComparingTo("18.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("167.00"); // 100 + 18 + 49
    }

    @Test
    void checkout_withPercentageCoupon_discountsBeforeGstAndDelivery() {
        Coupon coupon = Coupon.builder().code("SAVE10").discountType(DiscountType.PERCENTAGE)
                .value(new BigDecimal("10")).minCartValue(new BigDecimal("500")).active(true).build();
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(mouse, 2))); // subtotal 1398.00
        when(couponService.requireValidCoupon(eq("SAVE10"), any())).thenReturn(coupon);

        OrderResponse response = orderService.checkout(USER_ID, checkoutRequest("SAVE10"));

        // subtotal 1398 - 10% (139.80) = taxable 1258.20 -> still above 999 threshold => free delivery
        assertThat(response.discountAmount()).isEqualByComparingTo("139.80");
        assertThat(response.deliveryCharge()).isEqualByComparingTo("0.00");
        assertThat(response.gstAmount()).isEqualByComparingTo("226.48"); // 1258.20 * 18% rounded
        assertThat(response.totalAmount()).isEqualByComparingTo("1484.68");
        assertThat(response.couponCode()).isEqualTo("SAVE10");
    }

    @Test
    void checkout_redeemingPoints_discountsBeforeGstAndDeductsBalance() {
        user.setLoyaltyPoints(200);
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(mouse, 2))); // subtotal 1398.00

        OrderResponse response = orderService.checkout(USER_ID, checkoutRequest(null, 100));

        // 1398 - 100 (points) = taxable 1298.00 -> GST 18% = 233.64, still above threshold => free delivery
        assertThat(response.pointsRedeemed()).isEqualTo(100);
        assertThat(response.gstAmount()).isEqualByComparingTo("233.64");
        assertThat(response.totalAmount()).isEqualByComparingTo("1531.64");
        // earned = floor(1531.64 / 100) = 15; balance = 200 - 100 + 15 = 115
        assertThat(user.getLoyaltyPoints()).isEqualTo(115);
        assertThat(response.pointsEarned()).isEqualTo(15);
    }

    @Test
    void checkout_redeemingMorePointsThanOwned_throwsInsufficientLoyaltyPoints() {
        user.setLoyaltyPoints(10);
        when(cartItemRepository.findByUserIdOrderByAddedAtAsc(USER_ID))
                .thenReturn(List.of(cartItemOf(mouse, 2)));

        assertThatThrownBy(() -> orderService.checkout(USER_ID, checkoutRequest(null, 50)))
                .isInstanceOf(com.shoppingcart.exception.InsufficientLoyaltyPointsException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancel_completedOrder_restocksItemsAndMarksCancelled() {
        OrderItem orderItem = OrderItem.builder()
                .productSku("ELE-001").productName("Wireless Mouse").category(Category.ELECTRONICS)
                .unitPrice(new BigDecimal("699.00")).quantity(2).lineTotal(new BigDecimal("1398.00")).build();
        Order order = Order.builder()
                .invoiceNumber("INV-1").user(user).status(OrderStatus.COMPLETED)
                .subtotal(new BigDecimal("1398.00")).gstRate(new BigDecimal("18")).gstAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO).deliveryCharge(BigDecimal.ZERO).totalAmount(new BigDecimal("1398.00"))
                .paymentMethod(PaymentMethod.UPI).shippingName("Test User").shippingPhone("123").shippingAddress("addr")
                .build();
        order.getItems().add(orderItem);
        mouse.setStock(48);

        when(orderRepository.findByInvoiceNumber("INV-1")).thenReturn(Optional.of(order));
        when(productRepository.findBySku("ELE-001")).thenReturn(Optional.of(mouse));

        OrderResponse response = orderService.cancel(USER_ID, false, "INV-1");

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(mouse.getStock()).isEqualTo(50); // 48 + 2 restocked
        verify(orderRepository).save(order);
    }

    @Test
    void cancel_alreadyCancelledOrder_throwsOrderCancellationNotAllowed() {
        Order order = Order.builder()
                .invoiceNumber("INV-1").user(user).status(OrderStatus.CANCELLED).items(new java.util.ArrayList<>())
                .build();
        when(orderRepository.findByInvoiceNumber("INV-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(USER_ID, false, "INV-1"))
                .isInstanceOf(OrderCancellationNotAllowedException.class);
    }
}
