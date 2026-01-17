package com.ebook.service;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final MessageSource messageSource;

    public BigDecimal getPrice(Locale locale) {
        String priceStr = messageSource.getMessage("ebook.price", null, "10000", locale);
        return new BigDecimal(priceStr);
    }

    public double getPriceDouble(Locale locale) {
        return getPrice(locale).doubleValue();
    }
}