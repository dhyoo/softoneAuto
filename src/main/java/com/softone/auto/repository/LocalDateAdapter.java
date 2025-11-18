package com.softone.auto.repository;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDate를 JSON으로 직렬화/역직렬화하기 위한 Gson 어댑터
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        if (date == null) {
            return new JsonPrimitive("");  // null 대신 빈 문자열 반환
        }
        return new JsonPrimitive(date.format(formatter));
    }
    
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String dateStr = json.getAsString();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (Exception e) {
            throw new JsonParseException("날짜 형식 오류: " + dateStr + " (예상 형식: yyyy-MM-dd)", e);
        }
    }
}

