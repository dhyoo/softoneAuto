package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.model.Developer;
import com.softone.auto.repository.sqlite.DeveloperSqliteRepository;
import com.softone.auto.util.AppContext;
import com.softone.auto.util.AuditLogger;
import com.softone.auto.util.PrivacyMaskingUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 개발자 관리 서비스 (회사별 데이터 분리)
 */
@Slf4j
public class DeveloperService {
    
    private final DeveloperSqliteRepository repository;
    
    public DeveloperService() {
        this.repository = new DeveloperSqliteRepository();
    }
    
    /**
     * 현재 회사의 개발자 목록 조회
     */
    public List<Developer> getAllDevelopers() {
        try {
            Company currentCompany = AppContext.getInstance().getCurrentCompany();
            String companyName = currentCompany != null ? PrivacyMaskingUtil.maskName(currentCompany.getName()) : "없음";
            log.debug("개발자 목록 조회 시작 - 회사: {}", companyName);
            
            List<Developer> allDevelopers = repository.findAll();
            
            if (currentCompany == null) {
                log.warn("회사가 선택되지 않아 전체 개발자 목록 반환");
                return allDevelopers;
            }
            
            List<Developer> filtered = allDevelopers.stream()
                    .filter(dev -> currentCompany.getId().equals(dev.getCompanyId()))
                    .collect(Collectors.toList());
            
            log.debug("개발자 목록 조회 완료 - 회사: {}, 조회된 개발자 수: {}건", companyName, filtered.size());
            AuditLogger.logDataAccess("SYSTEM", "READ", "Developer", String.valueOf(filtered.size()));
            
            return filtered;
        } catch (Exception e) {
            log.error("개발자 데이터 조회 오류: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 개발자 등록 (현재 회사에 자동 할당)
     */
    public Developer createDeveloper(String name, String position, String role, 
                                    String team, String email, String phone, 
                                    String emergencyPhone, LocalDate joinDate, String notes) {
        Developer developer = new Developer();
        developer.setId(UUID.randomUUID().toString());
        
        // 현재 선택된 회사 ID 자동 설정
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany != null) {
            developer.setCompanyId(currentCompany.getId());
        }
        
        developer.setName(name);
        developer.setPosition(position);
        developer.setRole(role);
        developer.setTeam(team);
        developer.setEmail(email);
        developer.setPhone(phone);
        developer.setEmergencyPhone(emergencyPhone);
        developer.setJoinDate(joinDate);
        developer.setStatus("ACTIVE");
        developer.setNotes(notes);
        
        repository.save(developer);
        
        // 감사 로그 기록 (개인정보 마스킹)
        String maskedName = PrivacyMaskingUtil.maskName(name);
        String maskedEmail = PrivacyMaskingUtil.maskEmail(email);
        String maskedPhone = PrivacyMaskingUtil.maskPhone(phone);
        log.info("개발자 등록 완료 - 이름: {}, 이메일: {}, 전화: {}", maskedName, maskedEmail, maskedPhone);
        AuditLogger.logDataModification("SYSTEM", "CREATE", "Developer", developer.getId(), 
            "이름: " + maskedName);
        
        return developer;
    }
    
    /**
     * 개발자 정보 수정
     */
    public void updateDeveloper(Developer developer) {
        String maskedName = developer.getName() != null ? PrivacyMaskingUtil.maskName(developer.getName()) : "N/A";
        log.info("개발자 정보 수정 - ID: {}, 이름: {}", developer.getId(), maskedName);
        
        repository.update(developer);
        
        AuditLogger.logDataModification("SYSTEM", "UPDATE", "Developer", developer.getId(), 
            "이름: " + maskedName);
    }
    
    /**
     * 개발자 삭제
     */
    public void deleteDeveloper(String id) {
        log.info("개발자 삭제 - ID: {}", id);
        repository.delete(id);
        AuditLogger.logDataModification("SYSTEM", "DELETE", "Developer", id, null);
    }
    
    /**
     * ID로 개발자 찾기
     */
    public Developer getDeveloperById(String id) {
        return repository.findById(id).orElse(null);
    }
    
    /**
     * 이름으로 개발자 찾기 (현재 회사 기준)
     */
    public Developer getDeveloperByName(String name) {
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            return null;
        }
        
        // 현재 회사의 개발자만 검색
        List<Developer> developers = getAllDevelopers();
        return developers.stream()
                .filter(dev -> dev.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}

