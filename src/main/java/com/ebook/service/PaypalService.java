package com.ebook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaypalService {

    @Value("${paypal.mode:sandbox}")
    private String mode;

    public String createPaymentUrl(String secretCode) {
        // Placeholder URL for sandbox; integrate real PayPal SDK later.
        return "https://www.sandbox.paypal.com/checkoutnow?token=" + secretCode;
    }

    public boolean isSandbox() {
        return "sandbox".equalsIgnoreCase(mode);
    }
}
