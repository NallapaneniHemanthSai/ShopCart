package com.shoppingcart.controller;

import com.shoppingcart.dto.response.VendorResponse;
import com.shoppingcart.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    public ResponseEntity<List<VendorResponse>> listAll() {
        return ResponseEntity.ok(vendorService.listAll());
    }
}
