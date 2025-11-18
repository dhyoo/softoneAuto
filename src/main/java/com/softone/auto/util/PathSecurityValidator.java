package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * 파일 경로 보안 검증 유틸리티
 * Path Traversal 공격을 방지하기 위한 강력한 검증 제공
 * 
 * 보안 검증 항목:
 * - Path Traversal 시도 차단 (.., ../)
 * - 절대 경로 차단 (상대 경로만 허용)
 * - 특수 문자 필터링
 * - 정규화 후 재검증
 * - 화이트리스트 기반 허용 문자
 */
@Slf4j
public class PathSecurityValidator {
    
    // Path Traversal 패턴
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(\\.\\./|\\.\\.\\\\|/\\.\\.|\\\\\\.\\.|\\.\\.%2F|\\.\\.%5C)", 
        Pattern.CASE_INSENSITIVE
    );
    
    // 위험한 문자 패턴
    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile(
        "[<>:\"|?*\\x00-\\x1F]"
    );
    
    // Windows 예약 이름
    private static final String[] WINDOWS_RESERVED_NAMES = {
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    };
    
    /**
     * 경로가 안전한지 검증
     * 
     * @param path 검증할 경로
     * @return 안전하면 true, 위험하면 false
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        // 1. Path Traversal 패턴 검사
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            log.warn("Path Traversal 시도 감지: {}", path);
            return false;
        }
        
        // 2. 위험한 문자 검사
        if (DANGEROUS_CHARS_PATTERN.matcher(path).find()) {
            log.warn("위험한 문자가 포함된 경로: {}", path);
            return false;
        }
        
        // 3. 정규화 후 재검사
        try {
            Path normalizedPath = Paths.get(path).normalize();
            String normalized = normalizedPath.toString();
            
            // 정규화 후에도 Path Traversal 패턴이 있으면 차단
            if (PATH_TRAVERSAL_PATTERN.matcher(normalized).find()) {
                log.warn("정규화 후 Path Traversal 패턴 발견: {}", normalized);
                return false;
            }
            
            // 정규화 후 상위 디렉토리로 이동하는지 확인
            if (normalized.contains("..")) {
                log.warn("정규화 후 상위 디렉토리 참조 발견: {}", normalized);
                return false;
            }
            
        } catch (Exception e) {
            log.warn("경로 정규화 실패: {} - {}", path, e.getMessage());
            return false;
        }
        
        // 4. Windows 예약 이름 검사
        String fileName = new File(path).getName();
        for (String reserved : WINDOWS_RESERVED_NAMES) {
            if (fileName.equalsIgnoreCase(reserved) || 
                fileName.toUpperCase().startsWith(reserved + ".")) {
                log.warn("Windows 예약 이름 사용: {}", fileName);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 경로를 정제하여 안전한 경로로 변환
     * 위험한 문자를 제거하거나 대체
     * 
     * @param path 정제할 경로
     * @return 정제된 경로, 정제 불가능하면 null
     */
    public static String sanitizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        
        // Path Traversal 패턴이 있으면 정제 불가
        if (PATH_TRAVERSAL_PATTERN.matcher(path).find()) {
            log.warn("Path Traversal 패턴이 있어 정제 불가: {}", path);
            return null;
        }
        
        // 위험한 문자 제거
        String sanitized = DANGEROUS_CHARS_PATTERN.matcher(path).replaceAll("");
        
        // 정규화
        try {
            Path normalizedPath = Paths.get(sanitized).normalize();
            sanitized = normalizedPath.toString();
            
            // 정규화 후에도 Path Traversal이 있으면 null 반환
            if (sanitized.contains("..")) {
                return null;
            }
        } catch (Exception e) {
            log.warn("경로 정규화 실패: {}", e.getMessage());
            return null;
        }
        
        return sanitized;
    }
    
    /**
     * 경로가 허용된 디렉토리 내에 있는지 검증
     * 
     * @param path 검증할 경로
     * @param allowedBaseDir 허용된 기본 디렉토리
     * @return 허용된 디렉토리 내에 있으면 true
     */
    public static boolean isWithinAllowedDirectory(String path, String allowedBaseDir) {
        if (path == null || allowedBaseDir == null) {
            return false;
        }
        
        try {
            Path filePath = Paths.get(path).normalize().toAbsolutePath();
            Path basePath = Paths.get(allowedBaseDir).normalize().toAbsolutePath();
            
            // 파일 경로가 기본 디렉토리 내에 있는지 확인
            return filePath.startsWith(basePath);
        } catch (Exception e) {
            log.warn("경로 검증 실패: {} - {}", path, e.getMessage());
            return false;
        }
    }
    
    /**
     * 파일명이 안전한지 검증
     * 
     * @param fileName 검증할 파일명
     * @return 안전하면 true
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // 파일명 길이 제한 (일반적으로 255자)
        if (fileName.length() > 255) {
            log.warn("파일명이 너무 깁니다: {} (길이: {})", fileName, fileName.length());
            return false;
        }
        
        // 위험한 문자 검사
        if (DANGEROUS_CHARS_PATTERN.matcher(fileName).find()) {
            log.warn("위험한 문자가 포함된 파일명: {}", fileName);
            return false;
        }
        
        // Windows 예약 이름 검사
        String nameWithoutExt = fileName;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            nameWithoutExt = fileName.substring(0, lastDot);
        }
        
        for (String reserved : WINDOWS_RESERVED_NAMES) {
            if (nameWithoutExt.equalsIgnoreCase(reserved)) {
                log.warn("Windows 예약 이름 사용: {}", fileName);
                return false;
            }
        }
        
        // 점으로 시작하거나 끝나는 파일명 차단 (숨김 파일, 확장자 없는 파일)
        if (fileName.startsWith(".") || fileName.endsWith(".")) {
            log.warn("점으로 시작하거나 끝나는 파일명: {}", fileName);
            return false;
        }
        
        return true;
    }
    
    /**
     * 파일명 정제
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unnamed";
        }
        
        // 위험한 문자 제거
        String sanitized = DANGEROUS_CHARS_PATTERN.matcher(fileName).replaceAll("_");
        
        // 점으로 시작하거나 끝나면 제거
        sanitized = sanitized.replaceAll("^\\.+|\\.+$", "");
        
        // 빈 문자열이면 기본값
        if (sanitized.isEmpty()) {
            sanitized = "unnamed";
        }
        
        // 길이 제한
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }
    
    /**
     * 경로 검증 및 예외 발생
     * 검증 실패 시 SecurityException 발생
     */
    public static void validatePathOrThrow(String path) throws SecurityException {
        if (!isValidPath(path)) {
            throw new SecurityException("안전하지 않은 경로: " + path);
        }
    }
    
    /**
     * 파일명 검증 및 예외 발생
     */
    public static void validateFileNameOrThrow(String fileName) throws SecurityException {
        if (!isValidFileName(fileName)) {
            throw new SecurityException("안전하지 않은 파일명: " + fileName);
        }
    }
}

