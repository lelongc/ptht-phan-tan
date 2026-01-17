package com.ebook.controller;

import java.util.Map;
import java.util.regex.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebook.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final OrderService orderService;

    // Stub: mark paid when PayPal webhook hits
    @PostMapping("/paypal/{secretCode}")
    public ResponseEntity<String> paypalPaid(@PathVariable String secretCode, @RequestBody(required = false) Map<String, Object> body) {
        String txnId = body != null ? String.valueOf(body.getOrDefault("txnId", "PAYPAL-TXN")) : "PAYPAL-TXN";
        orderService.markPaid(secretCode, txnId);
        return ResponseEntity.ok("ok");
    }

    // Thêm webhook cho SePay
    @PostMapping("/sepay")
    public ResponseEntity<String> sepayWebhook(@RequestBody Map<String, Object> payload) {
        // System.out.println("SePay payload: " + payload);
        String description = (String) payload.get("description");
        String status = (String) payload.get("status"); // Có thể null, tuỳ payload SePay

        // Tìm mã đơn hàng dạng ORDERxxxxxx hoặc ORDER-xxxxxx ở bất kỳ vị trí nào
        String secretCode = null;
        if (description != null) {
            Pattern p = Pattern.compile("ORDER-?([A-Z0-9]+)");
            Matcher m = p.matcher(description);
            if (m.find()) {
                secretCode = m.group(1);
            }
        }

        if (secretCode != null /* && "SUCCESS".equalsIgnoreCase(status) */) {
            orderService.markPaid(secretCode, "SEPAY-TXN");
        }
        return ResponseEntity.ok("ok");
    }
}
