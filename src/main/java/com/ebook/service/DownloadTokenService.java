package com.ebook.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebook.entity.DownloadToken;
import com.ebook.entity.Order;
import com.ebook.enums.OrderStatus;
import com.ebook.repository.DownloadTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownloadTokenService {

    private final DownloadTokenRepository downloadTokenRepository;
    private final TokenService tokenService;

    @Value("${app.download.max-uses:5}")
    private int maxUses;

    @Transactional
    public String generateToken(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Order not paid");
        }
        String jwt = tokenService.generateDownloadToken(order);
        DownloadToken token = DownloadToken.builder()
                .order(order)
                .token(jwt)
                .expiresAt(order.getDownloadExpiresAt())
                .maxUses(maxUses)
                .build();
        downloadTokenRepository.save(token);
        return jwt;
    }

    @Transactional(readOnly = true)
    public DownloadToken get(String token) {
        return downloadTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
    }

    @Transactional
    public void incrementUsage(DownloadToken token) {
        token.setUsedCount(token.getUsedCount() + 1);
        if (token.getUsedCount() > token.getMaxUses()) {
            throw new IllegalStateException("Download limit reached");
        }
    }

    public boolean isExpired(DownloadToken token) {
        return token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now());
    }
}
