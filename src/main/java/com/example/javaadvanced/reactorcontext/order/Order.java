package com.example.javaadvanced.reactorcontext.order;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("orders")
public record Order(
        @Id
        Long id,

        @Column("tenant_id")
        String tenantId,

        @Column("user_id")
        String userId,

        @Column("product_name")
        String productName,

        BigDecimal amount,

        @Column("created_at")
        Instant createdAt
) {

    public static Order newOrder(String tenantId, String userId, OrderRequest req) {
        return new Order(null, tenantId, userId, req.productName(), req.amount(), Instant.now());
    }
}
