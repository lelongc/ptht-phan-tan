package com.ebook.dto;

import java.math.BigDecimal;

public record EbookResponse(
        Long id,
        String title,
        String author,
        String description,
        BigDecimal price,
        String coverUrl
) { }
