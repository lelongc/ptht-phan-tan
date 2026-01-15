package com.ebook.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebook.dto.CreateOrderRequest;
import com.ebook.dto.CreateOrderResponse;
import com.ebook.dto.OrderStatusResponse;
import com.ebook.entity.Ebook;
import com.ebook.entity.Order;
import com.ebook.entity.User;
import com.ebook.enums.OrderStatus;
import com.ebook.enums.PaymentMethod;
import com.ebook.enums.UserStatus;
import com.ebook.repository.EbookRepository;
import com.ebook.repository.OrderRepository;
import com.ebook.repository.UserRepository;
import com.ebook.util.RandomUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final EbookRepository ebookRepository;
    private final OrderRepository orderRepository;
    private final VietQRService vietQRService;
    private final PaypalService paypalService;

    @Value("${app.order.expire-minutes:15}")
    private int orderExpireMinutes;

    @Value("${app.order.download-expire-hours:24}")
    private int downloadExpireHours;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Ebook ebook = resolveEbook(request.ebookId());
        User user = findOrCreateUser(request.email());

        String secretCode = RandomUtil.randomCode(12);
        Instant now = Instant.now();

        Order order = Order.builder()
                .user(user)
                .ebook(ebook)
                .amount(ebook.getPrice())
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .secretCode(secretCode)
                .payerEmail(request.email())
                .expiresAt(now.plus(orderExpireMinutes, ChronoUnit.MINUTES))
                .downloadExpiresAt(now.plus(downloadExpireHours, ChronoUnit.HOURS))
                .build();
        orderRepository.save(order);

        String addInfo = "ORDER-" + secretCode;
        String paymentUrl;
        String qrUrl = null;

        if (request.paymentMethod() == PaymentMethod.VIETQR) {
            qrUrl = vietQRService.buildQrImageUrl(ebook.getPrice(), addInfo);
            paymentUrl = qrUrl;
        } else {
            paymentUrl = "paypal"; // PayPal will be handled in payment page
        }

        return new CreateOrderResponse(secretCode, paymentUrl, qrUrl);
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getStatus(String secretCode) {
        Order order = orderRepository.findBySecretCode(secretCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        String message = switch (order.getStatus()) {
            case PENDING -> "Waiting for payment";
            case PAID -> "Paid";
            case FAILED -> "Failed";
            case EXPIRED -> "Expired";
            case REFUNDED -> "Refunded";
        };
        return new OrderStatusResponse(order.getStatus(), message, order.getAmount().doubleValue());
    }

    @Transactional
    public void markPaid(String secretCode, String txnId) {
        orderRepository.findBySecretCode(secretCode).ifPresent(order -> {
            order.setStatus(OrderStatus.PAID);
            order.setTransactionId(txnId);
            order.setPaidAt(Instant.now());
        });
    }

    @Transactional
    public void markFailed(String secretCode) {
        orderRepository.findBySecretCode(secretCode).ifPresent(order -> order.setStatus(OrderStatus.FAILED));
    }

    @Transactional(readOnly = true)
    public Order getBySecretCode(String secretCode) {
        return orderRepository.findBySecretCode(secretCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Transactional(readOnly = true)
    public boolean hasEmailPaidOrder(String email) {
        return orderRepository.findByPayerEmailAndStatus(email, OrderStatus.PAID)
                .isPresent();
    }

    private User findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .status(UserStatus.ACTIVE)
                        .build()));
    }

    private Ebook resolveEbook(Long ebookId) {
        if (ebookId == null) {
            return ebookRepository.findFirstByStatus(com.ebook.enums.EbookStatus.ACTIVE)
                    .orElseThrow(() -> new IllegalStateException("No active ebook available"));
        }
        return ebookRepository.findById(ebookId)
                .orElseThrow(() -> new IllegalArgumentException("Ebook not found"));
    }
}
