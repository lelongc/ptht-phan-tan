package com.ebook.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceService {
    private final MessageSource messageSource;
    private static final BigDecimal VND_PER_USD = new BigDecimal("25000");

    public BigDecimal getPriceVnd() {
        String priceStr = messageSource.getMessage("ebook.price", null, "10000", Locale.forLanguageTag("vi"));
        return new BigDecimal(priceStr);
    }

    public BigDecimal getDisplayPrice(Locale locale) {
        BigDecimal vnd = getPriceVnd();
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return vnd.divide(VND_PER_USD, 2, RoundingMode.HALF_UP);
        }
        return vnd;
    }

    public String getDisplayCurrency(Locale locale) {
        return (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) ? "USD" : "VND";
    }

    // Convert any VND amount to a locale-aware display amount (USD for en, VND otherwise)
    public BigDecimal toDisplayAmount(BigDecimal vnd, Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return vnd.divide(VND_PER_USD, 2, RoundingMode.HALF_UP);
        }
        return vnd;
    }
}