package com.shoppingcart.service;

import com.shoppingcart.dto.request.ProductCreateRequest;
import com.shoppingcart.dto.request.ProductUpdateRequest;
import com.shoppingcart.dto.request.StockAdjustRequest;
import com.shoppingcart.dto.response.ProductResponse;
import com.shoppingcart.entity.Category;

import java.util.List;

public interface ProductService {

    List<ProductResponse> listAll();

    List<ProductResponse> listAllForAdmin();

    List<ProductResponse> search(String query, Category category, String sortBy);

    ProductResponse getBySku(String sku);

    ProductResponse create(ProductCreateRequest request);

    ProductResponse update(String sku, ProductUpdateRequest request);

    ProductResponse adjustStock(String sku, StockAdjustRequest request);

    void deactivate(String sku);

    List<ProductResponse> lowStock(int threshold);

    void recordView(Long userId, String sku);

    List<ProductResponse> recentlyViewed(Long userId);
}
