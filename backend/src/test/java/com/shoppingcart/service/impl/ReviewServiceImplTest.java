package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.ReviewRequest;
import com.shoppingcart.dto.response.ReviewResponse;
import com.shoppingcart.entity.Category;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.Review;
import com.shoppingcart.entity.User;
import com.shoppingcart.exception.DuplicateReviewException;
import com.shoppingcart.exception.ReviewNotAllowedException;
import com.shoppingcart.repository.OrderItemRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.ReviewRepository;
import com.shoppingcart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String SKU = "ELE-001";

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderItemRepository orderItemRepository;

    private ReviewServiceImpl reviewService;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, productRepository, userRepository, orderItemRepository);
        product = Product.builder().id(10L).sku(SKU).name("Wireless Mouse")
                .category(Category.ELECTRONICS).price(new BigDecimal("699.00")).stock(50).active(true).build();
        user = User.builder().id(USER_ID).name("Test User").build();
    }

    @Test
    void addReview_userNeverPurchasedProduct_throwsReviewNotAllowed() {
        when(productRepository.findBySku(SKU)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProduct_SkuAndUser_Id(SKU, USER_ID)).thenReturn(false);
        when(orderItemRepository.existsByProductSkuAndOrder_User_Id(SKU, USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.addReview(USER_ID, SKU, new ReviewRequest(5, "Great!")))
                .isInstanceOf(ReviewNotAllowedException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_alreadyReviewed_throwsDuplicateReview() {
        when(productRepository.findBySku(SKU)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProduct_SkuAndUser_Id(SKU, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.addReview(USER_ID, SKU, new ReviewRequest(4, "Good")))
                .isInstanceOf(DuplicateReviewException.class);

        verify(orderItemRepository, never()).existsByProductSkuAndOrder_User_Id(any(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_verifiedPurchaseAndFirstReview_savesAndReturnsResponse() {
        when(productRepository.findBySku(SKU)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProduct_SkuAndUser_Id(SKU, USER_ID)).thenReturn(false);
        when(orderItemRepository.existsByProductSkuAndOrder_User_Id(SKU, USER_ID)).thenReturn(true);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewResponse response = reviewService.addReview(USER_ID, SKU, new ReviewRequest(5, "Excellent mouse"));

        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.reviewerName()).isEqualTo("Test User");
        assertThat(response.verifiedPurchase()).isTrue();

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getRating()).isEqualTo(5);
        assertThat(captor.getValue().getComment()).isEqualTo("Excellent mouse");
    }
}
