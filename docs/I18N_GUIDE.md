# 🌍 HƯỚNG DẪN ĐA NGÔN NGỮ (i18n) - TIẾNG VIỆT + TIẾNG ANH

> **Mục tiêu:** Website hỗ trợ 2 ngôn ngữ (Tiếng Việt và Tiếng Anh) với khả năng chuyển đổi linh hoạt

---

## 📋 MỤC LỤC

1. [Database Schema với Multi-language](#1-database-schema)
2. [Spring Boot i18n Configuration](#2-spring-boot-configuration)
3. [Language Files (Properties)](#3-language-files)
4. [Backend Implementation](#4-backend-implementation)
5. [Frontend Implementation](#5-frontend-implementation)
6. [Email Templates](#6-email-templates)
7. [Testing](#7-testing)

---

## 1. DATABASE SCHEMA

### 1.1 Update Ebooks Table

**Chiến lược:** Lưu nội dung đa ngôn ngữ trong các cột riêng biệt

```sql
CREATE TABLE ebooks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  -- Vietnamese content
  title_vi VARCHAR(255) NOT NULL,
  author_vi VARCHAR(255),
  description_vi TEXT,
  
  -- English content
  title_en VARCHAR(255) NOT NULL,
  author_en VARCHAR(255),
  description_en TEXT,
  
  -- Common fields (không cần dịch)
  price DECIMAL(10, 2) NOT NULL,
  cover_url VARCHAR(255),
  s3_key VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  status VARCHAR(20) DEFAULT 'ACTIVE',
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  INDEX idx_title_vi (title_vi),
  INDEX idx_title_en (title_en),
  INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 1.2 Seed Data với Đa Ngôn Ngữ

```sql
INSERT INTO ebooks (title_vi, title_en, author_vi, author_en, description_vi, description_en, price, cover_url, s3_key, category) VALUES
(
  'Alice Ở Xứ Sở Thần Tiên', 
  'Alice in Wonderland',
  'Lewis Carroll',
  'Lewis Carroll',
  'Câu chuyện cổ điển về cô gái rơi xuống hố thỏ vào thế giới kỳ ảo.',
  'A classic tale of a girl who falls through a rabbit hole into a fantasy world.',
  29000,
  '/covers/alice.jpg',
  'ebooks/alice.pdf',
  'Fiction'
),
(
  'Kiêu Hãnh và Định Kiến',
  'Pride and Prejudice',
  'Jane Austen',
  'Jane Austen',
  'Tiểu thuyết lãng mạn về phong cách sống của tầng lớp quý tộc Anh.',
  'A romantic novel of manners written by Jane Austen in 1813.',
  39000,
  '/covers/pride.jpg',
  'ebooks/pride.pdf',
  'Romance'
),
(
  'Cá Voi Trắng Moby Dick',
  'Moby Dick',
  'Herman Melville',
  'Herman Melville',
  'Câu chuyện về hành trình truy đuổi con cá voi trắng huyền thoại.',
  'The narrative of Captain Ahab\'s obsessive quest to kill the white whale.',
  49000,
  '/covers/moby.jpg',
  'ebooks/moby.pdf',
  'Adventure'
);
```

---

## 2. SPRING BOOT CONFIGURATION

### 2.1 Thêm Dependency (pom.xml)

```xml
<!-- Đã có sẵn trong Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2.2 I18nConfig.java

**Tạo file:** `src/main/java/com/ebook/config/I18nConfig.java`

```java
package com.ebook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * LocaleResolver: Quyết định locale dựa trên cookie
     * Default: Tiếng Việt (vi)
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(new Locale("vi")); // Mặc định tiếng Việt
        resolver.setCookieName("lang"); // Cookie name: "lang"
        resolver.setCookieMaxAge(Duration.ofDays(365)); // Cookie tồn tại 1 năm
        return resolver;
    }

    /**
     * LocaleChangeInterceptor: Cho phép thay đổi locale qua param ?lang=vi hoặc ?lang=en
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // URL param: ?lang=vi
        return interceptor;
    }

    /**
     * Đăng ký interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

### 2.3 application.properties

```properties
# Internationalization (i18n)
spring.messages.basename=i18n/messages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
spring.messages.cache-duration=3600

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.encoding=UTF-8
```

---

## 3. LANGUAGE FILES

### 3.1 Tạo Folder Structure

```
src/main/resources/
└── i18n/
    ├── messages_vi.properties
    └── messages_en.properties
```

### 3.2 messages_vi.properties (Tiếng Việt)

**Tạo file:** `src/main/resources/i18n/messages_vi.properties`

```properties
# ============================================
# COMMON
# ============================================
app.name=Cửa Hàng Ebook
app.tagline=Mua ebook chất lượng cao, giá tốt nhất

# Navigation
nav.home=Trang Chủ
nav.ebooks=Sách Điện Tử
nav.about=Giới Thiệu
nav.contact=Liên Hệ
nav.cart=Giỏ Hàng

# Language Switcher
lang.vietnamese=Tiếng Việt
lang.english=English
lang.switch=Đổi ngôn ngữ

# ============================================
# HOME PAGE
# ============================================
home.title=Khám Phá Kho Ebook
home.subtitle=Hơn 1000+ ebook chất lượng cao
home.search.placeholder=Tìm kiếm sách...
home.search.button=Tìm Kiếm
home.filter.category=Danh Mục
home.filter.price=Khoảng Giá
home.filter.all=Tất Cả

# Ebook Card
ebook.by=Tác giả:
ebook.price=Giá:
ebook.currency=₫
ebook.buy=Mua Ngay
ebook.details=Chi Tiết
ebook.add_cart=Thêm Vào Giỏ

# Categories
category.fiction=Văn Học
category.romance=Lãng Mạn
category.adventure=Phiêu Lưu
category.technology=Công Nghệ
category.business=Kinh Doanh

# ============================================
# CHECKOUT PAGE
# ============================================
checkout.title=Thanh Toán
checkout.ebook=Sách Điện Tử
checkout.price=Giá
checkout.email=Email Của Bạn
checkout.email.placeholder=nguyen@example.com
checkout.email.helper=Link tải sách sẽ được gửi qua email này
checkout.payment.method=Phương Thức Thanh Toán
checkout.payment.paypal=PayPal
checkout.payment.vietqr=Chuyển Khoản (VietQR)
checkout.button=Thanh Toán
checkout.total=Tổng Cộng

# ============================================
# PAYMENT PAGE
# ============================================
payment.title=Đang Chờ Thanh Toán
payment.order=Đơn Hàng
payment.amount=Số Tiền
payment.status=Trạng Thái
payment.status.pending=Đang Chờ
payment.status.paid=Đã Thanh Toán
payment.status.failed=Thất Bại
payment.status.expired=Hết Hạn

payment.qr.title=Quét Mã QR Để Thanh Toán
payment.qr.bank=Ngân Hàng
payment.qr.account=Số Tài Khoản
payment.qr.content=Nội Dung Chuyển Khoản
payment.qr.helper=Vui lòng chuyển đúng số tiền và nội dung

payment.paypal.title=Thanh Toán Qua PayPal
payment.paypal.button=Tiếp Tục Với PayPal

payment.timeout=Đơn hàng sẽ hết hạn sau:
payment.checking=Đang kiểm tra thanh toán...
payment.minutes=phút
payment.seconds=giây

# ============================================
# SUCCESS PAGE
# ============================================
success.title=Thanh Toán Thành Công!
success.message=Cảm ơn bạn đã mua hàng
success.email.sent=Chúng tôi đã gửi link tải sách về email:
success.email.check=Vui lòng kiểm tra hộp thư (hoặc spam)
success.email.resend=Không nhận được email?
success.email.resend.button=Gửi Lại
success.download.title=Hoặc tải ngay tại đây:
success.download.button=Tải Ebook
success.order.id=Mã Đơn Hàng
success.order.date=Ngày Mua
success.back=Quay Về Trang Chủ

# ============================================
# ERROR PAGE
# ============================================
error.title=Có Lỗi Xảy Ra
error.payment.failed=Thanh toán thất bại
error.payment.expired=Đơn hàng đã hết hạn
error.order.notfound=Không tìm thấy đơn hàng
error.download.invalid=Link tải không hợp lệ
error.download.expired=Link tải đã hết hạn
error.try_again=Thử Lại
error.contact=Liên hệ hỗ trợ

# ============================================
# EMAIL TEMPLATES
# ============================================
email.subject.order=Đơn hàng #{0} - Cảm ơn bạn đã mua ebook
email.greeting=Xin chào {0},
email.thanks=Cảm ơn bạn đã mua ebook tại {1}
email.order.details=Chi tiết đơn hàng:
email.order.id=Mã đơn:
email.order.ebook=Ebook:
email.order.amount=Số tiền:
email.download.title=Tải ebook của bạn:
email.download.button=Tải Ngay
email.download.link=Hoặc copy link sau:
email.download.expire=Link tải có hiệu lực trong 24 giờ
email.support=Nếu có thắc mắc, liên hệ:
email.footer=© 2026 Ebook Store. All rights reserved.

# ============================================
# FORM VALIDATION
# ============================================
validation.email.required=Vui lòng nhập email
validation.email.invalid=Email không hợp lệ
validation.payment.required=Vui lòng chọn phương thức thanh toán

# ============================================
# BUTTONS & ACTIONS
# ============================================
button.submit=Gửi
button.cancel=Hủy
button.close=Đóng
button.back=Quay Lại
button.next=Tiếp Theo
button.confirm=Xác Nhận
button.loading=Đang xử lý...

# ============================================
# MESSAGES
# ============================================
message.loading=Đang tải...
message.no_results=Không tìm thấy kết quả
message.error=Đã xảy ra lỗi
message.success=Thành công
```

### 3.3 messages_en.properties (English)

**Tạo file:** `src/main/resources/i18n/messages_en.properties`

```properties
# ============================================
# COMMON
# ============================================
app.name=Ebook Store
app.tagline=Buy high-quality ebooks at the best prices

# Navigation
nav.home=Home
nav.ebooks=Ebooks
nav.about=About
nav.contact=Contact
nav.cart=Cart

# Language Switcher
lang.vietnamese=Tiếng Việt
lang.english=English
lang.switch=Change Language

# ============================================
# HOME PAGE
# ============================================
home.title=Discover Our Ebook Collection
home.subtitle=Over 1000+ high-quality ebooks
home.search.placeholder=Search books...
home.search.button=Search
home.filter.category=Category
home.filter.price=Price Range
home.filter.all=All

# Ebook Card
ebook.by=By:
ebook.price=Price:
ebook.currency=$
ebook.buy=Buy Now
ebook.details=Details
ebook.add_cart=Add to Cart

# Categories
category.fiction=Fiction
category.romance=Romance
category.adventure=Adventure
category.technology=Technology
category.business=Business

# ============================================
# CHECKOUT PAGE
# ============================================
checkout.title=Checkout
checkout.ebook=Ebook
checkout.price=Price
checkout.email=Your Email
checkout.email.placeholder=john@example.com
checkout.email.helper=Download link will be sent to this email
checkout.payment.method=Payment Method
checkout.payment.paypal=PayPal
checkout.payment.vietqr=Bank Transfer (VietQR)
checkout.button=Proceed to Payment
checkout.total=Total

# ============================================
# PAYMENT PAGE
# ============================================
payment.title=Waiting for Payment
payment.order=Order
payment.amount=Amount
payment.status=Status
payment.status.pending=Pending
payment.status.paid=Paid
payment.status.failed=Failed
payment.status.expired=Expired

payment.qr.title=Scan QR Code to Pay
payment.qr.bank=Bank
payment.qr.account=Account Number
payment.qr.content=Transfer Content
payment.qr.helper=Please transfer exact amount with correct content

payment.paypal.title=Pay with PayPal
payment.paypal.button=Continue with PayPal

payment.timeout=Order expires in:
payment.checking=Checking payment status...
payment.minutes=minutes
payment.seconds=seconds

# ============================================
# SUCCESS PAGE
# ============================================
success.title=Payment Successful!
success.message=Thank you for your purchase
success.email.sent=We've sent the download link to:
success.email.check=Please check your inbox (or spam folder)
success.email.resend=Didn't receive email?
success.email.resend.button=Resend
success.download.title=Or download now:
success.download.button=Download Ebook
success.order.id=Order ID
success.order.date=Purchase Date
success.back=Back to Home

# ============================================
# ERROR PAGE
# ============================================
error.title=Something Went Wrong
error.payment.failed=Payment failed
error.payment.expired=Order expired
error.order.notfound=Order not found
error.download.invalid=Invalid download link
error.download.expired=Download link expired
error.try_again=Try Again
error.contact=Contact support

# ============================================
# EMAIL TEMPLATES
# ============================================
email.subject.order=Order #{0} - Thank you for your purchase
email.greeting=Hi {0},
email.thanks=Thank you for purchasing ebook at {1}
email.order.details=Order details:
email.order.id=Order ID:
email.order.ebook=Ebook:
email.order.amount=Amount:
email.download.title=Download your ebook:
email.download.button=Download Now
email.download.link=Or copy this link:
email.download.expire=Download link is valid for 24 hours
email.support=For any questions, contact:
email.footer=© 2026 Ebook Store. All rights reserved.

# ============================================
# FORM VALIDATION
# ============================================
validation.email.required=Email is required
validation.email.invalid=Invalid email address
validation.payment.required=Please select a payment method

# ============================================
# BUTTONS & ACTIONS
# ============================================
button.submit=Submit
button.cancel=Cancel
button.close=Close
button.back=Back
button.next=Next
button.confirm=Confirm
button.loading=Processing...

# ============================================
# MESSAGES
# ============================================
message.loading=Loading...
message.no_results=No results found
message.error=An error occurred
message.success=Success
```

---

## 4. BACKEND IMPLEMENTATION

### 4.1 Ebook Entity với Multi-language

```java
package com.ebook.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ebooks")
@Data
public class Ebook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Vietnamese content
    @Column(name = "title_vi", nullable = false)
    private String titleVi;
    
    @Column(name = "author_vi")
    private String authorVi;
    
    @Column(name = "description_vi", columnDefinition = "TEXT")
    private String descriptionVi;
    
    // English content
    @Column(name = "title_en", nullable = false)
    private String titleEn;
    
    @Column(name = "author_en")
    private String authorEn;
    
    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;
    
    // Common fields
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "cover_url")
    private String coverUrl;
    
    @Column(name = "s3_key", nullable = false)
    private String s3Key;
    
    private String category;
    private String status = "ACTIVE";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Helper methods
    public String getTitle(String locale) {
        return "en".equals(locale) ? titleEn : titleVi;
    }
    
    public String getAuthor(String locale) {
        return "en".equals(locale) ? authorEn : authorVi;
    }
    
    public String getDescription(String locale) {
        return "en".equals(locale) ? descriptionEn : descriptionVi;
    }
}
```

### 4.2 EbookDTO với Auto Locale Detection

```java
package com.ebook.dto;

import com.ebook.entity.Ebook;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EbookDTO {
    private Long id;
    private String title;
    private String author;
    private String description;
    private BigDecimal price;
    private String coverUrl;
    private String category;
    
    public static EbookDTO fromEntity(Ebook ebook, String locale) {
        EbookDTO dto = new EbookDTO();
        dto.setId(ebook.getId());
        dto.setTitle(ebook.getTitle(locale));
        dto.setAuthor(ebook.getAuthor(locale));
        dto.setDescription(ebook.getDescription(locale));
        dto.setPrice(ebook.getPrice());
        dto.setCoverUrl(ebook.getCoverUrl());
        dto.setCategory(ebook.getCategory());
        return dto;
    }
}
```

### 4.3 EbookService với Locale Support

```java
package com.ebook.service;

import com.ebook.dto.EbookDTO;
import com.ebook.entity.Ebook;
import com.ebook.repository.EbookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EbookService {
    
    private final EbookRepository ebookRepository;
    private final LocaleResolver localeResolver;
    
    @Cacheable(value = "ebooks", key = "#locale + '_' + #pageable.pageNumber")
    public Page<EbookDTO> getAllEbooks(Pageable pageable, String locale) {
        Page<Ebook> ebooks = ebookRepository.findByStatus("ACTIVE", pageable);
        return ebooks.map(ebook -> EbookDTO.fromEntity(ebook, locale));
    }
    
    public EbookDTO getEbookById(Long id, String locale) {
        Ebook ebook = ebookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ebook not found"));
        return EbookDTO.fromEntity(ebook, locale);
    }
    
    public String getCurrentLocale(HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        return locale.getLanguage(); // "vi" or "en"
    }
}
```

### 4.4 LanguageController (Language Switcher)

```java
package com.ebook.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
public class LanguageController {
    
    private final LocaleResolver localeResolver;
    
    public LanguageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }
    
    @GetMapping("/change-language")
    public String changeLanguage(
            @RequestParam String lang,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // Set new locale
        Locale locale = new Locale(lang);
        localeResolver.setLocale(request, response, locale);
        
        // Redirect back to previous page or home
        String referer = redirect != null ? redirect : request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
```

---

## 5. FRONTEND IMPLEMENTATION

### 5.1 Language Switcher Component

**Thêm vào header của tất cả pages:**

```html
<!-- Language Switcher -->
<div class="language-switcher">
    <a th:href="@{/change-language(lang='vi', redirect=${#httpServletRequest.requestURI})}" 
       th:class="${#locale.language == 'vi'} ? 'active' : ''">
        <img src="/flags/vn.png" alt="Vietnamese" width="20"> 
        <span th:text="#{lang.vietnamese}">Tiếng Việt</span>
    </a>
    <span>|</span>
    <a th:href="@{/change-language(lang='en', redirect=${#httpServletRequest.requestURI})}" 
       th:class="${#locale.language == 'en'} ? 'active' : ''">
        <img src="/flags/gb.png" alt="English" width="20"> 
        <span th:text="#{lang.english}">English</span>
    </a>
</div>

<style>
.language-switcher {
    display: flex;
    align-items: center;
    gap: 10px;
}

.language-switcher a {
    text-decoration: none;
    color: #666;
    display: flex;
    align-items: center;
    gap: 5px;
}

.language-switcher a.active {
    color: #007bff;
    font-weight: bold;
}

.language-switcher img {
    vertical-align: middle;
}
</style>
```

### 5.2 index.html với i18n

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:lang="${#locale.language}">
<head>
    <meta charset="UTF-8">
    <title th:text="#{app.name}">Ebook Store</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <!-- Header -->
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <div class="container">
            <a class="navbar-brand" href="/" th:text="#{app.name}">Ebook Store</a>
            <p class="text-muted mb-0" th:text="#{app.tagline}"></p>
            
            <!-- Language Switcher -->
            <div class="language-switcher">
                <a th:href="@{/change-language(lang='vi')}" 
                   th:class="${#locale.language == 'vi'} ? 'active' : ''">
                    🇻🇳 <span th:text="#{lang.vietnamese}"></span>
                </a>
                |
                <a th:href="@{/change-language(lang='en')}" 
                   th:class="${#locale.language == 'en'} ? 'active' : ''">
                    🇬🇧 <span th:text="#{lang.english}"></span>
                </a>
            </div>
        </div>
    </nav>
    
    <!-- Search -->
    <div class="container my-5">
        <div class="input-group">
            <input type="text" 
                   class="form-control" 
                   th:placeholder="#{home.search.placeholder}">
            <button class="btn btn-primary" th:text="#{home.search.button}">Search</button>
        </div>
    </div>
    
    <!-- Ebook Grid -->
    <div class="container">
        <h2 th:text="#{home.title}">Discover Ebooks</h2>
        <div class="row">
            <div class="col-md-4" th:each="ebook : ${ebooks}">
                <div class="card mb-4">
                    <img th:src="${ebook.coverUrl}" class="card-img-top" th:alt="${ebook.title}">
                    <div class="card-body">
                        <h5 class="card-title" th:text="${ebook.title}"></h5>
                        <p class="card-text">
                            <span th:text="#{ebook.by}">By:</span> 
                            <span th:text="${ebook.author}"></span>
                        </p>
                        <p class="text-primary fw-bold">
                            <span th:text="#{ebook.price}">Price:</span> 
                            <span th:text="${#numbers.formatDecimal(ebook.price, 0, 'COMMA', 0, 'POINT')}"></span>
                            <span th:text="#{ebook.currency}">₫</span>
                        </p>
                        <a th:href="@{/checkout/{id}(id=${ebook.id})}" 
                           class="btn btn-success w-100" 
                           th:text="#{ebook.buy}">Buy Now</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

---

## 6. EMAIL TEMPLATES

### 6.1 EmailService với i18n

```java
package com.ebook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    
    public void sendOrderConfirmationEmail(String to, String orderCode, 
                                          String ebookTitle, String downloadToken,
                                          String locale) throws MessagingException {
        Locale loc = new Locale(locale);
        
        // Build email context
        Context context = new Context(loc);
        context.setVariable("orderCode", orderCode);
        context.setVariable("ebookTitle", ebookTitle);
        context.setVariable("downloadUrl", "https://yourdomain.com/download/" + downloadToken);
        
        // Render email template
        String htmlContent = templateEngine.process("email/order-confirmation", context);
        
        // Send email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(messageSource.getMessage("email.subject.order", 
                         new Object[]{orderCode}, loc));
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}
```

---

## 7. TESTING

### 7.1 Test Language Switching

```bash
# Default (Vietnamese)
curl http://localhost:8080/

# Switch to English
curl http://localhost:8080/change-language?lang=en

# Check cookie
curl -c cookies.txt http://localhost:8080/change-language?lang=en
curl -b cookies.txt http://localhost:8080/
```

### 7.2 Test API với Locale

```bash
# Vietnamese
curl http://localhost:8080/api/ebooks?lang=vi

# English
curl http://localhost:8080/api/ebooks?lang=en
```

---

## ✅ CHECKLIST

- [ ] Database có columns `title_vi`, `title_en`, `description_vi`, `description_en`
- [ ] `I18nConfig.java` đã tạo với `CookieLocaleResolver`
- [ ] Files `messages_vi.properties` và `messages_en.properties` đầy đủ
- [ ] `EbookEntity` có methods `getTitle(locale)`, `getDescription(locale)`
- [ ] `EbookService` return DTO theo locale
- [ ] `LanguageController` xử lý `/change-language?lang=vi|en`
- [ ] Frontend có language switcher trên header
- [ ] Tất cả text trong HTML dùng `th:text="#{key}"`
- [ ] Email templates support multi-language
- [ ] Test chuyển đổi ngôn ngữ OK

---

**🎉 HOÀN THÀNH! Website giờ đã hỗ trợ Tiếng Việt và Tiếng Anh!**
