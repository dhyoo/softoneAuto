package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 예외를 사용자 친화적 메시지로 변환하는 유틸리티
 */
@Slf4j
public class ErrorMessageMapper {
    
    /**
     * 예외를 사용자 친화적 메시지로 변환
     * 기술적 세부사항은 숨기고 일반 사용자가 이해할 수 있는 메시지 제공
     */
    public static String getUserFriendlyMessage(Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        
        // FileNotFoundException
        if (cause instanceof java.io.FileNotFoundException) {
            return "데이터 파일을 찾을 수 없습니다.\n" +
                   "데이터가 손상되었거나 삭제되었을 수 있습니다.\n\n" +
                   "백업 파일에서 복원을 시도하거나 관리자에게 문의해주세요.";
        }
        
        // IOException (파일 I/O 관련)
        if (cause instanceof java.io.IOException) {
            String message = cause.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                
                // 파일 사용 중
                if (lowerMessage.contains("사용 중") || 
                    lowerMessage.contains("being used") ||
                    lowerMessage.contains("cannot access") ||
                    lowerMessage.contains("locked")) {
                    return "파일이 다른 프로그램에서 사용 중입니다.\n\n" +
                           "다음 사항을 확인해주세요:\n" +
                           "1. 다른 프로그램에서 같은 파일을 열고 있는지 확인\n" +
                           "2. 프로그램을 닫고 다시 시도\n" +
                           "3. 파일 탐색기에서 파일이 열려있는지 확인";
                }
                
                // 권한 문제
                if (lowerMessage.contains("권한") || 
                    lowerMessage.contains("permission") ||
                    lowerMessage.contains("denied") ||
                    lowerMessage.contains("access denied")) {
                    return "파일에 접근할 권한이 없습니다.\n\n" +
                           "다음 사항을 확인해주세요:\n" +
                           "1. 관리자 권한으로 프로그램 실행\n" +
                           "2. 데이터 폴더의 읽기/쓰기 권한 확인\n" +
                           "3. 파일이 읽기 전용으로 설정되어 있는지 확인";
                }
                
                // 디스크 공간 부족
                if (lowerMessage.contains("공간") || 
                    lowerMessage.contains("space") ||
                    lowerMessage.contains("no space")) {
                    return "디스크 공간이 부족합니다.\n\n" +
                           "디스크 공간을 확보한 후 다시 시도해주세요.";
                }
            }
            return "파일 읽기/쓰기 중 오류가 발생했습니다.\n\n" +
                   "데이터 폴더의 접근 권한과 디스크 공간을 확인해주세요.";
        }
        
        // JSON 파싱 오류
        if (cause instanceof com.google.gson.JsonSyntaxException) {
            return "데이터 파일이 손상되었습니다.\n\n" +
                   "백업 파일에서 복원을 시도합니다.\n" +
                   "복원이 실패하면 관리자에게 문의해주세요.";
        }
        
        // 날짜 형식 오류
        if (cause instanceof java.time.format.DateTimeParseException) {
            return "날짜 형식이 올바르지 않습니다.\n\n" +
                   "올바른 형식: yyyy-MM-dd\n" +
                   "예시: 2025-01-15";
        }
        
        // 숫자 형식 오류
        if (cause instanceof NumberFormatException) {
            return "숫자 형식이 올바르지 않습니다.\n\n" +
                   "올바른 숫자를 입력해주세요.";
        }
        
        // IllegalArgumentException
        if (cause instanceof IllegalArgumentException) {
            String message = cause.getMessage();
            if (message != null && message.contains("companyId")) {
                return "회사 정보가 없습니다.\n\n" +
                       "회사를 먼저 선택한 후 다시 시도해주세요.";
            }
            return "입력값이 올바르지 않습니다.\n\n" +
                   "모든 필수 항목을 올바르게 입력했는지 확인해주세요.";
        }
        
        // 알 수 없는 오류
        return "예기치 않은 오류가 발생했습니다.\n\n" +
               "문제가 지속되면 관리자에게 문의해주세요.\n" +
               "오류 코드: " + System.currentTimeMillis();
    }
    
    /**
     * 로그에만 상세 정보 기록
     * 사용자에게는 기술적 정보를 노출하지 않음
     */
    public static void logError(String context, Exception e) {
        log.error("{} 오류 발생", context, e);
        
        // 디버그 모드에서만 상세 스택 트레이스 기록
        if (log.isDebugEnabled()) {
            log.debug("상세 스택 트레이스:", e);
        }
    }
    
    /**
     * 오류 메시지와 함께 로그 기록
     */
    public static void logErrorWithMessage(String context, String userMessage, Exception e) {
        log.error("{} 오류 발생 - 사용자 메시지: {}", context, userMessage, e);
    }
}

