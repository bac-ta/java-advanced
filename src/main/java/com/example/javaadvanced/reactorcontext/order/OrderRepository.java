package com.example.javaadvanced.reactorcontext.order;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Flux<Order> findByTenantId(String tenantId);

    @Query("SELECT * FROM orders WHERE id = :id AND tenant_id = :tenantId")
    Mono<Order> findByIdAndTenantId(Long id, String tenantId);
}
