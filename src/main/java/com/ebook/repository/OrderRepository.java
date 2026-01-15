package com.ebook.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebook.entity.Order;
import com.ebook.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findBySecretCode(String secretCode);
    
    Optional<Order> findByPayerEmailAndStatus(String payerEmail, OrderStatus status);

    @Query("select o from Order o where o.status = :status and o.expiresAt < :now")
    List<Order> findExpired(@Param("status") OrderStatus status, @Param("now") Instant now);
}
