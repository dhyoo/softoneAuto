package com.softone.auto.service;

import com.softone.auto.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompanyService 통합 테스트
 */
@DisplayName("CompanyService 테스트")
public class CompanyServiceTest {
    
    private CompanyService service;
    
    @BeforeEach
    void setUp() {
        System.out.println("\n=== 테스트 시작: CompanyService ===");
        service = new CompanyService();
    }
    
    @Test
    @DisplayName("1. createCompany 테스트")
    void testCreateCompany() {
        System.out.println("\n[테스트 1] createCompany");
        
        Company company = service.createCompany(
            "서비스 테스트 회사",
            "서비스 테스트 프로젝트",
            "파견",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            "서비스 테스트용"
        );
        
        assertNotNull(company, "회사가 생성되어야 함");
        assertNotNull(company.getId(), "ID가 있어야 함");
        assertEquals("서비스 테스트 회사", company.getName());
        System.out.println("  ✓ 회사 생성 성공: " + company.getName());
    }
    
    @Test
    @DisplayName("2. getAllCompanies 테스트")
    void testGetAllCompanies() {
        System.out.println("\n[테스트 2] getAllCompanies");
        
        // 데이터 생성
        service.createCompany("전체 조회 테스트 1", "프로젝트 1", "파견",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "");
        service.createCompany("전체 조회 테스트 2", "프로젝트 2", "용역",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "");
        
        // 조회
        List<Company> companies = service.getAllCompanies();
        
        assertNotNull(companies);
        assertFalse(companies.isEmpty(), "회사 목록이 비어있지 않아야 함");
        System.out.println("  ✓ 조회된 회사 수: " + companies.size());
        
        for (Company c : companies) {
            System.out.println("    - " + c.getName());
        }
    }
    
    @Test
    @DisplayName("3. getActiveCompanies 테스트")
    void testGetActiveCompanies() {
        System.out.println("\n[테스트 3] getActiveCompanies");
        
        // 활성 회사 생성
        service.createCompany("활성 회사", "활성 프로젝트", "파견",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "");
        
        // 조회
        List<Company> active = service.getActiveCompanies();
        
        assertNotNull(active);
        System.out.println("  ✓ 활성 회사 수: " + active.size());
        
        for (Company c : active) {
            assertEquals("ACTIVE", c.getStatus());
            System.out.println("    - " + c.getName() + " (상태: " + c.getStatus() + ")");
        }
    }
    
    @Test
    @DisplayName("4. getCompanyByName 테스트")
    void testGetCompanyByName() {
        System.out.println("\n[테스트 4] getCompanyByName");
        
        // 회사 생성
        service.createCompany("이름 조회 회사", "프로젝트", "파견",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "");
        
        // 이름으로 조회
        Company found = service.getCompanyByName("이름 조회 회사");
        
        assertNotNull(found, "회사를 찾아야 함");
        assertEquals("이름 조회 회사", found.getName());
        System.out.println("  ✓ 회사 조회 성공: " + found.getName());
    }
    
    @Test
    @DisplayName("5. Service 전체 플로우 테스트")
    void testServiceFullFlow() {
        System.out.println("\n[테스트 5] Service 전체 플로우");
        
        // 1. 회사 생성
        Company c1 = service.createCompany("플로우 1", "프로젝트 1", "파견",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "");
        System.out.println("  ✓ 회사 1 생성: " + c1.getName());
        
        Company c2 = service.createCompany("플로우 2", "프로젝트 2", "용역",
            LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "");
        System.out.println("  ✓ 회사 2 생성: " + c2.getName());
        
        // 2. 전체 조회
        List<Company> all = service.getAllCompanies();
        System.out.println("  ✓ 전체 조회: " + all.size() + "개");
        assertTrue(all.size() >= 2);
        
        // 3. 활성 회사 조회
        List<Company> active = service.getActiveCompanies();
        System.out.println("  ✓ 활성 회사 조회: " + active.size() + "개");
        assertTrue(active.size() >= 2);
        
        // 4. 이름으로 조회
        Company found = service.getCompanyByName("플로우 1");
        assertNotNull(found);
        System.out.println("  ✓ 이름으로 조회 성공: " + found.getName());
        
        System.out.println("\n=== Service 전체 플로우 테스트 성공 ===\n");
    }
}

