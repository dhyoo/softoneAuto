package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.model.Developer;
import com.softone.auto.repository.DeveloperRepository;
import com.softone.auto.util.AppContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 개발자 관리 서비스 (회사별 데이터 분리)
 */
public class DeveloperService {
    
    private final DeveloperRepository repository;
    
    public DeveloperService() {
        this.repository = new DeveloperRepository();
    }
    
    /**
     * 현재 회사의 개발자 목록 조회
     */
    public List<Developer> getAllDevelopers() {
        try {
            Company currentCompany = AppContext.getInstance().getCurrentCompany();
            List<Developer> allDevelopers = repository.findAll();
            
            if (currentCompany == null) {
                return allDevelopers;
            }
            
            return allDevelopers.stream()
                    .filter(dev -> currentCompany.getId().equals(dev.getCompanyId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("개발자 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
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
        return developer;
    }
    
    /**
     * 개발자 정보 수정
     */
    public void updateDeveloper(Developer developer) {
        repository.update(developer);
    }
    
    /**
     * 개발자 삭제
     */
    public void deleteDeveloper(String id) {
        repository.delete(id);
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

