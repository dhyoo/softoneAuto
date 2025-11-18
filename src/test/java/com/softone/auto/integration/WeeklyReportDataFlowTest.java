package com.softone.auto.integration;

import com.softone.auto.model.Company;
import com.softone.auto.model.WeeklyReport;
import com.softone.auto.repository.sqlite.WeeklyReportSqliteRepository;
import com.softone.auto.service.CompanyService;
import com.softone.auto.service.WeeklyReportService;
import com.softone.auto.util.AppContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 주간보고서 데이터 플로우 통합 테스트 (DDD 방식)
 * 
 * 테스트 시나리오:
 * 1. 도메인 레이어: Repository 직접 테스트
 * 2. 애플리케이션 레이어: Service 테스트
 * 3. 통합 테스트: 전체 플로우 테스트
 */
@DisplayName("주간보고서 데이터 플로우 통합 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeeklyReportDataFlowTest {
    
    private WeeklyReportSqliteRepository repository;
    private WeeklyReportService service;
    private CompanyService companyService;
    private Company testCompany;
    
    @BeforeAll
    void setUpAll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("주간보고서 데이터 플로우 통합 테스트 시작");
        System.out.println("=".repeat(60) + "\n");
    }
    
    @BeforeEach
    void setUp() {
        System.out.println("\n--- 테스트 케이스 시작 ---");
        try {
            repository = new WeeklyReportSqliteRepository();
            service = new WeeklyReportService();
            companyService = new CompanyService();
            
            // 테스트용 회사 생성 또는 조회
            List<Company> companies = companyService.getAllCompanies();
            if (companies.isEmpty()) {
                System.out.println("  → 테스트용 회사 생성 중...");
                testCompany = companyService.createCompany(
                    "테스트 회사",
                    "테스트 프로젝트",
                    "파견",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    "테스트용"
                );
            } else {
                testCompany = companies.get(0);
            }
            
            // AppContext에 회사 설정
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            System.out.println("✓ Repository 및 Service 초기화 완료");
            System.out.println("  테스트 회사: " + testCompany.getName() + " (ID: " + testCompany.getId() + ")");
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
            WeeklyReport report = new WeeklyReport();
            report.setId(java.util.UUID.randomUUID().toString());
            report.setCompanyId(testCompany.getId());
            report.setTitle("Repository 테스트 보고서");
            report.setProjectName("테스트 프로젝트");
            report.setReporter("테스터");
            report.setStartDate(LocalDate.of(2024, 11, 18));
            report.setEndDate(LocalDate.of(2024, 11, 22));
            report.setCreatedDate(LocalDate.now());
            
            System.out.println("    저장 시작: " + report.getTitle());
            repository.save(report);
            System.out.println("    저장 완료: " + report.getTitle() + " (ID: " + report.getId() + ")");
            
            assertNotNull(report.getId());
            assertNotNull(report.getCompanyId());
            
            // 조회
            System.out.println("    조회 시작");
            List<WeeklyReport> all = repository.findAll();
            System.out.println("    조회 완료: " + all.size() + "건");
            
            assertFalse(all.isEmpty(), "보고서 목록이 비어있습니다!");
            assertTrue(all.stream().anyMatch(r -> r.getId().equals(report.getId())), 
                      "저장한 보고서를 찾을 수 없습니다!");
            
            // ID로 조회
            var found = repository.findById(report.getId());
            assertTrue(found.isPresent(), "ID로 보고서를 찾을 수 없습니다!");
            assertEquals(report.getTitle(), found.get().getTitle());
            
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
            System.out.println("    보고서 생성 시작");
            LocalDate startDate = LocalDate.of(2024, 11, 19);
            LocalDate endDate = LocalDate.of(2024, 11, 23);
            
            WeeklyReport report = service.createReport(
                "Service 테스트 보고서",
                startDate,
                endDate,
                "테스트 프로젝트",
                "테스터"
            );
            System.out.println("    보고서 생성 완료: " + report.getTitle() + " (ID: " + report.getId() + ")");
            
            assertNotNull(report);
            assertNotNull(report.getId());
            assertEquals(testCompany.getId(), report.getCompanyId(), "회사 ID가 일치하지 않습니다!");
            assertEquals("Service 테스트 보고서", report.getTitle());
            
            // 조회
            System.out.println("    전체 보고서 조회 시작");
            List<WeeklyReport> all = service.getAllReports();
            System.out.println("    전체 보고서 조회 완료: " + all.size() + "건");
            
            assertFalse(all.isEmpty(), "보고서 목록이 비어있습니다!");
            assertTrue(all.stream().anyMatch(r -> r.getId().equals(report.getId())), 
                      "저장한 보고서를 찾을 수 없습니다!");
            
            // 시작일로 조회
            WeeklyReport found = service.getReportByStartDate(startDate);
            assertNotNull(found, "시작일로 보고서를 찾을 수 없습니다!");
            assertEquals(report.getId(), found.getId());
            
            System.out.println("  ✓ 애플리케이션 레이어 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 애플리케이션 레이어 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("애플리케이션 레이어 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("3. 통합 테스트: 여러 Service 인스턴스 간 데이터 공유 테스트")
    void testMultipleServiceInstances() {
        System.out.println("\n[3단계] 통합 테스트");
        System.out.println("  → 여러 Service 인스턴스 간 데이터 공유");
        
        try {
            // 첫 번째 Service로 저장
            WeeklyReportService service1 = new WeeklyReportService();
            LocalDate startDate = LocalDate.of(2024, 11, 20);
            LocalDate endDate = LocalDate.of(2024, 11, 24);
            
            System.out.println("    [Service1] 저장 시작");
            WeeklyReport report = service1.createReport(
                "통합 테스트 보고서",
                startDate,
                endDate,
                "테스트 프로젝트",
                "테스터"
            );
            System.out.println("    [Service1] 저장 완료: " + report.getTitle());
            
            // 두 번째 Repository로 조회 (다른 인스턴스)
            WeeklyReportSqliteRepository repo2 = new WeeklyReportSqliteRepository();
            System.out.println("    [Repo2] 조회 시작");
            List<WeeklyReport> all = repo2.findAll();
            System.out.println("    [Repo2] 조회 완료: " + all.size() + "건");
            
            // 세 번째 Service로 조회
            WeeklyReportService service3 = new WeeklyReportService();
            System.out.println("    [Service3] 조회 시작");
            List<WeeklyReport> filtered = service3.getAllReports();
            System.out.println("    [Service3] 조회 완료: " + filtered.size() + "건");
            
            // 검증
            assertFalse(all.isEmpty(), "Repo2에서 데이터를 찾을 수 없습니다!");
            assertTrue(all.stream().anyMatch(r -> r.getId().equals(report.getId())), 
                      "Repo2에서 저장한 보고서를 찾을 수 없습니다!");
            
            assertFalse(filtered.isEmpty(), "Service3에서 데이터를 찾을 수 없습니다!");
            assertTrue(filtered.stream().anyMatch(r -> r.getId().equals(report.getId())), 
                      "Service3에서 저장한 보고서를 찾을 수 없습니다!");
            
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
            WeeklyReportService testService = new WeeklyReportService();
            
            // 최근 4주치 보고서 생성 (SampleDataInitializer와 유사한 방식)
            int reportCount = 0;
            for (int weekOffset = 4; weekOffset >= 1; weekOffset--) {
                try {
                    LocalDate weekStart = LocalDate.now().minusWeeks(weekOffset)
                        .minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
                    LocalDate weekEnd = weekStart.plusDays(4);
                    
                    // 이미 존재하는 보고서는 건너뛰기
                    if (testService.existsByStartDate(weekStart)) {
                        System.out.println("    [주차 " + weekOffset + "] 이미 존재: " + weekStart);
                        continue;
                    }
                    
                    System.out.println("    보고서 생성: " + weekStart + " ~ " + weekEnd);
                    WeeklyReport report = testService.createReport(
                        "주간 업무 보고서 - " + weekStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        weekStart,
                        weekEnd,
                        testCompany.getProjectName(),
                        "관리자"
                    );
                    System.out.println("      → 생성 완료: " + report.getTitle() + " (ID: " + report.getId() + ")");
                    reportCount++;
                    
                } catch (Exception e) {
                    System.err.println("    ✗ 주차 " + weekOffset + " 보고서 생성 실패: " + e.getMessage());
                    e.printStackTrace();
                    // 계속 진행
                }
            }
            
            // 조회 확인
            System.out.println("    전체 보고서 조회");
            List<WeeklyReport> all = testService.getAllReports();
            System.out.println("      → 조회된 보고서 수: " + all.size());
            
            // 검증
            assertTrue(all.size() >= reportCount, 
                      "최소 " + reportCount + "개의 보고서가 있어야 합니다. 실제: " + all.size());
            
            // 각 보고서의 회사 ID 확인
            for (WeeklyReport report : all) {
                assertNotNull(report.getCompanyId(), "보고서의 회사 ID가 null입니다!");
                assertEquals(testCompany.getId(), report.getCompanyId(), 
                            "보고서의 회사 ID가 일치하지 않습니다!");
            }
            
            System.out.println("  ✓ 샘플 데이터 생성 시뮬레이션 성공");
            System.out.println("    생성된 보고서: " + reportCount + "건");
            System.out.println("    조회된 보고서: " + all.size() + "건");
            
        } catch (Exception e) {
            System.err.println("  ✗ 샘플 데이터 생성 시뮬레이션 실패: " + e.getMessage());
            e.printStackTrace();
            fail("샘플 데이터 생성 시뮬레이션 실패", e);
        }
    }
    
    @Test
    @DisplayName("5. 회사별 필터링 테스트")
    void testCompanyFiltering() {
        System.out.println("\n[5단계] 회사별 필터링 테스트");
        System.out.println("  → 다른 회사의 보고서는 필터링되는지 확인");
        
        try {
            // 다른 회사 생성
            Company otherCompany = companyService.createCompany(
                "다른 테스트 회사",
                "다른 프로젝트",
                "파견",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "필터링 테스트용"
            );
            
            // 현재 회사로 보고서 생성
            AppContext.getInstance().setCurrentCompany(testCompany);
            WeeklyReportService service1 = new WeeklyReportService();
            WeeklyReport report1 = service1.createReport(
                "현재 회사 보고서",
                LocalDate.of(2024, 11, 25),
                LocalDate.of(2024, 11, 29),
                testCompany.getProjectName(),
                "테스터"
            );
            System.out.println("    현재 회사 보고서 생성: " + report1.getId());
            
            // 다른 회사로 보고서 생성
            AppContext.getInstance().setCurrentCompany(otherCompany);
            WeeklyReportService service2 = new WeeklyReportService();
            WeeklyReport report2 = service2.createReport(
                "다른 회사 보고서",
                LocalDate.of(2024, 11, 25),
                LocalDate.of(2024, 11, 29),
                otherCompany.getProjectName(),
                "테스터"
            );
            System.out.println("    다른 회사 보고서 생성: " + report2.getId());
            
            // 현재 회사로 다시 설정하고 조회
            AppContext.getInstance().setCurrentCompany(testCompany);
            WeeklyReportService service3 = new WeeklyReportService();
            List<WeeklyReport> filtered = service3.getAllReports();
            System.out.println("    필터링된 보고서: " + filtered.size() + "건");
            
            // 검증
            assertTrue(filtered.stream().anyMatch(r -> r.getId().equals(report1.getId())), 
                      "현재 회사의 보고서가 포함되어야 합니다!");
            assertFalse(filtered.stream().anyMatch(r -> r.getId().equals(report2.getId())), 
                       "다른 회사의 보고서는 필터링되어야 합니다!");
            
            System.out.println("  ✓ 회사별 필터링 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 회사별 필터링 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("회사별 필터링 테스트 실패", e);
        }
    }
    
    @AfterAll
    void tearDownAll() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("주간보고서 데이터 플로우 통합 테스트 완료");
        System.out.println("=".repeat(60) + "\n");
    }
}

