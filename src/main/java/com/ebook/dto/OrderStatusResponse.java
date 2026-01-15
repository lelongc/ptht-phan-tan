package com.ebook.dto;

import com.ebook.enums.OrderStatus;

public record OrderStatusResponse(
        OrderStatus status,
        String message
) { }
