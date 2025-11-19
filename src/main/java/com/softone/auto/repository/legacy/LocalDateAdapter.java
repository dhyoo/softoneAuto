package com.softone.auto.repository.legacy;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDate JSON 직렬화/역직렬화 어댑터 (레거시)
 * 
 * @deprecated JSON 기반 저장소가 SQLite로 전환되어 더 이상 사용되지 않음.
 *             마이그레이션 유틸리티(JsonToSqliteMigrator)에서만 사용됨.
 */
@Deprecated
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public JsonElement serialize(LocalDate src, java.lang.reflect.Type typeOfSrc, 
                                com.google.gson.JsonSerializationContext context) {
        return new JsonPrimitive(src.format(FORMATTER));
    }
    
    @Override
    public LocalDate deserialize(JsonElement json, java.lang.reflect.Type typeOfT, 
                                com.google.gson.JsonDeserializationContext context) {
        return LocalDate.parse(json.getAsString(), FORMATTER);
    }
}

