package com.shoppingcart.controller;

import com.shoppingcart.dto.response.WishlistItemResponse;
import com.shoppingcart.security.CustomUserDetails;
import com.shoppingcart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(wishlistService.list(principal.getId()));
    }

    @PostMapping("/{sku}")
    public ResponseEntity<List<WishlistItemResponse>> add(@AuthenticationPrincipal CustomUserDetails principal,
                                                            @PathVariable String sku) {
        return ResponseEntity.ok(wishlistService.add(principal.getId(), sku));
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<List<WishlistItemResponse>> remove(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @PathVariable String sku) {
        return ResponseEntity.ok(wishlistService.remove(principal.getId(), sku));
    }
}
