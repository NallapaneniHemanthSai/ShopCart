package com.shoppingcart.service.impl;

import com.shoppingcart.dto.response.AnalyticsResponse;
import com.shoppingcart.dto.response.AnalyticsResponse.TopProduct;
import com.shoppingcart.entity.Order;
import com.shoppingcart.entity.OrderItem;
import com.shoppingcart.mapper.ProductMapper;
import com.shoppingcart.repository.OrderRepository;
import com.shoppingcart.repository.ProductRepository;
import com.shoppingcart.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int TOP_PRODUCT_LIMIT = 5;
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public AnalyticsResponse getAnalytics() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        List<TopProduct> topProducts = computeTopSellingProducts(orders);

        List<com.shoppingcart.dto.response.ProductResponse> lowStock = productRepository
                .findByStockLessThanEqualAndActiveTrue(LOW_STOCK_THRESHOLD).stream()
                .sorted(Comparator.comparing(p -> p.getStock()))
                .map(ProductMapper::toResponse)
                .toList();

        return new AnalyticsResponse(totalRevenue, totalOrders, averageOrderValue, topProducts, lowStock);
    }

    private List<TopProduct> computeTopSellingProducts(List<Order> orders) {
        record Agg(String sku, String name, long units, BigDecimal revenue) {
        }

        Map<String, Agg> aggregates = new LinkedHashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                aggregates.merge(item.getProductSku(),
                        new Agg(item.getProductSku(), item.getProductName(), item.getQuantity(), item.getLineTotal()),
                        (a, b) -> new Agg(a.sku(), a.name(), a.units() + b.units(), a.revenue().add(b.revenue())));
            }
        }

        return aggregates.values().stream()
                .sorted(Comparator.comparingLong(Agg::units).reversed())
                .limit(TOP_PRODUCT_LIMIT)
                .map(a -> new TopProduct(a.sku(), a.name(), a.units(), a.revenue()))
                .toList();
    }
}
