package com.shoppingcart.service.impl;

import com.shoppingcart.dto.request.ProductCreateRequest;
import com.shoppingcart.dto.request.ProductUpdateRequest;
import com.shoppingcart.dto.request.StockAdjustRequest;
import com.shoppingcart.dto.response.ProductResponse;
import com.shoppingcart.entity.Category;
import com.shoppingcart.entity.Product;
import com.shoppingcart.entity.Vendor;
import com.shoppingcart.exception.DuplicateSkuException;
import com.shoppingcart.exception.ProductNotFoundException;
import com.shoppingcart.mapper.ProductMapper;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.repository.RatingAggregate;
import com.shoppingcart.service.ProductService;
import com.shoppingcart.service.ReviewService;
import com.shoppingcart.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final int RECENTLY_VIEWED_LIMIT = 10;
    private static final int LOW_STOCK_DEFAULT_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final VendorService vendorService;
    private final ReviewService reviewService;

    /** Per-user recently-viewed SKUs, most-recent-first, deduplicated, bounded by RECENTLY_VIEWED_LIMIT. */
    private final Map<Long, Deque<String>> recentlyViewedByUser = new ConcurrentHashMap<>();

    @Override
    public List<ProductResponse> listAll() {
        Map<Long, RatingAggregate> ratings = reviewService.aggregateAllRatings();
        return productRepository.findByActiveTrue().stream()
                .map(p -> mapWithRating(p, ratings))
                .toList();
    }

    @Override
    public List<ProductResponse> listAllForAdmin() {
        Map<Long, RatingAggregate> ratings = reviewService.aggregateAllRatings();
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getSku))
                .map(p -> mapWithRating(p, ratings))
                .toList();
    }

    @Override
    public List<ProductResponse> search(String query, Category category, String sortBy) {
        List<Product> results;
        if (category != null) {
            results = productRepository.findByCategoryAndActiveTrue(category);
        } else if (query != null && !query.isBlank()) {
            results = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query.trim());
        } else {
            results = productRepository.findByActiveTrue();
        }

        if (query != null && !query.isBlank() && category != null) {
            String needle = query.trim().toLowerCase();
            results = results.stream()
                    .filter(p -> p.getName().toLowerCase().contains(needle) || p.getSku().toLowerCase().contains(needle))
                    .toList();
        }

        Comparator<Product> comparator = resolveComparator(sortBy);
        Map<Long, RatingAggregate> ratings = reviewService.aggregateAllRatings();
        return results.stream()
                .sorted(comparator)
                .map(p -> mapWithRating(p, ratings))
                .toList();
    }

    private ProductResponse mapWithRating(Product product, Map<Long, RatingAggregate> ratings) {
        RatingAggregate agg = ratings.get(product.getId());
        if (agg == null || agg.getAvgRating() == null) {
            return ProductMapper.toResponse(product);
        }
        BigDecimal avg = BigDecimal.valueOf(agg.getAvgRating()).setScale(1, RoundingMode.HALF_UP);
        return ProductMapper.toResponse(product, avg, agg.getReviewCount());
    }

    private Comparator<Product> resolveComparator(String sortBy) {
        if (sortBy == null) {
            return Comparator.comparing(Product::getSku);
        }
        return switch (sortBy.toLowerCase()) {
            case "price" -> Comparator.comparing(Product::getPrice);
            case "price_desc" -> Comparator.comparing(Product::getPrice).reversed();
            case "stock" -> Comparator.comparing(Product::getStock);
            case "category" -> Comparator.comparing(Product::getCategory).thenComparing(Product::getName);
            case "name" -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Product::getSku);
        };
    }

    @Override
    public ProductResponse getBySku(String sku) {
        Product product = findEntity(sku);
        RatingAggregate agg = reviewService.aggregateRatingFor(product.getId());
        if (agg == null || agg.getAvgRating() == null) {
            return ProductMapper.toResponse(product);
        }
        BigDecimal avg = BigDecimal.valueOf(agg.getAvgRating()).setScale(1, RoundingMode.HALF_UP);
        return ProductMapper.toResponse(product, avg, agg.getReviewCount());
    }

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }
        Vendor vendor = vendorService.requireById(request.vendorId());
        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .vendor(vendor)
                .price(request.price())
                .stock(request.stock())
                .active(true)
                .imageUrl(request.imageUrl())
                .build();
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(String sku, ProductUpdateRequest request) {
        Product product = findEntity(sku);
        Vendor vendor = vendorService.requireById(request.vendorId());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setVendor(vendor);
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(request.active());
        product.setImageUrl(request.imageUrl());
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse adjustStock(String sku, StockAdjustRequest request) {
        Product product = findEntity(sku);
        int newStock = product.getStock() + request.delta();
        if (newStock < 0) {
            newStock = 0;
        }
        product.setStock(newStock);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deactivate(String sku) {
        Product product = findEntity(sku);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public List<ProductResponse> lowStock(int threshold) {
        int effectiveThreshold = threshold > 0 ? threshold : LOW_STOCK_DEFAULT_THRESHOLD;
        return productRepository.findByStockLessThanEqualAndActiveTrue(effectiveThreshold).stream()
                .sorted(Comparator.comparing(Product::getStock))
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public void recordView(Long userId, String sku) {
        if (!productRepository.existsBySku(sku)) {
            throw new ProductNotFoundException(sku);
        }
        Deque<String> viewed = recentlyViewedByUser.computeIfAbsent(userId, id -> new ArrayDeque<>());
        synchronized (viewed) {
            viewed.remove(sku);
            viewed.addFirst(sku);
            while (viewed.size() > RECENTLY_VIEWED_LIMIT) {
                viewed.removeLast();
            }
        }
    }

    @Override
    public List<ProductResponse> recentlyViewed(Long userId) {
        Map<Long, RatingAggregate> ratings = reviewService.aggregateAllRatings();
        Deque<String> viewed = recentlyViewedByUser.getOrDefault(userId, new ArrayDeque<>());
        synchronized (viewed) {
            return viewed.stream()
                    .map(sku -> productRepository.findBySku(sku).orElse(null))
                    .filter(p -> p != null && p.isActive())
                    .map(p -> mapWithRating(p, ratings))
                    .toList();
        }
    }

    private Product findEntity(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }
}
