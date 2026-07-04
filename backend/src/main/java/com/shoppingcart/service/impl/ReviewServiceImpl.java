package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.ReviewRequest;
import com.shoppingcart.dto.response.ReviewResponse;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.Review;
import com.shoppingcart.entity.User;
import com.shoppingcart.exception.DuplicateReviewException;
import com.shoppingcart.exception.ProductNotFoundException;
import com.shoppingcart.exception.ReviewNotAllowedException;
import com.shoppingcart.mapper.ReviewMapper;
import com.shoppingcart.repository.OrderItemRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.RatingAggregate;
import com.shoppingcart.repository.ReviewRepository;
import com.shoppingcart.repository.UserRepository;
import com.shoppingcart.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public List<ReviewResponse> listForProduct(String sku) {
        return reviewRepository.findByProductSkuOrderByCreatedAtDesc(sku).stream()
                .map(ReviewMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse addReview(Long userId, String sku, ReviewRequest request) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));

        if (reviewRepository.existsByProduct_SkuAndUser_Id(sku, userId)) {
            throw new DuplicateReviewException(sku);
        }
        if (!orderItemRepository.existsByProductSkuAndOrder_User_Id(sku, userId)) {
            throw new ReviewNotAllowedException();
        }

        User userRef = userRepository.getReferenceById(userId);
        Review review = Review.builder()
                .product(product)
                .user(userRef)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        Review saved = reviewRepository.save(review);
        // Re-fetch the user's name for the response without relying on the lazy proxy post-save.
        User author = userRepository.findById(userId).orElseThrow();
        saved.setUser(author);
        return ReviewMapper.toResponse(saved);
    }

    @Override
    public Map<Long, RatingAggregate> aggregateAllRatings() {
        return reviewRepository.aggregateRatings().stream()
                .collect(java.util.stream.Collectors.toMap(RatingAggregate::getProductId, Function.identity()));
    }

    @Override
    public RatingAggregate aggregateRatingFor(Long productId) {
        return reviewRepository.aggregateRatingsForProduct(productId).orElse(null);
    }
}
