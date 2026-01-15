package com.ebook.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ebook.entity.Order;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenService {

    private final byte[] secret;
    private final int expirationHours;

    public TokenService(
            @Value("${app.jwt.secret:change-me-secret-key}") String secretKey,
            @Value("${app.jwt.expiration-hours:24}") int expirationHours) {
        this.secret = secretKey.getBytes();
        this.expirationHours = expirationHours;
    }

    public String generateDownloadToken(Order order) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationHours * 3600L);
        return Jwts.builder()
                .setSubject("download")
                .claim("orderId", order.getId())
                .claim("secretCode", order.getSecretCode())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret), SignatureAlgorithm.HS256)
                .compact();
    }
}
