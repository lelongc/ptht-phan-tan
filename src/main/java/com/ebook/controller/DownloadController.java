package com.ebook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebook.entity.DownloadToken;
import com.ebook.service.DownloadTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadTokenService downloadTokenService;

    @GetMapping("/{token}")
    public ResponseEntity<String> download(@PathVariable String token) {
        DownloadToken t = downloadTokenService.get(token);
        if (downloadTokenService.isExpired(t)) {
            return ResponseEntity.badRequest().body("Token expired");
        }
        downloadTokenService.incrementUsage(t);
        // For demo: return placeholder. In production, generate S3 signed URL and redirect.
        return ResponseEntity.ok("Download granted for token: " + token);
    }
}
