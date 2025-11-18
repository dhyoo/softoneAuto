package com.softone.auto.repository;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime을 JSON으로 직렬화/역직렬화하기 위한 Gson 어댑터
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Override
    public JsonElement serialize(LocalDateTime dateTime, Type typeOfSrc, JsonSerializationContext context) {
        if (dateTime == null) {
            return new JsonPrimitive("");  // null 대신 빈 문자열 반환
        }
        return new JsonPrimitive(dateTime.format(formatter));
    }
    
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String dateTimeStr = json.getAsString();
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), formatter);
        } catch (Exception e) {
            throw new JsonParseException("날짜/시간 형식 오류: " + dateTimeStr + " (예상 형식: yyyy-MM-ddTHH:mm:ss)", e);
        }
    }
}

