package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * 안전한 파일 경로 빌더
 * 화이트리스트 기반으로 허용된 디렉토리 내에서만 파일 경로를 생성합니다.
 * 
 * Path Traversal 공격을 방지하고, 허용된 디렉토리 외부로의 접근을 차단합니다.
 * 
 * 사용 예:
 * <pre>
 * String safePath = SafePathBuilder.buildSafePath(dataPath, "reports", "excel", "report.xlsx");
 * </pre>
 */
@Slf4j
public class SafePathBuilder {
    
    /**
     * 허용된 루트 디렉토리 목록
     */
    private static final Set<String> ALLOWED_ROOT_DIRS = Set.of(
        "data",
        "reports",
        "templates",
        "logs",
        "backup"
    );
    
    /**
     * 안전한 파일 경로 생성
     * 
     * @param basePath 기본 경로 (예: 사용자 데이터 디렉토리)
     * @param parts 경로 구성 요소들 (예: "reports", "excel", "file.xlsx")
     * @return 안전한 경로 문자열, 검증 실패 시 null
     */
    public static String buildSafePath(String basePath, String... parts) {
        if (basePath == null || basePath.trim().isEmpty()) {
            log.warn("기본 경로가 비어있습니다.");
            return null;
        }
        
        if (parts == null || parts.length == 0) {
            log.warn("경로 구성 요소가 없습니다.");
            return null;
        }
        
        // 첫 번째 부분이 허용된 루트 디렉토리인지 확인
        String firstPart = parts[0];
        if (!ALLOWED_ROOT_DIRS.contains(firstPart.toLowerCase())) {
            log.warn("허용되지 않은 루트 디렉토리: {}", firstPart);
            return null;
        }
        
        // 경로 구성 요소 검증
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) {
                log.warn("경로 구성 요소가 비어있습니다.");
                return null;
            }
            
            // Path Traversal 시도 차단
            if (!PathSecurityValidator.isValidFileName(part)) {
                log.warn("안전하지 않은 경로 구성 요소: {}", part);
                return null;
            }
        }
        
        // 경로 조합
        try {
            Path base = Paths.get(basePath).normalize().toAbsolutePath();
            Path combined = base;
            
            for (String part : parts) {
                combined = combined.resolve(part).normalize();
            }
            
            String resultPath = combined.toString();
            
            // 최종 경로가 기본 경로 내에 있는지 확인
            if (!PathSecurityValidator.isWithinAllowedDirectory(resultPath, basePath)) {
                log.warn("경로가 허용된 디렉토리 밖으로 나갑니다: {}", resultPath);
                return null;
            }
            
            // Path Traversal 패턴 최종 검증
            if (!PathSecurityValidator.isValidPath(resultPath)) {
                log.warn("최종 경로 검증 실패: {}", resultPath);
                return null;
            }
            
            return resultPath;
            
        } catch (Exception e) {
            log.error("경로 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 안전한 파일 경로 생성 (File 객체 반환)
     * 
     * @param basePath 기본 경로
     * @param parts 경로 구성 요소들
     * @return File 객체, 검증 실패 시 null
     */
    public static File buildSafeFile(String basePath, String... parts) {
        String safePath = buildSafePath(basePath, parts);
        if (safePath == null) {
            return null;
        }
        return new File(safePath);
    }
    
    /**
     * 안전한 파일 경로 생성 (Path 객체 반환)
     * 
     * @param basePath 기본 경로
     * @param parts 경로 구성 요소들
     * @return Path 객체, 검증 실패 시 null
     */
    public static Path buildSafePathObject(String basePath, String... parts) {
        String safePath = buildSafePath(basePath, parts);
        if (safePath == null) {
            return null;
        }
        return Paths.get(safePath);
    }
    
    /**
     * 디렉토리 생성 (안전한 경로로)
     * 
     * @param basePath 기본 경로
     * @param parts 경로 구성 요소들
     * @return 생성된 디렉토리 File 객체, 실패 시 null
     */
    public static File createSafeDirectory(String basePath, String... parts) {
        File dir = buildSafeFile(basePath, parts);
        if (dir == null) {
            return null;
        }
        
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.debug("디렉토리 생성 완료: {}", dir.getAbsolutePath());
            } else {
                log.warn("디렉토리 생성 실패: {}", dir.getAbsolutePath());
                return null;
            }
        }
        
        return dir;
    }
}

