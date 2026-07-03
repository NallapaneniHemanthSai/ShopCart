package com.shoppingcart.controller;

import com.shoppingcart.dto.request.CheckoutRequest;
import com.shoppingcart.dto.response.OrderResponse;
import com.shoppingcart.entity.Order;
import com.shoppingcart.entity.Role;
import com.shoppingcart.security.CustomUserDetails;
import com.shoppingcart.service.FileExportService;
import com.shoppingcart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final FileExportService fileExportService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(principal.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> history(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(orderService.history(principal.getId()));
    }

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<OrderResponse> getOne(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @PathVariable String invoiceNumber) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        return ResponseEntity.ok(orderService.getByInvoiceNumber(principal.getId(), isAdmin, invoiceNumber));
    }

    @PostMapping("/{invoiceNumber}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @PathVariable String invoiceNumber) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        return ResponseEntity.ok(orderService.cancel(principal.getId(), isAdmin, invoiceNumber));
    }

    @GetMapping("/{invoiceNumber}/export")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal CustomUserDetails principal,
                                          @PathVariable String invoiceNumber,
                                          @RequestParam(defaultValue = "txt") String format) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        Order order = orderService.requireOrderEntity(principal.getId(), isAdmin, invoiceNumber);

        byte[] content;
        MediaType mediaType;
        String extension;
        if ("csv".equalsIgnoreCase(format)) {
            content = fileExportService.toCsv(order);
            mediaType = MediaType.valueOf("text/csv");
            extension = "csv";
        } else {
            content = fileExportService.toTxt(order);
            mediaType = MediaType.TEXT_PLAIN;
            extension = "txt";
        }

        String filename = order.getInvoiceNumber() + "." + extension;
        ContentDisposition disposition = ContentDisposition.attachment().filename(filename).build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(content);
    }
}
