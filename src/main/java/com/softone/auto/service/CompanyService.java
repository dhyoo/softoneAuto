package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.repository.sqlite.CompanySqliteRepository;
import com.softone.auto.util.PrivacyMaskingUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 회사 관리 서비스
 */
@Slf4j
public class CompanyService {
    
    private final CompanySqliteRepository companyRepository;
    
    public CompanyService() {
        this.companyRepository = new CompanySqliteRepository();
    }
    
    /**
     * 모든 회사 조회
     */
    public List<Company> getAllCompanies() {
        try {
            return companyRepository.findAll();
        } catch (Exception e) {
            log.error("회사 데이터 조회 오류: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 활성 회사만 조회
     */
    public List<Company> getActiveCompanies() {
        try {
            return companyRepository.findAllActive();
        } catch (Exception e) {
            log.error("활성 회사 데이터 조회 오류: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * ID로 회사 조회
     */
    public Company getCompanyById(String id) {
        return companyRepository.findById(id).orElse(null);
    }
    
    /**
     * 이름으로 회사 조회
     */
    public Company getCompanyByName(String name) {
        return companyRepository.findByName(name).orElse(null);
    }
    
    /**
     * 회사 생성
     */
    public Company createCompany(String name, String projectName, String contractType,
                                  LocalDate startDate, LocalDate endDate, String notes) {
        // 로그에 회사 이름은 마스킹하지 않음 (회사명은 개인정보가 아님)
        // 다만, 필요시 PrivacyMaskingUtil.maskName(name) 사용 가능
        Company company = new Company();
        company.setName(name);
        company.setProjectName(projectName);
        company.setContractType(contractType);
        company.setStartDate(startDate);
        company.setEndDate(endDate);
        company.setStatus("ACTIVE");
        company.setNotes(notes);
        
        Company saved = companyRepository.save(company);
        
        // 로그에 회사 이름은 마스킹하지 않음 (회사명은 개인정보가 아님)
        // 다만, 필요시 PrivacyMaskingUtil.maskName(name) 사용 가능
        log.info("회사 생성 완료 - 이름: {}, ID: {}", name, saved.getId());
        
        // 저장 후 즉시 확인
        List<Company> all = companyRepository.findAll();
        log.debug("저장 후 전체 회사 수: {}건", all.size());
        
        return saved;
    }
    
    /**
     * 회사 정보 수정
     */
    public Company updateCompany(Company company) {
        log.info("회사 정보 수정 - ID: {}, 이름: {}", company.getId(), company.getName());
        Company updated = companyRepository.save(company);
        log.debug("회사 정보 수정 완료 - ID: {}", updated.getId());
        return updated;
    }
    
    /**
     * 회사 삭제
     */
    public void deleteCompany(String id) {
        log.info("회사 삭제 - ID: {}", id);
        companyRepository.deleteById(id);
        log.debug("회사 삭제 완료 - ID: {}", id);
    }
}

