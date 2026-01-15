package com.ebook.dto;

public record CreateOrderResponse(
        String secretCode,
        String paymentUrl,
        String qrImageUrl
) { }
