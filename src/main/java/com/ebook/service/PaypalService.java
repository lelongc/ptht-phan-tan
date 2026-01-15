package com.ebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class PaypalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(mode) 
            ? "https://api-m.sandbox.paypal.com"
            : "https://api-m.paypal.com";
    }

    // Get access token from PayPal
    public String getAccessToken() {
        try {
            String url = getBaseUrl() + "/v1/oauth2/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PayPal access token", e);
        }
    }

    // Create PayPal order
    public String createOrder(String secretCode, double amount) {
        try {
            String accessToken = getAccessToken();
            String url = getBaseUrl() + "/v2/checkout/orders";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("intent", "CAPTURE");

            Map<String, Object> purchaseUnit = new HashMap<>();
            Map<String, Object> amountObj = new HashMap<>();
            amountObj.put("currency_code", "USD");
            amountObj.put("value", String.format("%.2f", amount / 25000)); // VND to USD
            purchaseUnit.put("amount", amountObj);
            purchaseUnit.put("custom_id", secretCode);

            body.put("purchase_units", Arrays.asList(purchaseUnit));

            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PayPal order", e);
        }
    }

    // Capture PayPal order (complete payment)
    public boolean captureOrder(String orderId) {
        try {
            String accessToken = getAccessToken();
            String url = getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            String status = json.get("status").asText();
            return "COMPLETED".equals(status);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSandbox() {
        return "sandbox".equalsIgnoreCase(mode);
    }
}

