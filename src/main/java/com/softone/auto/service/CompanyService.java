package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.repository.CompanyRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 회사 관리 서비스
 */
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    
    public CompanyService() {
        this.companyRepository = new CompanyRepository();
    }
    
    /**
     * 모든 회사 조회
     */
    public List<Company> getAllCompanies() {
        try {
            return companyRepository.findAll();
        } catch (Exception e) {
            System.err.println("회사 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("활성 회사 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("[CompanyService.createCompany] 회사 생성 시작: " + name);
        Company company = new Company();
        company.setName(name);
        company.setProjectName(projectName);
        company.setContractType(contractType);
        company.setStartDate(startDate);
        company.setEndDate(endDate);
        company.setStatus("ACTIVE");
        company.setNotes(notes);
        
        Company saved = companyRepository.save(company);
        System.out.println("[CompanyService.createCompany] 회사 생성 완료: " + name + " (ID: " + saved.getId() + ")");
        
        // 저장 후 즉시 확인
        List<Company> all = companyRepository.findAll();
        System.out.println("[CompanyService.createCompany] 저장 후 전체 회사 수: " + all.size());
        
        return saved;
    }
    
    /**
     * 회사 정보 수정
     */
    public Company updateCompany(Company company) {
        return companyRepository.save(company);
    }
    
    /**
     * 회사 삭제
     */
    public void deleteCompany(String id) {
        companyRepository.deleteById(id);
    }
}

