package com.softone.auto.repository.legacy;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalTime JSON 직렬화/역직렬화 어댑터 (레거시)
 * 
 * @deprecated JSON 기반 저장소가 SQLite로 전환되어 더 이상 사용되지 않음.
 *             마이그레이션 유틸리티(JsonToSqliteMigrator)에서만 사용됨.
 */
@Deprecated
public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    
    @Override
    public JsonElement serialize(LocalTime src, java.lang.reflect.Type typeOfSrc, 
                                com.google.gson.JsonSerializationContext context) {
        return new JsonPrimitive(src.format(FORMATTER));
    }
    
    @Override
    public LocalTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, 
                                com.google.gson.JsonDeserializationContext context) {
        return LocalTime.parse(json.getAsString(), FORMATTER);
    }
}

