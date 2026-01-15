package com.ebook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.ebook.dto.CreateOrderRequest;
import com.ebook.dto.CreateOrderResponse;
import com.ebook.dto.OrderStatusResponse;
import com.ebook.service.OrderService;
import com.ebook.service.PaypalService;

import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaypalService paypalService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Validated @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{secretCode}/status")
    public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable String secretCode) {
        return ResponseEntity.ok(orderService.getStatus(secretCode));
    }

    @PostMapping("/{secretCode}/paypal/create-order")
    public ResponseEntity<Map<String, String>> createPayPalOrder(@PathVariable String secretCode) {
        try {
            OrderStatusResponse order = orderService.getStatus(secretCode);
            String paypalOrderId = paypalService.createOrder(secretCode, order.amount());
            Map<String, String> response = new HashMap<>();
            response.put("orderId", paypalOrderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{secretCode}/paypal/capture")
    public ResponseEntity<Map<String, Object>> capturePayPalOrder(@PathVariable String secretCode, @RequestParam String paypalOrderId) {
        try {
            boolean captured = paypalService.captureOrder(paypalOrderId);
            if (captured) {
                orderService.markPaid(secretCode, "PAYPAL");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment successful");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Capture failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}

