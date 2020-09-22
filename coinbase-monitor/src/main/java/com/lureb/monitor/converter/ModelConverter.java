package com.lureb.monitor.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ModelConverter {
    private final ObjectMapper objectMapper;

    public ModelConverter() {
        this.objectMapper = new ObjectMapper();
    }

    public <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
        return objectMapper.convertValue(fromValue, toValueType);
    }
}
