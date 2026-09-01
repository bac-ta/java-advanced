package com.example.javaadvanced.reactorcontext.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class OrderEventJsonDeserializer implements Deserializer<OrderEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    public OrderEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(new String(data, StandardCharsets.UTF_8), OrderEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize OrderEvent from JSON", e);
        }
    }
}
