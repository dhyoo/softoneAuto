package com.softone.auto.repository;

import com.softone.auto.model.Company;
import com.softone.auto.repository.sqlite.CompanySqliteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 회사 데이터 저장소 (SQLite로 전환됨)
 * @deprecated JSON 기반 저장소에서 SQLite로 전환됨
 */
@Deprecated
public class CompanyRepository {
    
    private final CompanySqliteRepository sqliteRepository;
    
    public CompanyRepository() {
        try {
            this.sqliteRepository = new CompanySqliteRepository();
            
            // 마이그레이션 자동 실행 (최초 1회) - 예외 발생 시 무시
            try {
                migrateFromJsonIfNeeded();
            } catch (Exception e) {
                System.err.println("마이그레이션 실패 (무시): " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("CompanyRepository 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패", e);
        }
    }
    
    /**
     * JSON에서 SQLite로 마이그레이션 (필요시)
     */
    private void migrateFromJsonIfNeeded() {
        try {
            List<Company> existing = sqliteRepository.findAll();
            if (existing == null || existing.isEmpty()) {
                System.out.println("회사 데이터가 없어 마이그레이션을 시도합니다...");
                com.softone.auto.util.JsonToSqliteMigrator migrator = new com.softone.auto.util.JsonToSqliteMigrator();
                migrator.migrateAll();
            }
        } catch (Exception e) {
            System.err.println("마이그레이션 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
            // 마이그레이션 실패해도 계속 진행
        }
    }
    
    /**
     * 모든 회사 조회
     */
    public List<Company> findAll() {
        return sqliteRepository.findAll();
    }
    
    /**
     * 활성 회사만 조회
     */
    public List<Company> findAllActive() {
        return sqliteRepository.findAllActive();
    }
    
    /**
     * ID로 회사 조회
     */
    public Optional<Company> findById(String id) {
        return sqliteRepository.findById(id);
    }
    
    /**
     * 이름으로 회사 조회
     */
    public Optional<Company> findByName(String name) {
        return sqliteRepository.findByName(name);
    }
    
    /**
     * 회사 저장
     */
    public Company save(Company company) {
        if (company.getId() == null || company.getId().isEmpty()) {
            company.setId(UUID.randomUUID().toString());
        }
        return sqliteRepository.save(company);
    }
    
    /**
     * 회사 삭제
     */
    public void deleteById(String id) {
        sqliteRepository.deleteById(id);
    }
}

