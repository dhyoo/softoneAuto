package com.softone.auto.service;

import com.softone.auto.model.CommonCode;
import com.softone.auto.repository.sqlite.CommonCodeSqliteRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 공통코드 서비스
 * 공통코드는 변경 빈도가 낮으므로 메모리 캐싱을 사용합니다.
 */
@Slf4j
public class CommonCodeService {
    
    private final CommonCodeSqliteRepository repository;
    
    // 메모리 캐시: 카테고리별 코드 목록
    private final Map<String, List<CommonCode>> categoryCache = new ConcurrentHashMap<>();
    // 전체 코드 목록 캐시
    private List<CommonCode> allCodesCache = null;
    // 캐시 무효화 플래그
    private volatile boolean cacheInvalidated = true;
    
    public CommonCodeService() {
        this.repository = new CommonCodeSqliteRepository();
    }
    
    /**
     * 캐시 무효화 (코드 생성/수정/삭제 시 호출)
     */
    public void invalidateCache() {
        cacheInvalidated = true;
        categoryCache.clear();
        allCodesCache = null;
        log.debug("CommonCode 캐시 무효화");
    }
    
    /**
     * 캐시에서 전체 코드 목록 가져오기 또는 DB에서 로드
     */
    private List<CommonCode> getAllCodesWithCache() {
        if (cacheInvalidated || allCodesCache == null) {
            allCodesCache = repository.findAll();
            cacheInvalidated = false;
            log.debug("CommonCode 전체 목록 캐시 로드: {}건", allCodesCache.size());
        }
        return allCodesCache;
    }
    
    /**
     * 전체 코드 조회 (캐싱 사용)
     */
    public List<CommonCode> getAllCodes() {
        try {
            return getAllCodesWithCache();
        } catch (Exception e) {
            log.error("공통코드 데이터 조회 오류: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 카테고리별 코드 조회 (캐싱 사용)
     */
    public List<CommonCode> getCodesByCategory(String category) {
        try {
            // 캐시에서 먼저 확인
            if (categoryCache.containsKey(category) && !cacheInvalidated) {
                log.debug("CommonCode 카테고리 캐시 히트: {}", category);
                return categoryCache.get(category);
            }
            
            // 캐시 미스 또는 무효화된 경우 DB에서 조회
            List<CommonCode> codes = repository.findByCategory(category);
            categoryCache.put(category, codes);
            log.debug("CommonCode 카테고리 캐시 로드: {} - {}건", category, codes.size());
            return codes;
        } catch (Exception e) {
            log.error("공통코드 카테고리별 조회 오류: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 코드 생성
     */
    public CommonCode createCode(String category, String code, String name, 
                                 String description, Integer sortOrder) {
        CommonCode commonCode = new CommonCode();
        commonCode.setId(UUID.randomUUID().toString());
        commonCode.setCategory(category);
        commonCode.setCode(code);
        commonCode.setName(name);
        commonCode.setDescription(description);
        commonCode.setSortOrder(sortOrder);
        commonCode.setIsActive(true);
        
        repository.save(commonCode);
        
        // 캐시 무효화
        invalidateCache();
        
        return commonCode;
    }
    
    /**
     * 코드 수정
     */
    public void updateCode(CommonCode code) {
        repository.update(code);
        
        // 캐시 무효화
        invalidateCache();
    }
    
    /**
     * 코드 삭제
     */
    public void deleteCode(String id) {
        repository.delete(id);
        
        // 캐시 무효화
        invalidateCache();
    }
    
    /**
     * 카테고리별 코드 이름 배열 반환 (ComboBox용)
     */
    public String[] getCodeNamesForComboBox(String category) {
        List<CommonCode> codes = getCodesByCategory(category);
        return codes.stream()
                .map(CommonCode::getName)
                .toArray(String[]::new);
    }
}

