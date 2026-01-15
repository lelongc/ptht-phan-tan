package com.ebook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ebook.entity.WebhookLog;
import com.ebook.enums.WebhookProvider;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    Page<WebhookLog> findByProvider(WebhookProvider provider, Pageable pageable);
}
