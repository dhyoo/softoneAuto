package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 감사 로그 유틸리티
 * 
 * 데이터 접근, 수정, 삭제 등의 중요한 작업을 추적하기 위한 감사 로그를 기록합니다.
 * 개인정보는 마스킹하여 기록합니다.
 * 
 * 사용 예:
 * <pre>
 * AuditLogger.logDataAccess("user123", "CREATE", "Developer", "홍길동");
 * AuditLogger.logDataModification("user123", "UPDATE", "WeeklyReport", "report-001");
 * </pre>
 */
@Slf4j
public class AuditLogger {
    
    private static final String AUDIT_LOG_PREFIX = "[AUDIT]";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 데이터 접근 로그 기록
     * 
     * @param userId 사용자 ID (마스킹됨)
     * @param action 수행한 작업 (CREATE, READ, UPDATE, DELETE 등)
     * @param resourceType 리소스 타입 (예: "Developer", "WeeklyReport", "Attendance")
     * @param resourceId 리소스 ID 또는 식별자 (마스킹 가능)
     */
    public static void logDataAccess(String userId, String action, String resourceType, String resourceId) {
        String maskedUserId = userId != null ? PrivacyMaskingUtil.maskName(userId) : "UNKNOWN";
        String maskedResourceId = resourceId != null ? PrivacyMaskingUtil.maskLogMessage(resourceId) : "N/A";
        
        String message = String.format("%s [%s] User: %s | Action: %s | Resource: %s | ID: %s",
            AUDIT_LOG_PREFIX,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            maskedUserId,
            action,
            resourceType,
            maskedResourceId
        );
        
        log.info(message);
    }
    
    /**
     * 데이터 수정 로그 기록
     * 
     * @param userId 사용자 ID
     * @param action 수행한 작업
     * @param resourceType 리소스 타입
     * @param resourceId 리소스 ID
     * @param details 추가 상세 정보 (마스킹됨)
     */
    public static void logDataModification(String userId, String action, String resourceType, 
                                          String resourceId, String details) {
        String maskedUserId = userId != null ? PrivacyMaskingUtil.maskName(userId) : "UNKNOWN";
        String maskedResourceId = resourceId != null ? PrivacyMaskingUtil.maskLogMessage(resourceId) : "N/A";
        String maskedDetails = details != null ? PrivacyMaskingUtil.maskLogMessage(details) : "";
        
        String message = String.format("%s [%s] User: %s | Action: %s | Resource: %s | ID: %s | Details: %s",
            AUDIT_LOG_PREFIX,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            maskedUserId,
            action,
            resourceType,
            maskedResourceId,
            maskedDetails
        );
        
        log.info(message);
    }
    
    /**
     * 간단한 데이터 접근 로그 (리소스 ID 없이)
     * 
     * @param userId 사용자 ID
     * @param action 수행한 작업
     * @param resourceType 리소스 타입
     */
    public static void logDataAccess(String userId, String action, String resourceType) {
        logDataAccess(userId, action, resourceType, null);
    }
    
    /**
     * 시스템 이벤트 로그 (로그인, 로그아웃 등)
     * 
     * @param userId 사용자 ID
     * @param event 이벤트 타입
     * @param details 상세 정보
     */
    public static void logSystemEvent(String userId, String event, String details) {
        String maskedUserId = userId != null ? PrivacyMaskingUtil.maskName(userId) : "SYSTEM";
        String maskedDetails = details != null ? PrivacyMaskingUtil.maskLogMessage(details) : "";
        
        String message = String.format("%s [%s] User: %s | Event: %s | Details: %s",
            AUDIT_LOG_PREFIX,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            maskedUserId,
            event,
            maskedDetails
        );
        
        log.info(message);
    }
}

