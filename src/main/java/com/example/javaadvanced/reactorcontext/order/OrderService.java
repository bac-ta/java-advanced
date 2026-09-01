package com.example.javaadvanced.reactorcontext.order;

import com.example.javaadvanced.reactorcontext.context.RequestContextKeys;
import com.example.javaadvanced.reactorcontext.context.RequestContextView;
import com.example.javaadvanced.reactorcontext.kafka.OrderEvent;
import com.example.javaadvanced.reactorcontext.kafka.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    public Mono<Order> createOrder(OrderRequest req) {
        return Mono.deferContextual(ctxView -> {
            var ctx = RequestContextView.from(ctxView);
            log.info("[{}] User {} creating order for tenant {}",
                    ctx.traceId(), ctx.userId(), ctx.tenantId());

            Order order = Order.newOrder(ctx.tenantId(), ctx.userId(), req);

            return orderRepository.save(order)
                    .flatMap(saved -> orderEventPublisher.publish(toEvent(saved))
                            .thenReturn(saved));
        });
    }

    public Flux<Order> listOrders() {
        return Flux.deferContextual(ctxView -> {
            String tenantId = ctxView.get(RequestContextKeys.TENANT_ID);
            return orderRepository.findByTenantId(tenantId);
        });
    }

    public Mono<Order> getOrder(Long id) {
        return Mono.deferContextual(ctxView -> {
            String tenantId = ctxView.get(RequestContextKeys.TENANT_ID);
            return orderRepository.findByIdAndTenantId(id, tenantId)
                    .switchIfEmpty(Mono.error(new OrderNotFoundException(id)));
        });
    }

    private OrderEvent toEvent(Order order) {
        return new OrderEvent(order.id(), order.tenantId(), order.userId(),
                order.productName(), order.amount(), order.createdAt());
    }
}
