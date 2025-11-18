package com.softone.auto.repository;

import com.softone.auto.model.Developer;
import com.softone.auto.repository.sqlite.DeveloperSqliteRepository;

import java.util.List;
import java.util.Optional;

/**
 * 개발자 정보 저장소 (SQLite로 전환됨)
 * @deprecated JSON 기반 저장소에서 SQLite로 전환됨
 */
@Deprecated
public class DeveloperRepository {
    
    private final DeveloperSqliteRepository sqliteRepository;
    
    public DeveloperRepository() {
        try {
            this.sqliteRepository = new DeveloperSqliteRepository();
            
            // 마이그레이션 자동 실행 (최초 1회) - 예외 발생 시 무시
            try {
                migrateFromJsonIfNeeded();
            } catch (Exception e) {
                System.err.println("마이그레이션 실패 (무시): " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("DeveloperRepository 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패", e);
        }
    }
    
    /**
     * JSON에서 SQLite로 마이그레이션 (필요시)
     */
    private void migrateFromJsonIfNeeded() {
        try {
            List<Developer> existing = sqliteRepository.findAll();
            if (existing == null || existing.isEmpty()) {
                // 마이그레이션은 CompanyRepository에서 한 번만 실행
                // 중복 실행 방지
            }
        } catch (Exception e) {
            System.err.println("마이그레이션 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 전체 목록 조회
     */
    public List<Developer> findAll() {
        return sqliteRepository.findAll();
    }
    
    /**
     * ID로 개발자 찾기
     */
    public Optional<Developer> findById(String id) {
        return sqliteRepository.findById(id);
    }
    
    /**
     * 이름으로 개발자 찾기
     */
    public Optional<Developer> findByName(String name) {
        return sqliteRepository.findByName(name);
    }
    
    /**
     * 개발자 저장
     */
    public void save(Developer developer) {
        sqliteRepository.save(developer);
    }
    
    /**
     * 개발자 업데이트
     */
    public void update(Developer developer) {
        sqliteRepository.update(developer);
    }
    
    /**
     * 개발자 삭제
     */
    public void delete(String id) {
        sqliteRepository.delete(id);
    }
}

