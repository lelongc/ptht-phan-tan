package com.ebook.entity;

import com.ebook.enums.WebhookProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private WebhookProvider provider;

    private Long orderId;

    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    private Integer statusCode;

    private String responseMessage;
}
