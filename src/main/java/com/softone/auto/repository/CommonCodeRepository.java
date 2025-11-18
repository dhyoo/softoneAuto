package com.softone.auto.repository;

import com.softone.auto.model.CommonCode;
import com.softone.auto.repository.sqlite.CommonCodeSqliteRepository;

import java.util.List;

/**
 * 공통코드 저장소 (SQLite로 전환됨)
 * @deprecated JSON 기반 저장소에서 SQLite로 전환됨
 */
@Deprecated
public class CommonCodeRepository {
    
    private final CommonCodeSqliteRepository sqliteRepository;
    
    public CommonCodeRepository() {
        try {
            this.sqliteRepository = new CommonCodeSqliteRepository();
        } catch (Exception e) {
            System.err.println("CommonCodeRepository 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패", e);
        }
    }
    
    /**
     * 전체 목록 조회
     */
    public List<CommonCode> findAll() {
        return sqliteRepository.findAll();
    }
    
    /**
     * 카테고리별 코드 조회
     */
    public List<CommonCode> findByCategory(String category) {
        return sqliteRepository.findByCategory(category);
    }
    
    /**
     * ID로 코드 찾기
     */
    public CommonCode findById(String id) {
        return sqliteRepository.findById(id);
    }
    
    /**
     * 코드 저장
     */
    public void save(CommonCode code) {
        sqliteRepository.save(code);
    }
    
    /**
     * 코드 업데이트
     */
    public void update(CommonCode code) {
        sqliteRepository.update(code);
    }
    
    /**
     * 코드 삭제
     */
    public void delete(String id) {
        sqliteRepository.delete(id);
    }
}

