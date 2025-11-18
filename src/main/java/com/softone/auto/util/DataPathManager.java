package com.softone.auto.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 데이터 저장 경로 관리자 (JSON 기반 AppConfig 사용)
 * @deprecated Use AppConfig.getInstance() instead
 */
@Deprecated
public class DataPathManager {
    
    /**
     * 데이터 저장 경로 가져오기
     * @deprecated Use AppConfig.getInstance().getOrSelectDataPath() instead
     */
    @Deprecated
    public static String getDataPath() {
        return AppConfig.getInstance().getOrSelectDataPath();
    }
    
    /**
     * 데이터 경로 재설정
     * @deprecated Use AppConfig.getInstance().resetDataPath() instead
     */
    @Deprecated
    public static void resetDataPath() {
        AppConfig.getInstance().resetDataPath();
    }
    
    /**
     * 회사별 데이터 폴더 경로
     */
    public static String getCompanyDataPath(String companyId) {
        // companyId 검증
        if (companyId == null || companyId.isEmpty()) {
            throw new IllegalArgumentException("회사 ID가 비어있습니다");
        }
        
        // Path Traversal 방지
        if (!PathSecurityValidator.isValidPath(companyId)) {
            throw new SecurityException("안전하지 않은 회사 ID: " + companyId);
        }
        
        String basePath = getDataPath();
        String sanitizedCompanyId = PathSecurityValidator.sanitizeFileName(companyId);
        String companyPath = basePath + File.separator + "companies" + File.separator + sanitizedCompanyId;
        
        // 경로 검증
        if (!PathSecurityValidator.isValidPath(companyPath)) {
            throw new SecurityException("안전하지 않은 경로: " + companyPath);
        }
        
        try {
            Path path = Paths.get(companyPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("회사 폴더 생성 실패: " + e.getMessage());
        }
        
        return companyPath;
    }
    
    /**
     * 이슈 날짜별 폴더 경로 (YYYY-MM 형식)
     */
    public static String getIssueDataPath(String companyId, String yearMonth) {
        String companyPath = getCompanyDataPath(companyId);
        String issuePath = companyPath + File.separator + "issues" + File.separator + yearMonth;
        
        try {
            Path path = Paths.get(issuePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("이슈 폴더 생성 실패: " + e.getMessage());
        }
        
        return issuePath;
    }
    
    /**
     * 백업 폴더 경로
     */
    public static String getBackupPath() {
        String basePath = getDataPath();
        String backupPath = basePath + File.separator + "backups";
        
        try {
            Path path = Paths.get(backupPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("백업 폴더 생성 실패: " + e.getMessage());
        }
        
        return backupPath;
    }
}

