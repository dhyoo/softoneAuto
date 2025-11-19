package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.model.CustomerCommunication;
import com.softone.auto.repository.sqlite.CustomerCommunicationSqliteRepository;
import com.softone.auto.util.AppContext;
import com.softone.auto.util.AuditLogger;
import com.softone.auto.util.PrivacyMaskingUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 고객 소통 관리 서비스 (회사별 데이터 분리)
 */
@Slf4j
public class CustomerCommunicationService {
    
    private final CustomerCommunicationSqliteRepository repository;
    
    public CustomerCommunicationService() {
        this.repository = new CustomerCommunicationSqliteRepository();
    }
    
    /**
     * 현재 회사의 소통 기록 조회
     */
    public List<CustomerCommunication> getAllCommunications() {
        try {
            Company currentCompany = AppContext.getInstance().getCurrentCompany();
            List<CustomerCommunication> allCommunications = repository.findAll();
            
            if (currentCompany == null) {
                return allCommunications;
            }
            
            return allCommunications.stream()
                    .filter(comm -> currentCompany.getId().equals(comm.getCompanyId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("고객 소통 데이터 조회 오류: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 소통 기록 등록 (현재 회사에 자동 할당)
     */
    public CustomerCommunication createCommunication(String type, String title, String content,
                                                    String customerName, String ourRepresentative,
                                                    LocalDateTime communicationDate, String priority,
                                                    LocalDateTime dueDate, String notes) {
        CustomerCommunication communication = new CustomerCommunication();
        communication.setId(UUID.randomUUID().toString());
        
        // 현재 선택된 회사 ID 자동 설정
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            throw new IllegalStateException("회사가 선택되지 않았습니다. 고객소통을 생성하려면 먼저 회사를 선택해주세요.");
        }
        communication.setCompanyId(currentCompany.getId());
        
        communication.setType(type);
        communication.setTitle(title);
        communication.setContent(content);
        communication.setCustomerName(customerName);
        communication.setOurRepresentative(ourRepresentative);
        communication.setCommunicationDate(communicationDate);
        communication.setStatus("PENDING");
        communication.setPriority(priority);
        communication.setDueDate(dueDate);
        communication.setNotes(notes);
        
        repository.save(communication);
        
        // 감사 로그 기록 (개인정보 마스킹)
        String maskedCustomerName = PrivacyMaskingUtil.maskName(customerName);
        String maskedRepresentative = PrivacyMaskingUtil.maskName(ourRepresentative);
        log.info("고객 소통 등록 완료 - 고객사: {}, 담당자: {}, 제목: {}", 
            maskedCustomerName, maskedRepresentative, title);
        AuditLogger.logDataModification("SYSTEM", "CREATE", "CustomerCommunication", communication.getId(), 
            "고객사: " + maskedCustomerName + ", 담당자: " + maskedRepresentative);
        
        return communication;
    }
    
    /**
     * 소통 기록 수정
     */
    public void updateCommunication(CustomerCommunication communication) {
        // 완료 상태로 변경 시 완료일 설정
        if ("COMPLETED".equals(communication.getStatus()) && communication.getCompletedDate() == null) {
            communication.setCompletedDate(LocalDateTime.now());
        }
        
        repository.update(communication);
        
        // 감사 로그 기록
        String maskedCustomerName = communication.getCustomerName() != null ? 
            PrivacyMaskingUtil.maskName(communication.getCustomerName()) : "N/A";
        log.info("고객 소통 수정 완료 - ID: {}, 고객사: {}, 상태: {}", 
            communication.getId(), maskedCustomerName, communication.getStatus());
        AuditLogger.logDataModification("SYSTEM", "UPDATE", "CustomerCommunication", communication.getId(), 
            "고객사: " + maskedCustomerName + ", 상태: " + communication.getStatus());
    }
    
    /**
     * 소통 기록 삭제
     */
    public void deleteCommunication(String id) {
        log.info("고객 소통 삭제 - ID: {}", id);
        repository.delete(id);
        AuditLogger.logDataModification("SYSTEM", "DELETE", "CustomerCommunication", id, null);
    }
    
    /**
     * 유형별 소통 기록 조회
     */
    public List<CustomerCommunication> getCommunicationsByType(String type) {
        return repository.findByType(type);
    }
    
    /**
     * 상태별 소통 기록 조회
     */
    public List<CustomerCommunication> getCommunicationsByStatus(String status) {
        return repository.findByStatus(status);
    }
    
    /**
     * 현재 회사의 대기 중인 소통 기록 조회
     */
    public List<CustomerCommunication> getPendingCommunications() {
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        List<CustomerCommunication> pending = repository.findByStatus("PENDING");
        
        if (currentCompany == null) {
            return pending;
        }
        
        return pending.stream()
                .filter(comm -> currentCompany.getId().equals(comm.getCompanyId()))
                .collect(Collectors.toList());
    }
}

