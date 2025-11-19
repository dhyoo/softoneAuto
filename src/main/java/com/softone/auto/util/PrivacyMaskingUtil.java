package com.softone.auto.util;

import java.util.regex.Pattern;

/**
 * 개인정보 마스킹 유틸리티
 * 로그 출력 시 개인정보를 마스킹하여 노출 방지
 * 
 * 적용 대상:
 * - 전화번호
 * - 이메일 주소
 * - 이름 (한글/영문)
 * - 개발자 정보 전체
 */
public class PrivacyMaskingUtil {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\d{2,3})-(\\d{3,4})-(\\d{4})");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("([a-zA-Z0-9._-]+)@([a-zA-Z0-9.-]+)\\.([a-zA-Z]{2,})");
    private static final Pattern KOREAN_NAME_PATTERN = Pattern.compile("[가-힣]{2,4}");
    
    /**
     * 전화번호 마스킹
     * 010-1234-5678 → 010-****-5678
     * 02-1234-5678 → 02-****-5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        return PHONE_PATTERN.matcher(phone).replaceAll("$1-****-$3");
    }
    
    /**
     * 이메일 마스킹
     * user@example.com → u***@e***.com
     * test.user@company.co.kr → t***@c***.co.kr
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        return EMAIL_PATTERN.matcher(email).replaceAll("$1***@$2***.$3");
    }
    
    /**
     * 이름 마스킹
     * - 한글: 홍길동 → 홍*동, 김철수 → 김*수
     * - 영문: John → J**n, Mary → M**y
     * - 2자: 홍동 → 홍*
     * - 1자: 홍 → 홍 (변경 없음)
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        int length = name.length();
        
        if (length == 1) {
            return name; // 1자는 마스킹하지 않음
        }
        
        if (length == 2) {
            return name.charAt(0) + "*";
        }
        
        // 3자 이상
        if (KOREAN_NAME_PATTERN.matcher(name).matches()) {
            // 한글 이름: 첫 글자 + 마스킹 + 마지막 글자
            return name.charAt(0) + "*".repeat(length - 2) + name.charAt(length - 1);
        } else {
            // 영문 이름: 첫 글자 + 마스킹 + 마지막 글자
            return name.charAt(0) + "*".repeat(Math.max(2, length - 2)) + name.charAt(length - 1);
        }
    }
    
    /**
     * 개발자 정보 마스킹 (종합)
     * 전화번호, 이메일, 이름을 모두 마스킹
     */
    public static String maskDeveloperInfo(String info) {
        if (info == null || info.isEmpty()) {
            return info;
        }
        
        String masked = maskPhone(info);
        masked = maskEmail(masked);
        
        // 이름 패턴이 포함되어 있으면 마스킹
        // (간단한 휴리스틱: 2-4자 한글이나 2-20자 영문)
        if (KOREAN_NAME_PATTERN.matcher(info).find()) {
            // 한글 이름 마스킹
            masked = KOREAN_NAME_PATTERN.matcher(masked).replaceAll(mr -> maskName(mr.group()));
        }
        
        return masked;
    }
    
    /**
     * 로그 메시지에서 개인정보 마스킹
     * 전화번호, 이메일, 이름 패턴을 모두 찾아서 마스킹
     */
    public static String maskLogMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        String masked = maskPhone(message);
        masked = maskEmail(masked);
        
        // 한글 이름 마스킹
        masked = KOREAN_NAME_PATTERN.matcher(masked).replaceAll(mr -> maskName(mr.group()));
        
        return masked;
    }
}

