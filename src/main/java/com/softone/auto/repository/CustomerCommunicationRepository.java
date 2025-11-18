package com.softone.auto.repository;

import com.softone.auto.model.CustomerCommunication;
import com.softone.auto.repository.sqlite.CustomerCommunicationSqliteRepository;

import java.util.List;
import java.util.Optional;

/**
 * 고객 소통 저장소 (SQLite로 전환됨)
 * @deprecated JSON 기반 저장소에서 SQLite로 전환됨
 */
@Deprecated
public class CustomerCommunicationRepository {
    
    private final CustomerCommunicationSqliteRepository sqliteRepository;
    
    public CustomerCommunicationRepository() {
        try {
            this.sqliteRepository = new CustomerCommunicationSqliteRepository();
        } catch (Exception e) {
            System.err.println("CustomerCommunicationRepository 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패", e);
        }
    }
    
    /**
     * 전체 목록 조회
     */
    public List<CustomerCommunication> findAll() {
        return sqliteRepository.findAll();
    }
    
    /**
     * ID로 소통 기록 찾기
     */
    public Optional<CustomerCommunication> findById(String id) {
        return sqliteRepository.findById(id);
    }
    
    /**
     * 유형별 소통 기록 조회
     */
    public List<CustomerCommunication> findByType(String type) {
        return sqliteRepository.findByType(type);
    }
    
    /**
     * 상태별 소통 기록 조회
     */
    public List<CustomerCommunication> findByStatus(String status) {
        return sqliteRepository.findByStatus(status);
    }
    
    /**
     * 소통 기록 저장
     */
    public void save(CustomerCommunication communication) {
        sqliteRepository.save(communication);
    }
    
    /**
     * 소통 기록 업데이트
     */
    public void update(CustomerCommunication communication) {
        sqliteRepository.update(communication);
    }
    
    /**
     * 소통 기록 삭제
     */
    public void delete(String id) {
        sqliteRepository.delete(id);
    }
}

