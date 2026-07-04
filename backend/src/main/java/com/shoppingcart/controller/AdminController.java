package com.shoppingcart.controller;

import com.shoppingcart.dto.request.CouponRequest;
import com.shoppingcart.dto.request.GstUpdateRequest;
import com.shoppingcart.dto.request.ProductCreateRequest;
import com.shoppingcart.dto.request.ProductUpdateRequest;
import com.shoppingcart.dto.request.StockAdjustRequest;
import com.shoppingcart.dto.request.VendorRequest;
import com.shoppingcart.dto.response.AnalyticsResponse;
import com.shoppingcart.dto.response.CouponResponse;
import com.shoppingcart.dto.response.GstConfigResponse;
import com.shoppingcart.dto.response.ProductResponse;
import com.shoppingcart.dto.response.VendorResponse;
import com.shoppingcart.service.AnalyticsService;
import com.shoppingcart.service.CouponService;
import com.shoppingcart.service.GstService;
import com.shoppingcart.service.ProductService;
import com.shoppingcart.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CouponService couponService;
    private final GstService gstService;
    private final AnalyticsService analyticsService;
    private final VendorService vendorService;

    // ---- Product & inventory management ----

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> listAllProducts() {
        return ResponseEntity.ok(productService.listAllForAdmin());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }

    @PutMapping("/products/{sku}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String sku,
                                                           @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(sku, request));
    }

    @PatchMapping("/products/{sku}/stock")
    public ResponseEntity<ProductResponse> adjustStock(@PathVariable String sku,
                                                         @Valid @RequestBody StockAdjustRequest request) {
        return ResponseEntity.ok(productService.adjustStock(sku, request));
    }

    @DeleteMapping("/products/{sku}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable String sku) {
        productService.deactivate(sku);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<List<ProductResponse>> lowStock(@RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.lowStock(threshold));
    }

    // ---- Coupon management ----

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponResponse>> listCoupons() {
        return ResponseEntity.ok(couponService.listAll());
    }

    @PostMapping("/coupons")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.create(request));
    }

    @PutMapping("/coupons/{code}")
    public ResponseEntity<CouponResponse> updateCoupon(@PathVariable String code,
                                                         @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.update(code, request));
    }

    @DeleteMapping("/coupons/{code}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String code) {
        couponService.delete(code);
        return ResponseEntity.noContent().build();
    }

    // ---- GST configuration ----

    @GetMapping("/gst")
    public ResponseEntity<GstConfigResponse> getGst() {
        return ResponseEntity.ok(gstService.getConfig());
    }

    @PutMapping("/gst")
    public ResponseEntity<GstConfigResponse> updateGst(@Valid @RequestBody GstUpdateRequest request) {
        return ResponseEntity.ok(gstService.updateRate(request.ratePercent()));
    }

    // ---- Vendor management ----

    @GetMapping("/vendors")
    public ResponseEntity<List<VendorResponse>> listVendors() {
        return ResponseEntity.ok(vendorService.listAll());
    }

    @PostMapping("/vendors")
    public ResponseEntity<VendorResponse> createVendor(@Valid @RequestBody VendorRequest request) {
        return ResponseEntity.ok(vendorService.create(request));
    }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<VendorResponse> updateVendor(@PathVariable Long id,
                                                        @Valid @RequestBody VendorRequest request) {
        return ResponseEntity.ok(vendorService.update(id, request));
    }

    // ---- Analytics ----

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> analytics() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}
