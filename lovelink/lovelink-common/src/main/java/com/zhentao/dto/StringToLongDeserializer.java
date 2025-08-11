package com.zhentao.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 字符串转Long的反序列化器
 * 用于处理前端传递的字符串类型ID
 * 
 * @author zhentao
 */
public class StringToLongDeserializer extends JsonDeserializer<Long> {
    
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid Long value: " + value, e);
        }
    }
}
