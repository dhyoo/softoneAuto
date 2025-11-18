package com.softone.auto.repository;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalTime을 JSON으로 직렬화/역직렬화하기 위한 Gson 어댑터
 */
public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
    
    @Override
    public JsonElement serialize(LocalTime time, Type typeOfSrc, JsonSerializationContext context) {
        if (time == null) {
            return new JsonPrimitive("");  // null 대신 빈 문자열 반환
        }
        return new JsonPrimitive(time.format(formatter));
    }
    
    @Override
    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String timeStr = json.getAsString();
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr.trim(), formatter);
        } catch (Exception e) {
            throw new JsonParseException("시간 형식 오류: " + timeStr + " (예상 형식: HH:mm:ss)", e);
        }
    }
}

