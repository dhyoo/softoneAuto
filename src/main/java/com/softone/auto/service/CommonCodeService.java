package com.softone.auto.service;

import com.softone.auto.model.CommonCode;
import com.softone.auto.repository.CommonCodeRepository;

import java.util.List;
import java.util.UUID;

/**
 * 공통코드 서비스
 */
public class CommonCodeService {
    
    private final CommonCodeRepository repository;
    
    public CommonCodeService() {
        this.repository = new CommonCodeRepository();
    }
    
    /**
     * 전체 코드 조회
     */
    public List<CommonCode> getAllCodes() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            System.err.println("공통코드 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 카테고리별 코드 조회
     */
    public List<CommonCode> getCodesByCategory(String category) {
        try {
            return repository.findByCategory(category);
        } catch (Exception e) {
            System.err.println("공통코드 카테고리별 조회 오류: " + e.getMessage());
            e.printStackTrace();
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
        return commonCode;
    }
    
    /**
     * 코드 수정
     */
    public void updateCode(CommonCode code) {
        repository.update(code);
    }
    
    /**
     * 코드 삭제
     */
    public void deleteCode(String id) {
        repository.delete(id);
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

