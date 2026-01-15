package com.ebook.controller;

import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebook.dto.EbookResponse;
import com.ebook.service.EbookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ebook")
@RequiredArgsConstructor
public class EbookController {

    private final EbookService ebookService;

    @GetMapping
    public ResponseEntity<EbookResponse> getEbook(@RequestHeader(name = "Accept-Language", required = false) String lang) {
        Locale locale = lang != null ? Locale.forLanguageTag(lang) : Locale.forLanguageTag("vi");
        return ResponseEntity.ok(ebookService.getActiveEbook(locale));
    }
}
