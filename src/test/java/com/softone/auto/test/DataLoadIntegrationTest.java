package com.softone.auto.test;

import com.softone.auto.model.Company;
import com.softone.auto.service.CompanyService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 데이터 로드 통합 테스트
 * 실제 애플리케이션과 동일한 환경에서 테스트
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DataLoadIntegrationTest {
    
    private CompanyService companyService;
    
    @BeforeAll
    void setUp() {
        System.out.println("\n========================================");
        System.out.println("데이터 로드 통합 테스트 시작");
        System.out.println("========================================\n");
        
        try {
            companyService = new CompanyService();
            System.out.println("✓ CompanyService 초기화 완료");
        } catch (Exception e) {
            System.err.println("✗ CompanyService 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            fail("CompanyService 초기화 실패", e);
        }
    }
    
    @Test
    void testCreateAndLoadCompany() {
        System.out.println("\n[테스트 1] 회사 생성 및 조회");
        System.out.println("----------------------------------------");
        
        try {
            // 회사 생성
            System.out.println("1. 회사 생성 시작...");
            Company company = companyService.createCompany(
                "통합테스트 회사",
                "통합테스트 프로젝트",
                "파견",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "통합 테스트용 회사"
            );
            System.out.println("   ✓ 회사 생성 완료: " + company.getName() + " (ID: " + company.getId() + ")");
            
            assertNotNull(company.getId(), "ID가 생성되어야 함");
            assertEquals("통합테스트 회사", company.getName());
            
            // 전체 조회
            System.out.println("\n2. 전체 회사 조회...");
            List<Company> all = companyService.getAllCompanies();
            System.out.println("   → 조회된 회사 수: " + all.size());
            
            assertFalse(all.isEmpty(), "회사 목록이 비어있지 않아야 함");
            boolean found = all.stream().anyMatch(c -> c.getName().equals("통합테스트 회사"));
            assertTrue(found, "생성한 회사가 조회되어야 함");
            
            // 활성 회사 조회
            System.out.println("\n3. 활성 회사 조회...");
            List<Company> active = companyService.getActiveCompanies();
            System.out.println("   → 조회된 활성 회사 수: " + active.size());
            
            assertFalse(active.isEmpty(), "활성 회사 목록이 비어있지 않아야 함");
            
            System.out.println("\n✓ [테스트 1] 통과");
            
        } catch (Exception e) {
            System.err.println("\n✗ [테스트 1] 실패: " + e.getMessage());
            e.printStackTrace();
            fail("회사 생성 및 조회 실패", e);
        }
    }
    
    @Test
    void testLoadExistingCompanies() {
        System.out.println("\n[테스트 2] 기존 회사 조회");
        System.out.println("----------------------------------------");
        
        try {
            // 전체 회사 조회
            System.out.println("1. 전체 회사 조회...");
            List<Company> all = companyService.getAllCompanies();
            System.out.println("   → 조회된 회사 수: " + all.size());
            
            if (all.isEmpty()) {
                System.out.println("   ⚠️ 회사 데이터가 없습니다.");
            } else {
                System.out.println("   회사 목록:");
                for (Company c : all) {
                    System.out.println("     - " + c.getName() + " (" + c.getProjectName() + ") [ID: " + c.getId() + "]");
                }
            }
            
            // 활성 회사 조회
            System.out.println("\n2. 활성 회사 조회...");
            List<Company> active = companyService.getActiveCompanies();
            System.out.println("   → 조회된 활성 회사 수: " + active.size());
            
            if (active.isEmpty()) {
                System.out.println("   ⚠️ 활성 회사 데이터가 없습니다.");
            } else {
                System.out.println("   활성 회사 목록:");
                for (Company c : active) {
                    System.out.println("     - " + c.getName() + " (" + c.getProjectName() + ")");
                }
            }
            
            System.out.println("\n✓ [테스트 2] 완료");
            
        } catch (Exception e) {
            System.err.println("\n✗ [테스트 2] 실패: " + e.getMessage());
            e.printStackTrace();
            fail("기존 회사 조회 실패", e);
        }
    }
    
    @Test
    void testSampleDataInitialization() {
        System.out.println("\n[테스트 3] 샘플 데이터 초기화 시뮬레이션");
        System.out.println("----------------------------------------");
        
        try {
            // 샘플 회사 생성
            String[] companyNames = {"LG전자", "SK텔레콤", "삼성SDS"};
            
            System.out.println("1. 샘플 회사 생성...");
            for (String name : companyNames) {
                try {
                    Company existing = companyService.getCompanyByName(name);
                    if (existing != null) {
                        System.out.println("   - " + name + " (이미 존재)");
                        continue;
                    }
                    
                    Company company = companyService.createCompany(
                        name,
                        name + " 프로젝트",
                        "파견",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 12, 31),
                        "샘플 데이터"
                    );
                    System.out.println("   ✓ " + name + " 생성 완료 (ID: " + company.getId() + ")");
                } catch (Exception e) {
                    System.err.println("   ✗ " + name + " 생성 실패: " + e.getMessage());
                }
            }
            
            // 생성 후 조회
            System.out.println("\n2. 생성 후 전체 조회...");
            List<Company> all = companyService.getAllCompanies();
            System.out.println("   → 전체 회사 수: " + all.size());
            
            // 각 샘플 회사 확인
            for (String name : companyNames) {
                Company found = companyService.getCompanyByName(name);
                if (found != null) {
                    System.out.println("   ✓ " + name + " 확인됨");
                } else {
                    System.err.println("   ✗ " + name + " 확인 실패");
                }
            }
            
            System.out.println("\n✓ [테스트 3] 완료");
            
        } catch (Exception e) {
            System.err.println("\n✗ [테스트 3] 실패: " + e.getMessage());
            e.printStackTrace();
            fail("샘플 데이터 초기화 실패", e);
        }
    }
}

