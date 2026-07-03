package com.shoppingcart.controller;

import com.shoppingcart.dto.request.AddToCartRequest;
import com.shoppingcart.dto.request.UpdateCartItemRequest;
import com.shoppingcart.dto.response.CartResponse;
import com.shoppingcart.security.CustomUserDetails;
import com.shoppingcart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(cartService.getCart(principal.getId(), sortBy));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItem(principal.getId(), request));
    }

    @PutMapping("/items/{sku}")
    public ResponseEntity<CartResponse> updateItem(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable String sku,
                                                    @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(principal.getId(), sku, request));
    }

    @DeleteMapping("/items/{sku}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable String sku) {
        return ResponseEntity.ok(cartService.removeItem(principal.getId(), sku));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cartService.clearCart(principal.getId()));
    }

    @PostMapping("/undo")
    public ResponseEntity<CartResponse> undo(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cartService.undo(principal.getId()));
    }
}
