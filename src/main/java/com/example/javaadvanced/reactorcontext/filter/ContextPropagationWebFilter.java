package com.example.javaadvanced.reactorcontext.filter;

import com.example.javaadvanced.reactorcontext.context.RequestContextKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContextPropagationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ContextPropagationWebFilter.class);

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID);
        String tenantId = exchange.getRequest().getHeaders().getFirst(HEADER_TENANT_ID);
        String traceId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER_TRACE_ID))
                .filter(s -> !s.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());

        // Trả traceId lại cho client để dễ tra cứu (support, FE log...)
        exchange.getResponse().getHeaders().add(HEADER_TRACE_ID, traceId);

        log.debug("Incoming request {} {} - traceId={} tenantId={} userId={}",
                exchange.getRequest().getMethod(), exchange.getRequest().getPath(),
                traceId, tenantId, userId);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx
                        .putNonNull(RequestContextKeys.USER_ID, userId)
                        .putNonNull(RequestContextKeys.TENANT_ID, tenantId)
                        .put(RequestContextKeys.TRACE_ID, traceId));
    }
}
