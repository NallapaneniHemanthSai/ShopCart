package com.shoppingcart.controller;

import com.shoppingcart.dto.response.ProductResponse;
import com.shoppingcart.entity.Category;
import com.shoppingcart.security.CustomUserDetails;
import com.shoppingcart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listAll() {
        return ResponseEntity.ok(productService.listAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(productService.search(q, category, sortBy));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getBySku(sku));
    }

    @PostMapping("/{sku}/view")
    public ResponseEntity<Void> recordView(@PathVariable String sku,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        productService.recordView(principal.getId(), sku);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<List<ProductResponse>> recentlyViewed(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(productService.recentlyViewed(principal.getId()));
    }
}
