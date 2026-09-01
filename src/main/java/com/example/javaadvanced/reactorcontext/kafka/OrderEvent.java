package com.example.javaadvanced.reactorcontext.kafka;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent(
        Long orderId,
        String tenantId,
        String userId,
        String productName,
        BigDecimal amount,
        Instant createdAt
) {
}
