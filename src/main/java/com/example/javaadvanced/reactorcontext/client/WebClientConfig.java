package com.example.javaadvanced.reactorcontext.client;

import com.example.javaadvanced.reactorcontext.context.RequestContextKeys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {


    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter(propagateContextHeaders())
                .build();
    }

    private ExchangeFilterFunction propagateContextHeaders() {
        return (request, next) -> Mono.deferContextual(ctx -> {
            ClientRequest.Builder builder = ClientRequest.from(request);

            String traceId = ctx.getOrDefault(RequestContextKeys.TRACE_ID, null);
            String tenantId = ctx.getOrDefault(RequestContextKeys.TENANT_ID, null);

            if (traceId != null) {
                builder.header("X-Trace-Id", traceId);
            }
            if (tenantId != null) {
                builder.header("X-Tenant-Id", tenantId);
            }

            return next.exchange(builder.build());
        });
    }
}
