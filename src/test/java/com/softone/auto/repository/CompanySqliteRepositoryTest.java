package com.softone.auto.repository;

import com.softone.auto.model.Company;
import com.softone.auto.repository.sqlite.CompanySqliteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompanySqliteRepository 테스트
 */
public class CompanySqliteRepositoryTest {
    
    private CompanySqliteRepository repository;
    
    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        // 테스트용 임시 디렉토리 사용
        System.setProperty("softone.data.path", tempDir.toString());
        
        try {
            repository = new CompanySqliteRepository();
        } catch (Exception e) {
            fail("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    @Test
    void testSaveAndFindAll() {
        System.out.println("\n=== 테스트 1: 저장 및 조회 ===");
        
        // 회사 생성
        Company company = new Company();
        company.setName("테스트 회사");
        company.setProjectName("테스트 프로젝트");
        company.setContractType("파견");
        company.setStartDate(LocalDate.of(2024, 1, 1));
        company.setEndDate(LocalDate.of(2024, 12, 31));
        company.setStatus("ACTIVE");
        company.setNotes("테스트 노트");
        
        // 저장
        System.out.println("회사 저장 시작...");
        Company saved = repository.save(company);
        System.out.println("저장 완료: " + saved.getName() + " (ID: " + saved.getId() + ")");
        
        assertNotNull(saved.getId(), "ID가 생성되어야 함");
        assertEquals("테스트 회사", saved.getName());
        
        // 조회
        System.out.println("전체 회사 조회 시작...");
        List<Company> all = repository.findAll();
        System.out.println("조회된 회사 수: " + all.size());
        
        assertFalse(all.isEmpty(), "회사 목록이 비어있지 않아야 함");
        assertEquals(1, all.size(), "회사가 1개 있어야 함");
        assertEquals("테스트 회사", all.get(0).getName());
        
        System.out.println("✓ 테스트 1 통과\n");
    }
    
    @Test
    void testFindAllActive() {
        System.out.println("\n=== 테스트 2: 활성 회사 조회 ===");
        
        // 활성 회사 생성
        Company active = new Company();
        active.setName("활성 회사");
        active.setProjectName("활성 프로젝트");
        active.setContractType("파견");
        active.setStatus("ACTIVE");
        repository.save(active);
        
        // 비활성 회사 생성
        Company inactive = new Company();
        inactive.setName("비활성 회사");
        inactive.setProjectName("비활성 프로젝트");
        inactive.setContractType("파견");
        inactive.setStatus("INACTIVE");
        repository.save(inactive);
        
        // 활성 회사만 조회
        System.out.println("활성 회사 조회 시작...");
        List<Company> activeList = repository.findAllActive();
        System.out.println("조회된 활성 회사 수: " + activeList.size());
        
        assertEquals(1, activeList.size(), "활성 회사가 1개 있어야 함");
        assertEquals("활성 회사", activeList.get(0).getName());
        
        System.out.println("✓ 테스트 2 통과\n");
    }
    
    @Test
    void testMultipleCompanies() {
        System.out.println("\n=== 테스트 3: 여러 회사 저장 및 조회 ===");
        
        // 여러 회사 생성
        for (int i = 1; i <= 3; i++) {
            Company company = new Company();
            company.setName("회사 " + i);
            company.setProjectName("프로젝트 " + i);
            company.setContractType("파견");
            company.setStatus("ACTIVE");
            repository.save(company);
            System.out.println("회사 " + i + " 저장 완료");
        }
        
        // 전체 조회
        List<Company> all = repository.findAll();
        System.out.println("전체 회사 수: " + all.size());
        
        assertEquals(3, all.size(), "회사가 3개 있어야 함");
        
        // 활성 회사 조회
        List<Company> active = repository.findAllActive();
        System.out.println("활성 회사 수: " + active.size());
        
        assertEquals(3, active.size(), "활성 회사가 3개 있어야 함");
        
        System.out.println("✓ 테스트 3 통과\n");
    }
}

