package com.shoppingcart.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        BigDecimal averageOrderValue,
        List<TopProduct> topSellingProducts,
        List<ProductResponse> lowStockProducts
) {
    public record TopProduct(String sku, String name, long unitsSold, BigDecimal revenue) {
    }
}
