package com.ebook.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<byte[]> favicon() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/x-icon");
        headers.add(HttpHeaders.CACHE_CONTROL, "public, max-age=604800"); // 7 days cache
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
    }
}
