package com.ebook.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VietQRService {

    @Value("${vietqr.bank-code:MB}")
    private String bankCode;

    @Value("${vietqr.account-number:0987214065}")
    private String accountNumber;

    @Value("${vietqr.account-name:Long%20Ebook}")
    private String accountName;

    public String buildQrImageUrl(BigDecimal amountVnd, String addInfo) {
        String encodedInfo = urlEncode(addInfo);
        String encodedName = accountName.contains("%") ? accountName : urlEncode(accountName);
        String amount = amountVnd.stripTrailingZeros().toPlainString();
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.jpg?amount=%s&addInfo=%s&accountName=%s",
                bankCode,
                accountNumber,
                amount,
                encodedInfo,
                encodedName
        );
    }

    private String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return input;
        }
    }
}
