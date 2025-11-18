package com.softone.auto.repository;

import com.softone.auto.model.Company;
import com.softone.auto.repository.sqlite.CompanySqliteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompanyRepository 테스트
 */
public class CompanyRepositoryTest {
    
    @TempDir
    Path tempDir;
    
    private CompanySqliteRepository repository;
    private String testDbPath;
    
    @BeforeEach
    void setUp() {
        // 테스트용 데이터베이스 경로 설정
        testDbPath = tempDir.resolve("test_softone.db").toString();
        System.out.println("=== 테스트 데이터베이스 경로: " + testDbPath + " ===");
        
        // DataPathManager를 임시로 오버라이드하기 위해 리플렉션 사용
        // 또는 테스트용 Repository 생성자에 경로를 전달할 수 있도록 수정 필요
        // 일단 기본 생성자로 테스트
    }
    
    @Test
    void testSaveAndFindAll() {
        System.out.println("\n=== testSaveAndFindAll 시작 ===");
        
        try {
            // Repository 생성 (실제 경로 사용)
            repository = new CompanySqliteRepository();
            
            // 테스트 데이터 생성
            Company company = new Company();
            company.setId("test-company-1");
            company.setName("테스트 회사");
            company.setProjectName("테스트 프로젝트");
            company.setContractType("파견");
            company.setStartDate(LocalDate.of(2024, 1, 1));
            company.setEndDate(LocalDate.of(2024, 12, 31));
            company.setStatus("ACTIVE");
            company.setNotes("테스트 노트");
            
            System.out.println("1. 회사 저장 시작: " + company.getName());
            Company saved = repository.save(company);
            System.out.println("   → 저장 완료: " + saved.getName() + " (ID: " + saved.getId() + ")");
            
            // 저장 확인
            assertNotNull(saved);
            assertEquals("테스트 회사", saved.getName());
            
            System.out.println("2. 전체 조회 시작");
            List<Company> all = repository.findAll();
            System.out.println("   → 조회된 회사 수: " + all.size());
            
            // 조회 확인
            assertFalse(all.isEmpty(), "회사 목록이 비어있습니다!");
            assertEquals(1, all.size(), "회사가 1개여야 합니다");
            assertEquals("테스트 회사", all.get(0).getName());
            
            System.out.println("3. 활성 회사 조회 시작");
            List<Company> active = repository.findAllActive();
            System.out.println("   → 조회된 활성 회사 수: " + active.size());
            
            // 활성 회사 확인
            assertFalse(active.isEmpty(), "활성 회사 목록이 비어있습니다!");
            assertEquals(1, active.size(), "활성 회사가 1개여야 합니다");
            
            System.out.println("=== testSaveAndFindAll 성공 ===\n");
            
        } catch (Exception e) {
            System.err.println("=== testSaveAndFindAll 실패 ===");
            e.printStackTrace();
            fail("테스트 실패: " + e.getMessage());
        }
    }
    
    @Test
    void testMultipleCompanies() {
        System.out.println("\n=== testMultipleCompanies 시작 ===");
        
        try {
            repository = new CompanySqliteRepository();
            
            // 여러 회사 저장
            for (int i = 1; i <= 3; i++) {
                Company company = new Company();
                company.setName("테스트 회사 " + i);
                company.setProjectName("프로젝트 " + i);
                company.setContractType("파견");
                company.setStartDate(LocalDate.of(2024, 1, 1));
                company.setEndDate(LocalDate.of(2024, 12, 31));
                company.setStatus("ACTIVE");
                
                System.out.println("회사 " + i + " 저장: " + company.getName());
                repository.save(company);
            }
            
            // 전체 조회
            List<Company> all = repository.findAll();
            System.out.println("전체 회사 수: " + all.size());
            
            assertEquals(3, all.size(), "3개의 회사가 있어야 합니다");
            
            // 활성 회사 조회
            List<Company> active = repository.findAllActive();
            System.out.println("활성 회사 수: " + active.size());
            
            assertEquals(3, active.size(), "3개의 활성 회사가 있어야 합니다");
            
            System.out.println("=== testMultipleCompanies 성공 ===\n");
            
        } catch (Exception e) {
            System.err.println("=== testMultipleCompanies 실패 ===");
            e.printStackTrace();
            fail("테스트 실패: " + e.getMessage());
        }
    }
}
