package com.softone.auto.repository.sqlite;

import com.softone.auto.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompanySqliteRepository 통합 테스트
 * SQLite 데이터 저장/조회 및 Connection 공유 검증
 */
class CompanySqliteRepositoryTest {
    
    @TempDir
    Path tempDir;
    
    private String testDbPath;
    private CompanySqliteRepository repository1;
    private CompanySqliteRepository repository2;
    
    @BeforeEach
    void setUp() {
        // 테스트용 데이터베이스 경로 설정
        testDbPath = tempDir.resolve("test_softone.db").toString();
        System.out.println("=== 테스트 데이터베이스 경로: " + testDbPath + " ===");
        
        // DataPathManager를 임시로 오버라이드하기 위해 리플렉션 사용
        // 대신 직접 경로를 설정할 수 있는 방법 사용
        setupTestDataPath();
    }
    
    private void setupTestDataPath() {
        // 테스트용으로 시스템 프로퍼티 설정
        System.setProperty("softone.data.path", tempDir.toString());
    }
    
    @Test
    void testSaveAndFindAll() {
        System.out.println("\n=== 테스트 1: 저장 및 조회 ===");
        
        // Repository 인스턴스 생성
        repository1 = createTestRepository();
        
        // 회사 생성
        Company company = new Company();
        company.setId("test-company-1");
        company.setName("테스트 회사");
        company.setProjectName("테스트 프로젝트");
        company.setContractType("파견");
        company.setStartDate(LocalDate.of(2024, 1, 1));
        company.setEndDate(LocalDate.of(2024, 12, 31));
        company.setStatus("ACTIVE");
        company.setNotes("테스트 노트");
        
        // 저장
        System.out.println("회사 저장 중...");
        Company saved = repository1.save(company);
        assertNotNull(saved);
        assertEquals("테스트 회사", saved.getName());
        System.out.println("✓ 저장 완료: " + saved.getName());
        
        // 조회
        System.out.println("회사 목록 조회 중...");
        List<Company> all = repository1.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty(), "저장한 회사가 조회되어야 합니다");
        assertEquals(1, all.size(), "저장한 회사 1개가 조회되어야 합니다");
        assertEquals("테스트 회사", all.get(0).getName());
        System.out.println("✓ 조회 완료: " + all.size() + "개");
    }
    
    @Test
    void testMultipleRepositoryInstancesShareData() {
        System.out.println("\n=== 테스트 2: 여러 Repository 인스턴스 간 데이터 공유 ===");
        
        // 첫 번째 Repository로 데이터 저장
        repository1 = createTestRepository();
        Company company = new Company();
        company.setId("test-company-2");
        company.setName("공유 테스트 회사");
        company.setProjectName("공유 프로젝트");
        company.setContractType("용역");
        company.setStartDate(LocalDate.of(2024, 1, 1));
        company.setEndDate(LocalDate.of(2024, 12, 31));
        company.setStatus("ACTIVE");
        
        System.out.println("Repository1로 회사 저장 중...");
        repository1.save(company);
        System.out.println("✓ Repository1 저장 완료");
        
        // 두 번째 Repository로 같은 데이터 조회
        repository2 = createTestRepository();
        System.out.println("Repository2로 회사 목록 조회 중...");
        List<Company> all = repository2.findAll();
        
        assertNotNull(all);
        assertFalse(all.isEmpty(), "두 번째 Repository에서도 저장한 데이터가 조회되어야 합니다");
        assertEquals(1, all.size(), "저장한 회사 1개가 조회되어야 합니다");
        assertEquals("공유 테스트 회사", all.get(0).getName());
        System.out.println("✓ Repository2 조회 완료: " + all.size() + "개");
        System.out.println("✓ 여러 Repository 인스턴스가 같은 데이터베이스를 공유함을 확인");
    }
    
    @Test
    void testFindAllActive() {
        System.out.println("\n=== 테스트 3: 활성 회사만 조회 ===");
        
        repository1 = createTestRepository();
        
        // 활성 회사 생성
        Company active1 = new Company();
        active1.setId("active-1");
        active1.setName("활성 회사 1");
        active1.setProjectName("프로젝트 1");
        active1.setStatus("ACTIVE");
        repository1.save(active1);
        
        Company active2 = new Company();
        active2.setId("active-2");
        active2.setName("활성 회사 2");
        active2.setProjectName("프로젝트 2");
        active2.setStatus("ACTIVE");
        repository1.save(active2);
        
        // 비활성 회사 생성
        Company inactive = new Company();
        inactive.setId("inactive-1");
        inactive.setName("비활성 회사");
        inactive.setProjectName("프로젝트 3");
        inactive.setStatus("INACTIVE");
        repository1.save(inactive);
        
        System.out.println("활성 회사만 조회 중...");
        List<Company> active = repository1.findAllActive();
        
        assertNotNull(active);
        assertEquals(2, active.size(), "활성 회사 2개만 조회되어야 합니다");
        assertTrue(active.stream().allMatch(c -> "ACTIVE".equals(c.getStatus())));
        System.out.println("✓ 활성 회사 조회 완료: " + active.size() + "개");
    }
    
    @Test
    void testSampleDataInitialization() {
        System.out.println("\n=== 테스트 4: 샘플 데이터 초기화 시뮬레이션 ===");
        
        repository1 = createTestRepository();
        
        // 샘플 데이터 생성 (SampleDataInitializer와 동일한 방식)
        Company lg = new Company();
        lg.setId("lg-electronics");
        lg.setName("LG전자");
        lg.setProjectName("스마트홈 플랫폼 구축");
        lg.setContractType("파견");
        lg.setStartDate(LocalDate.of(2024, 1, 1));
        lg.setEndDate(LocalDate.of(2024, 12, 31));
        lg.setStatus("ACTIVE");
        repository1.save(lg);
        System.out.println("✓ LG전자 저장");
        
        Company sk = new Company();
        sk.setId("sk-telecom");
        sk.setName("SK텔레콤");
        sk.setProjectName("5G 기지국 관리 시스템");
        sk.setContractType("용역");
        sk.setStartDate(LocalDate.of(2024, 3, 1));
        sk.setEndDate(LocalDate.of(2024, 11, 30));
        sk.setStatus("ACTIVE");
        repository1.save(sk);
        System.out.println("✓ SK텔레콤 저장");
        
        Company samsung = new Company();
        samsung.setId("samsung-sds");
        samsung.setName("삼성SDS");
        samsung.setProjectName("ERP 시스템 고도화");
        samsung.setContractType("파견");
        samsung.setStartDate(LocalDate.of(2023, 9, 1));
        samsung.setEndDate(LocalDate.of(2024, 8, 31));
        samsung.setStatus("ACTIVE");
        repository1.save(samsung);
        System.out.println("✓ 삼성SDS 저장");
        
        // 다른 Repository 인스턴스로 조회 (MainFrame의 CompanyService 시뮬레이션)
        repository2 = createTestRepository();
        System.out.println("새로운 Repository 인스턴스로 조회 중...");
        List<Company> all = repository2.findAllActive();
        
        assertNotNull(all);
        assertEquals(3, all.size(), "샘플 데이터 3개가 모두 조회되어야 합니다");
        assertTrue(all.stream().anyMatch(c -> "LG전자".equals(c.getName())));
        assertTrue(all.stream().anyMatch(c -> "SK텔레콤".equals(c.getName())));
        assertTrue(all.stream().anyMatch(c -> "삼성SDS".equals(c.getName())));
        System.out.println("✓ 샘플 데이터 조회 완료: " + all.size() + "개");
    }
    
    /**
     * 테스트용 Repository 생성 (실제 DataPathManager 대신 테스트 경로 사용)
     */
    private CompanySqliteRepository createTestRepository() {
        try {
            // 리플렉션을 사용하여 DataPathManager를 임시로 오버라이드
            // 또는 직접 경로를 받는 생성자 사용
            
            // 간단한 방법: DataPathManager의 getDataPath()가 테스트 경로를 반환하도록 설정
            // 하지만 이는 복잡하므로, 대신 실제 구현을 확인하고 테스트
            
            // 실제로는 DataPathManager를 모킹하거나, 
            // Repository 생성자가 경로를 받도록 수정해야 함
            // 여기서는 실제 구현을 테스트
            
            return new CompanySqliteRepository();
        } catch (Exception e) {
            System.err.println("Repository 생성 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 생성 실패", e);
        }
    }
}

