package com.ebook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebook.dto.CreateOrderRequest;
import com.ebook.dto.CreateOrderResponse;
import com.ebook.dto.OrderStatusResponse;
import com.ebook.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Validated @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{secretCode}/status")
    public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable String secretCode) {
        return ResponseEntity.ok(orderService.getStatus(secretCode));
    }
}
