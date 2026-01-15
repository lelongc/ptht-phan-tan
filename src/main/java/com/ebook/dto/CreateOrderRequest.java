package com.ebook.dto;

import com.ebook.enums.PaymentMethod;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @Email @NotBlank String email,
        @NotNull PaymentMethod paymentMethod,
        Long ebookId
) { }
