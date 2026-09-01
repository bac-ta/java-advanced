package com.example.javaadvanced.reactorcontext.kafka;


import com.example.javaadvanced.reactorcontext.context.RequestContextKeys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private static final String TOPIC = "order-events";
    private static final String GROUP_ID = "order-events-processor";

    private final String bootstrapServers;
    private Disposable subscription;

    public OrderEventConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @PostConstruct
    public void start() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderEventJsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ReceiverOptions<String, OrderEvent> receiverOptions = ReceiverOptions
                .<String, OrderEvent>create(props)
                .subscription(java.util.List.of(TOPIC));

        this.subscription = KafkaReceiver.create(receiverOptions)
                .receive()
                .flatMap(record -> {
                    String traceId = header(record.headers(), "traceId").orElse("N/A");
                    String tenantId = header(record.headers(), "tenantId").orElse(null);

                    return process(record.value())
                            .contextWrite(ctx -> ctx
                                    .put(RequestContextKeys.TRACE_ID, traceId)
                                    .putNonNull(RequestContextKeys.TENANT_ID, tenantId))
                            .doOnSuccess(v -> record.receiverOffset().acknowledge());
                })
                .doOnError(e -> log.error("Kafka consumer error", e))
                .retry() // tránh chết hẳn subscription khi lỗi tạm thời
                .subscribe();
    }

    private Mono<Void> process(OrderEvent event) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(RequestContextKeys.TRACE_ID, "N/A");
            log.info("[{}] Consumed order-events orderId={} tenantId={}",
                    traceId, event.orderId(), event.tenantId());
            // TODO: xử lý nghiệp vụ thực tế (gửi email, cập nhật thống kê...)
            return Mono.empty();
        });
    }

    private Optional<String> header(Iterable<Header> headers, String key) {
        for (Header h : headers) {
            if (h.key().equals(key)) {
                return Optional.of(new String(h.value(), StandardCharsets.UTF_8));
            }
        }
        return Optional.empty();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
