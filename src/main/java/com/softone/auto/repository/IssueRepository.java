package com.softone.auto.repository;

import com.softone.auto.model.Issue;
import com.softone.auto.repository.sqlite.IssueSqliteRepository;

import java.util.List;
import java.util.Optional;

/**
 * 이슈 저장소 (SQLite로 전환됨)
 * @deprecated JSON 기반 저장소에서 SQLite로 전환됨
 */
@Deprecated
public class IssueRepository {
    
    private final IssueSqliteRepository sqliteRepository;
    
    public IssueRepository() {
        try {
            this.sqliteRepository = new IssueSqliteRepository();
        } catch (Exception e) {
            System.err.println("IssueRepository 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패", e);
        }
    }
    
    /**
     * 전체 목록 조회
     */
    public List<Issue> findAll() {
        return sqliteRepository.findAll();
    }
    
    /**
     * ID로 이슈 찾기
     */
    public Optional<Issue> findById(String id) {
        return sqliteRepository.findById(id);
    }
    
    /**
     * 상태별 이슈 조회
     */
    public List<Issue> findByStatus(String status) {
        return sqliteRepository.findByStatus(status);
    }
    
    /**
     * 심각도별 이슈 조회
     */
    public List<Issue> findBySeverity(String severity) {
        return sqliteRepository.findBySeverity(severity);
    }
    
    /**
     * 이슈 저장
     */
    public void save(Issue issue) {
        sqliteRepository.save(issue);
    }
    
    /**
     * 이슈 업데이트
     */
    public void update(Issue issue) {
        sqliteRepository.update(issue);
    }
    
    /**
     * 이슈 삭제
     */
    public void delete(String id) {
        sqliteRepository.delete(id);
    }
}

