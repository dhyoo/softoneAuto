package com.softone.auto.util;

import javax.swing.*;
import javax.swing.text.*;

/**
 * 입력 검증 및 필터링 유틸리티
 * JSON 데이터 안전성을 위한 특수문자 제한
 */
public class InputValidator {
    
    /**
     * 안전한 텍스트인지 검증 (제어문자, 따옴표 등 제외)
     */
    public static boolean isSafeText(String text) {
        if (text == null) return true;
        
        // 제어문자 체크 (줄바꿈, 탭 제외)
        for (char c : text.toCharArray()) {
            if (Character.isISOControl(c) && c != '\n' && c != '\t' && c != '\r') {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 텍스트 정제 (위험한 문자 제거)
     */
    public static String sanitize(String text) {
        if (text == null) return "";
        
        // 제어문자 제거 (줄바꿈, 탭은 유지)
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (!Character.isISOControl(c) || c == '\n' || c == '\t' || c == '\r') {
                sb.append(c);
            }
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 한 줄 입력용 텍스트 정제 (줄바꿈 제거)
     */
    public static String sanitizeSingleLine(String text) {
        if (text == null) return "";
        
        // 제어문자 모두 제거
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (!Character.isISOControl(c)) {
                sb.append(c);
            }
        }
        
        return sb.toString().trim();
    }
    
    /**
     * 텍스트 필드용 DocumentFilter (실시간 입력 제한)
     */
    public static class SafeTextDocumentFilter extends DocumentFilter {
        
        private final boolean allowMultiline;
        private final int maxLength;
        
        public SafeTextDocumentFilter() {
            this(true, -1);
        }
        
        public SafeTextDocumentFilter(boolean allowMultiline) {
            this(allowMultiline, -1);
        }
        
        public SafeTextDocumentFilter(boolean allowMultiline, int maxLength) {
            this.allowMultiline = allowMultiline;
            this.maxLength = maxLength;
        }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                throws BadLocationException {
            
            String newText = filterText(string);
            
            // 최대 길이 체크
            if (maxLength > 0) {
                int currentLength = fb.getDocument().getLength();
                if (currentLength + newText.length() > maxLength) {
                    int remaining = maxLength - currentLength;
                    if (remaining > 0) {
                        newText = newText.substring(0, remaining);
                    } else {
                        return; // 더 이상 입력 불가
                    }
                }
            }
            
            if (!newText.isEmpty()) {
                super.insertString(fb, offset, newText, attr);
            }
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
            
            String newText = filterText(text);
            
            // 최대 길이 체크
            if (maxLength > 0) {
                int currentLength = fb.getDocument().getLength();
                int newLength = currentLength - length + newText.length();
                if (newLength > maxLength) {
                    int remaining = maxLength - (currentLength - length);
                    if (remaining > 0) {
                        newText = newText.substring(0, remaining);
                    } else {
                        return;
                    }
                }
            }
            
            if (newText != null) {
                super.replace(fb, offset, length, newText, attrs);
            }
        }
        
        private String filterText(String text) {
            if (text == null) return "";
            
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) {
                // 줄바꿈 처리
                if (c == '\n' || c == '\r') {
                    if (allowMultiline) {
                        sb.append(c);
                    }
                    // 줄바꿈 불허 시 무시
                } 
                // 제어문자 차단 (탭은 공백으로 변환)
                else if (Character.isISOControl(c)) {
                    if (c == '\t') {
                        sb.append("    "); // 탭을 4칸 공백으로
                    }
                    // 다른 제어문자는 무시
                } 
                // 정상 문자
                else {
                    sb.append(c);
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * JTextField에 안전한 필터 적용
     */
    public static void applySafeFilter(JTextField textField) {
        applySafeFilter(textField, 255);
    }
    
    public static void applySafeFilter(JTextField textField, int maxLength) {
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new SafeTextDocumentFilter(false, maxLength));
    }
    
    /**
     * JTextArea에 안전한 필터 적용
     */
    public static void applySafeFilter(JTextArea textArea) {
        applySafeFilter(textArea, -1);
    }
    
    public static void applySafeFilter(JTextArea textArea, int maxLength) {
        AbstractDocument doc = (AbstractDocument) textArea.getDocument();
        doc.setDocumentFilter(new SafeTextDocumentFilter(true, maxLength));
    }
    
    /**
     * 비어있는지 체크 (null, 빈 문자열, 공백만 있는 경우)
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
    
    /**
     * 이메일 형식 검증
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * 전화번호 형식 검증 (010-1234-5678 형태)
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return phone.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$");
    }
    
    /**
     * 파일 경로 검증 (Path Traversal 방지)
     * PathSecurityValidator를 사용하여 검증
     */
    public static boolean isValidFilePath(String path) {
        return PathSecurityValidator.isValidPath(path);
    }
    
    /**
     * 파일명 검증
     */
    public static boolean isValidFileName(String fileName) {
        return PathSecurityValidator.isValidFileName(fileName);
    }
    
    /**
     * 파일 경로 정제
     */
    public static String sanitizeFilePath(String path) {
        return PathSecurityValidator.sanitizePath(path);
    }
    
    /**
     * 파일명 정제
     */
    public static String sanitizeFileName(String fileName) {
        return PathSecurityValidator.sanitizeFileName(fileName);
    }
    
    /**
     * OS 명령어 검증 (Command Injection 방지)
     * CommandSecurityValidator를 사용하여 검증
     */
    public static boolean isValidCommand(String command) {
        return CommandSecurityValidator.isValidCommand(command);
    }
    
    /**
     * 명령어 배열 검증
     */
    public static boolean isValidCommandArray(String[] commands) {
        return CommandSecurityValidator.isValidCommandArray(commands);
    }
    
    /**
     * 명령어 정제
     */
    public static String sanitizeCommand(String command) {
        return CommandSecurityValidator.sanitizeCommand(command);
    }
    
    /**
     * 숫자 검증 (정수)
     */
    public static boolean isValidInteger(String text) {
        if (isEmpty(text)) return false;
        try {
            Integer.parseInt(text.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 숫자 검증 (실수)
     */
    public static boolean isValidDouble(String text) {
        if (isEmpty(text)) return false;
        try {
            Double.parseDouble(text.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 숫자 범위 검증 (정수)
     */
    public static boolean isValidIntegerRange(String text, int min, int max) {
        if (!isValidInteger(text)) return false;
        try {
            int value = Integer.parseInt(text.trim());
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 날짜 형식 검증 (yyyy-MM-dd)
     */
    public static boolean isValidDateFormat(String date) {
        if (isEmpty(date)) return false;
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
    
    /**
     * 시간 형식 검증 (HH:mm)
     */
    public static boolean isValidTimeFormat(String time) {
        if (isEmpty(time)) return false;
        return time.matches("^\\d{2}:\\d{2}$");
    }
    
    /**
     * SQL Injection 방지 (기본적인 패턴 검사)
     * 주의: 완전한 방어를 위해서는 PreparedStatement 사용 필수
     */
    public static boolean containsSqlInjection(String text) {
        if (text == null || text.isEmpty()) return false;
        
        String upperText = text.toUpperCase();
        
        // 위험한 SQL 키워드 패턴
        String[] dangerousPatterns = {
            "';", "--", "/*", "*/", "xp_", "sp_", 
            "UNION", "SELECT", "INSERT", "UPDATE", "DELETE", 
            "DROP", "CREATE", "ALTER", "EXEC", "EXECUTE"
        };
        
        for (String pattern : dangerousPatterns) {
            if (upperText.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * SQL Injection 패턴 제거
     */
    public static String sanitizeSql(String text) {
        if (text == null || text.isEmpty()) return "";
        
        // 위험한 문자 이스케이프
        return text.replace("'", "''")
                   .replace(";", "")
                   .replace("--", "")
                   .replace("/*", "")
                   .replace("*/", "");
    }
    
    /**
     * 최대 길이 검증
     */
    public static boolean isValidLength(String text, int maxLength) {
        if (text == null) return true;
        return text.length() <= maxLength;
    }
    
    /**
     * 최소/최대 길이 검증
     */
    public static boolean isValidLengthRange(String text, int minLength, int maxLength) {
        if (text == null) return minLength == 0;
        int length = text.length();
        return length >= minLength && length <= maxLength;
    }
}

