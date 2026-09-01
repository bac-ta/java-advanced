package com.example.javaadvanced.reactorcontext.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;


public class OrderEventJsonSerializer implements Serializer<OrderEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    public byte[] serialize(String topic, OrderEvent data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize serialize OrderEvent to JSON", e);
        }
    }
}
