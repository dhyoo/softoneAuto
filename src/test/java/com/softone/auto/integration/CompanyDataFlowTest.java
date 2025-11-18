package com.softone.auto.integration;

import com.softone.auto.model.Company;
import com.softone.auto.repository.CompanyRepository;
import com.softone.auto.service.CompanyService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 회사 데이터 플로우 통합 테스트 (DDD 방식)
 * 
 * 테스트 시나리오:
 * 1. 도메인 레이어: Repository 직접 테스트
 * 2. 애플리케이션 레이어: Service 테스트
 * 3. 통합 테스트: 전체 플로우 테스트
 */
@DisplayName("회사 데이터 플로우 통합 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CompanyDataFlowTest {
    
    private CompanyRepository repository;
    private CompanyService service;
    
    @BeforeAll
    void setUpAll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("회사 데이터 플로우 통합 테스트 시작");
        System.out.println("=".repeat(60) + "\n");
    }
    
    @BeforeEach
    void setUp() {
        System.out.println("\n--- 테스트 케이스 시작 ---");
        try {
            repository = new CompanyRepository();
            service = new CompanyService();
            System.out.println("✓ Repository 및 Service 초기화 완료");
        } catch (Exception e) {
            System.err.println("✗ 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            fail("초기화 실패", e);
        }
    }
    
    @AfterEach
    void tearDown() {
        System.out.println("--- 테스트 케이스 종료 ---\n");
    }
    
    @Test
    @DisplayName("1. 도메인 레이어: Repository 직접 저장/조회 테스트")
    void testRepositoryLayer() {
        System.out.println("\n[1단계] 도메인 레이어 테스트");
        System.out.println("  → Repository 직접 저장/조회");
        
        try {
            // 저장
            Company company = new Company();
            company.setName("Repository 테스트 회사");
            company.setProjectName("Repository 테스트 프로젝트");
            company.setContractType("파견");
            company.setStartDate(LocalDate.of(2024, 1, 1));
            company.setEndDate(LocalDate.of(2024, 12, 31));
            company.setStatus("ACTIVE");
            company.setNotes("Repository 레이어 테스트");
            
            System.out.println("    저장 시작: " + company.getName());
            Company saved = repository.save(company);
            System.out.println("    저장 완료: " + saved.getName() + " (ID: " + saved.getId() + ")");
            
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("Repository 테스트 회사", saved.getName());
            
            // 조회
            System.out.println("    조회 시작");
            List<Company> all = repository.findAll();
            System.out.println("    조회 완료: " + all.size() + "개");
            
            assertFalse(all.isEmpty(), "회사 목록이 비어있습니다!");
            assertTrue(all.stream().anyMatch(c -> c.getName().equals("Repository 테스트 회사")), 
                      "저장한 회사를 찾을 수 없습니다!");
            
            // 활성 회사 조회
            List<Company> active = repository.findAllActive();
            System.out.println("    활성 회사 조회: " + active.size() + "개");
            
            assertFalse(active.isEmpty(), "활성 회사 목록이 비어있습니다!");
            
            System.out.println("  ✓ 도메인 레이어 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 도메인 레이어 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("도메인 레이어 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("2. 애플리케이션 레이어: Service 저장/조회 테스트")
    void testServiceLayer() {
        System.out.println("\n[2단계] 애플리케이션 레이어 테스트");
        System.out.println("  → Service를 통한 저장/조회");
        
        try {
            // 저장
            System.out.println("    회사 생성 시작");
            Company company = service.createCompany(
                "Service 테스트 회사",
                "Service 테스트 프로젝트",
                "용역",
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 11, 30),
                "Service 레이어 테스트"
            );
            System.out.println("    회사 생성 완료: " + company.getName() + " (ID: " + company.getId() + ")");
            
            assertNotNull(company);
            assertNotNull(company.getId());
            assertEquals("Service 테스트 회사", company.getName());
            
            // 조회
            System.out.println("    전체 회사 조회 시작");
            List<Company> all = service.getAllCompanies();
            System.out.println("    전체 회사 조회 완료: " + all.size() + "개");
            
            assertFalse(all.isEmpty(), "회사 목록이 비어있습니다!");
            assertTrue(all.stream().anyMatch(c -> c.getName().equals("Service 테스트 회사")), 
                      "저장한 회사를 찾을 수 없습니다!");
            
            // 활성 회사 조회
            System.out.println("    활성 회사 조회 시작");
            List<Company> active = service.getActiveCompanies();
            System.out.println("    활성 회사 조회 완료: " + active.size() + "개");
            
            assertFalse(active.isEmpty(), "활성 회사 목록이 비어있습니다!");
            assertTrue(active.stream().anyMatch(c -> c.getName().equals("Service 테스트 회사")), 
                      "저장한 활성 회사를 찾을 수 없습니다!");
            
            System.out.println("  ✓ 애플리케이션 레이어 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 애플리케이션 레이어 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("애플리케이션 레이어 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("3. 통합 테스트: 여러 Repository 인스턴스 간 데이터 공유 테스트")
    void testMultipleRepositoryInstances() {
        System.out.println("\n[3단계] 통합 테스트");
        System.out.println("  → 여러 Repository 인스턴스 간 데이터 공유");
        
        try {
            // 첫 번째 Repository로 저장
            CompanyRepository repo1 = new CompanyRepository();
            Company company = new Company();
            company.setName("통합 테스트 회사");
            company.setProjectName("통합 테스트 프로젝트");
            company.setContractType("파견");
            company.setStartDate(LocalDate.of(2024, 3, 1));
            company.setEndDate(LocalDate.of(2024, 10, 31));
            company.setStatus("ACTIVE");
            
            System.out.println("    [Repo1] 저장 시작: " + company.getName());
            Company saved = repo1.save(company);
            System.out.println("    [Repo1] 저장 완료: " + saved.getName());
            
            // 두 번째 Repository로 조회 (다른 인스턴스)
            CompanyRepository repo2 = new CompanyRepository();
            System.out.println("    [Repo2] 조회 시작");
            List<Company> all = repo2.findAll();
            System.out.println("    [Repo2] 조회 완료: " + all.size() + "개");
            
            // 세 번째 Service로 조회
            CompanyService service2 = new CompanyService();
            System.out.println("    [Service2] 조회 시작");
            List<Company> active = service2.getActiveCompanies();
            System.out.println("    [Service2] 조회 완료: " + active.size() + "개");
            
            // 검증
            assertFalse(all.isEmpty(), "Repo2에서 데이터를 찾을 수 없습니다!");
            assertTrue(all.stream().anyMatch(c -> c.getName().equals("통합 테스트 회사")), 
                      "Repo2에서 저장한 회사를 찾을 수 없습니다!");
            
            assertFalse(active.isEmpty(), "Service2에서 데이터를 찾을 수 없습니다!");
            assertTrue(active.stream().anyMatch(c -> c.getName().equals("통합 테스트 회사")), 
                      "Service2에서 저장한 회사를 찾을 수 없습니다!");
            
            System.out.println("  ✓ 통합 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 통합 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("통합 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("4. 샘플 데이터 생성 시뮬레이션 테스트")
    void testSampleDataSimulation() {
        System.out.println("\n[4단계] 샘플 데이터 생성 시뮬레이션");
        System.out.println("  → 실제 샘플 데이터 초기화와 동일한 방식");
        
        try {
            CompanyService testService = new CompanyService();
            
            // 샘플 데이터 생성 (SampleDataInitializer와 동일한 방식)
            String[] companyNames = {"LG전자", "SK텔레콤", "삼성SDS"};
            String[] projects = {
                "스마트홈 플랫폼 구축",
                "5G 기지국 관리 시스템",
                "ERP 시스템 고도화"
            };
            
            for (int i = 0; i < companyNames.length; i++) {
                System.out.println("    회사 생성: " + companyNames[i]);
                Company company = testService.createCompany(
                    companyNames[i],
                    projects[i],
                    "파견",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    "테스트용 샘플 데이터"
                );
                System.out.println("      → 생성 완료: " + company.getName() + " (ID: " + company.getId() + ")");
            }
            
            // 조회 확인
            System.out.println("    전체 회사 조회");
            List<Company> all = testService.getAllCompanies();
            System.out.println("      → 조회된 회사 수: " + all.size());
            
            // 활성 회사 조회
            System.out.println("    활성 회사 조회");
            List<Company> active = testService.getActiveCompanies();
            System.out.println("      → 조회된 활성 회사 수: " + active.size());
            
            // 검증
            assertTrue(all.size() >= 3, "최소 3개의 회사가 있어야 합니다. 실제: " + all.size());
            assertTrue(active.size() >= 3, "최소 3개의 활성 회사가 있어야 합니다. 실제: " + active.size());
            
            for (String name : companyNames) {
                assertTrue(all.stream().anyMatch(c -> c.getName().equals(name)), 
                          name + "를 찾을 수 없습니다!");
                assertTrue(active.stream().anyMatch(c -> c.getName().equals(name)), 
                          name + "를 활성 회사 목록에서 찾을 수 없습니다!");
            }
            
            System.out.println("  ✓ 샘플 데이터 생성 시뮬레이션 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 샘플 데이터 생성 시뮬레이션 실패: " + e.getMessage());
            e.printStackTrace();
            fail("샘플 데이터 생성 시뮬레이션 실패", e);
        }
    }
    
    @AfterAll
    void tearDownAll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("회사 데이터 플로우 통합 테스트 완료");
        System.out.println("=".repeat(60) + "\n");
    }
}

