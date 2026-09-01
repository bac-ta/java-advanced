package com.example.javaadvanced.reactorcontext.kafka;

import com.example.javaadvanced.reactorcontext.context.RequestContextKeys;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private static final String TOPIC = "order-events";

    private final KafkaSender<String, OrderEvent> kafkaSender;

    public OrderEventPublisher(KafkaSender<String, OrderEvent> kafkaSender) {
        this.kafkaSender = kafkaSender;
    }

    public Mono<Void> publish(OrderEvent event) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(RequestContextKeys.TRACE_ID, "N/A");
            String tenantId = ctx.getOrDefault(RequestContextKeys.TENANT_ID, "");

            ProducerRecord<String, OrderEvent> record =
                    new ProducerRecord<>(TOPIC, String.valueOf(event.orderId()), event);
            record.headers()
                    .add(new RecordHeader("traceId", traceId.getBytes(StandardCharsets.UTF_8)))
                    .add(new RecordHeader("tenantId", tenantId.getBytes(StandardCharsets.UTF_8)));

            SenderRecord<String, OrderEvent, String> senderRecord =
                    SenderRecord.create(record, String.valueOf(event.orderId()));

            return kafkaSender.send(Mono.just(senderRecord))
                    .doOnNext(result -> log.info("[{}] Published order-events orderId={} partition={} offset={}",
                            traceId, event.orderId(),
                            result.recordMetadata().partition(), result.recordMetadata().offset()))
                    .doOnError(e -> log.error("[{}] Failed to publish orderId={}", traceId, event.orderId(), e))
                    .then();
        });
    }
}
