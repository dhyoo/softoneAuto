package com.softone.auto.integration;

import com.softone.auto.model.*;
import com.softone.auto.repository.sqlite.*;
import com.softone.auto.service.*;
import com.softone.auto.util.AppContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DDD 기반 전체 CRUD 통합 테스트
 * 
 * 테스트 구조:
 * 1. Domain Layer: 엔티티 생성 및 검증
 * 2. Repository Layer: 데이터 영속성 테스트
 * 3. Service Layer: 비즈니스 로직 테스트
 * 4. Integration: 전체 플로우 테스트
 */
@DisplayName("DDD 기반 전체 CRUD 통합 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DddCrudIntegrationTest {
    
    // Repository Layer (도메인 레이어 직접 접근)
    private CompanySqliteRepository companyRepository;
    private DeveloperSqliteRepository developerRepository;
    private AttendanceSqliteRepository attendanceRepository;
    private WeeklyReportSqliteRepository weeklyReportRepository;
    private IssueSqliteRepository issueRepository;
    private CustomerCommunicationSqliteRepository communicationRepository;
    private CommonCodeSqliteRepository commonCodeRepository;
    
    // Service Layer (애플리케이션 레이어)
    private CompanyService companyService;
    private DeveloperService developerService;
    private AttendanceService attendanceService;
    private WeeklyReportService weeklyReportService;
    private IssueService issueService;
    private CustomerCommunicationService communicationService;
    private CommonCodeService commonCodeService;
    
    // 테스트 데이터
    private Company testCompany;
    private Developer testDeveloper;
    
    @BeforeAll
    void setUpAll() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DDD 기반 전체 CRUD 통합 테스트 시작");
        System.out.println("=".repeat(80) + "\n");
    }
    
    @BeforeEach
    void setUp() {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("테스트 케이스 초기화");
        System.out.println("-".repeat(80));
        
        try {
            // Repository Layer 초기화
            companyRepository = new CompanySqliteRepository();
            developerRepository = new DeveloperSqliteRepository();
            attendanceRepository = new AttendanceSqliteRepository();
            weeklyReportRepository = new WeeklyReportSqliteRepository();
            issueRepository = new IssueSqliteRepository();
            communicationRepository = new CustomerCommunicationSqliteRepository();
            commonCodeRepository = new CommonCodeSqliteRepository();
            
            // Service Layer 초기화
            companyService = new CompanyService();
            developerService = new DeveloperService();
            attendanceService = new AttendanceService();
            weeklyReportService = new WeeklyReportService();
            issueService = new IssueService();
            communicationService = new CustomerCommunicationService();
            commonCodeService = new CommonCodeService();
            
            // 테스트용 회사 생성 (Repository 직접 사용)
            List<Company> companies = companyRepository.findAll();
            if (companies.isEmpty()) {
                System.out.println("  → 테스트용 회사 생성 (Repository Layer)");
                testCompany = new Company();
                testCompany.setId(UUID.randomUUID().toString());
                testCompany.setName("DDD 테스트 회사");
                testCompany.setProjectName("DDD 테스트 프로젝트");
                testCompany.setContractType("파견");
                testCompany.setStartDate(LocalDate.of(2024, 1, 1));
                testCompany.setEndDate(LocalDate.of(2024, 12, 31));
                testCompany.setStatus("ACTIVE");
                companyRepository.save(testCompany);
            } else {
                testCompany = companies.get(0);
            }
            
            // 테스트용 개발자 생성 (Repository 직접 사용)
            List<Developer> developers = developerRepository.findAll();
            if (developers.isEmpty() || developers.stream().noneMatch(d -> d.getCompanyId().equals(testCompany.getId()))) {
                System.out.println("  → 테스트용 개발자 생성 (Repository Layer)");
                testDeveloper = new Developer();
                testDeveloper.setId(UUID.randomUUID().toString());
                testDeveloper.setCompanyId(testCompany.getId());
                testDeveloper.setName("DDD 테스트 개발자");
                testDeveloper.setPosition("수석");
                testDeveloper.setRole("백엔드 개발");
                testDeveloper.setTeam("테스트팀");
                testDeveloper.setEmail("test@test.com");
                testDeveloper.setPhone("010-0000-0000");
                testDeveloper.setStatus("ACTIVE");
                developerRepository.save(testDeveloper);
            } else {
                testDeveloper = developers.stream()
                    .filter(d -> d.getCompanyId().equals(testCompany.getId()))
                    .findFirst()
                    .orElse(null);
            }
            
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            System.out.println("  ✓ 초기화 완료");
            System.out.println("    회사: " + testCompany.getName() + " (ID: " + testCompany.getId() + ")");
            System.out.println("    개발자: " + (testDeveloper != null ? testDeveloper.getName() : "없음"));
            
        } catch (Exception e) {
            System.err.println("  ✗ 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            fail("초기화 실패", e);
        }
    }
    
    @AfterEach
    void tearDown() {
        System.out.println("-".repeat(80) + "\n");
    }
    
    // ==================== 1. 회사 (Company) DDD 테스트 ====================
    
    @Test
    @DisplayName("1-1. Domain Layer: Company 엔티티 생성 및 검증")
    void testCompanyDomainLayer() {
        System.out.println("\n[1-1] Domain Layer: Company 엔티티 생성 및 검증");
        
        // Domain: 엔티티 생성
        Company company = new Company();
        company.setId(UUID.randomUUID().toString());
        company.setName("도메인 테스트 회사");
        company.setProjectName("도메인 테스트 프로젝트");
        company.setContractType("용역");
        company.setStartDate(LocalDate.of(2024, 6, 1));
        company.setEndDate(LocalDate.of(2024, 12, 31));
        company.setStatus("ACTIVE");
        
        // Domain: 엔티티 검증
        assertNotNull(company.getId(), "ID는 필수입니다");
        assertNotNull(company.getName(), "회사명은 필수입니다");
        assertTrue(company.getStartDate().isBefore(company.getEndDate()), "시작일은 종료일보다 이전이어야 합니다");
        
        System.out.println("  ✓ Domain Layer 검증 완료");
        System.out.println("    ID: " + company.getId());
        System.out.println("    이름: " + company.getName());
    }
    
    @Test
    @DisplayName("1-2. Repository Layer: Company 데이터 영속성")
    void testCompanyRepositoryLayer() {
        System.out.println("\n[1-2] Repository Layer: Company 데이터 영속성");
        
        try {
            // Repository: 저장
            Company company = new Company();
            company.setId(UUID.randomUUID().toString());
            company.setName("Repository 테스트 회사 " + System.currentTimeMillis());
            company.setProjectName("Repository 테스트 프로젝트");
            company.setContractType("파견");
            company.setStartDate(LocalDate.of(2024, 7, 1));
            company.setEndDate(LocalDate.of(2024, 12, 31));
            company.setStatus("ACTIVE");
            
            companyRepository.save(company);
            System.out.println("  ✓ Repository.save() 완료");
            
            // Repository: 조회
            Company found = companyRepository.findById(company.getId()).orElse(null);
            assertNotNull(found, "저장된 회사를 찾을 수 있어야 합니다");
            assertEquals(company.getName(), found.getName());
            System.out.println("  ✓ Repository.findById() 완료");
            
            // Repository: 수정
            found.setProjectName("수정된 프로젝트명");
            companyRepository.save(found); // update는 save로 처리
            Company updated = companyRepository.findById(found.getId()).orElse(null);
            assertEquals("수정된 프로젝트명", updated.getProjectName());
            System.out.println("  ✓ Repository.update() 완료");
            
            // Repository: 삭제
            companyRepository.deleteById(found.getId());
            Company deleted = companyRepository.findById(found.getId()).orElse(null);
            assertNull(deleted, "삭제된 회사는 조회되지 않아야 합니다");
            System.out.println("  ✓ Repository.deleteById() 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("1-3. Service Layer: Company 비즈니스 로직")
    void testCompanyServiceLayer() {
        System.out.println("\n[1-3] Service Layer: Company 비즈니스 로직");
        
        try {
            // Service: 생성
            Company company = companyService.createCompany(
                "Service 테스트 회사 " + System.currentTimeMillis(),
                "Service 테스트 프로젝트",
                "용역",
                LocalDate.of(2024, 8, 1),
                LocalDate.of(2024, 12, 31),
                "Service 테스트"
            );
            assertNotNull(company);
            System.out.println("  ✓ Service.createCompany() 완료");
            
            // Service: 조회
            Company found = companyService.getCompanyById(company.getId());
            assertNotNull(found);
            System.out.println("  ✓ Service.getCompanyById() 완료");
            
            // Service: 수정
            found.setProjectName("Service 수정된 프로젝트명");
            companyService.updateCompany(found);
            Company updated = companyService.getCompanyById(found.getId());
            assertEquals("Service 수정된 프로젝트명", updated.getProjectName());
            System.out.println("  ✓ Service.updateCompany() 완료");
            
            // Service: 삭제
            companyService.deleteCompany(company.getId());
            Company deleted = companyService.getCompanyById(company.getId());
            assertNull(deleted);
            System.out.println("  ✓ Service.deleteCompany() 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 2. 개발자 (Developer) DDD 테스트 ====================
    
    @Test
    @DisplayName("2-1. Domain Layer: Developer 엔티티 생성 및 검증")
    void testDeveloperDomainLayer() {
        System.out.println("\n[2-1] Domain Layer: Developer 엔티티 생성 및 검증");
        
        Developer developer = new Developer();
        developer.setId(UUID.randomUUID().toString());
        developer.setCompanyId(testCompany.getId());
        developer.setName("도메인 테스트 개발자");
        developer.setPosition("선임");
        developer.setRole("프론트엔드 개발");
        developer.setStatus("ACTIVE");
        
        assertNotNull(developer.getId());
        assertNotNull(developer.getCompanyId(), "회사 ID는 필수입니다");
        assertNotNull(developer.getName());
        
        System.out.println("  ✓ Domain Layer 검증 완료");
    }
    
    @Test
    @DisplayName("2-2. Repository Layer: Developer 데이터 영속성")
    void testDeveloperRepositoryLayer() {
        System.out.println("\n[2-2] Repository Layer: Developer 데이터 영속성");
        
        try {
            Developer developer = new Developer();
            developer.setId(UUID.randomUUID().toString());
            developer.setCompanyId(testCompany.getId());
            developer.setName("Repository 테스트 개발자 " + System.currentTimeMillis());
            developer.setPosition("책임");
            developer.setRole("풀스택 개발");
            developer.setStatus("ACTIVE");
            
            developerRepository.save(developer);
            Developer found = developerRepository.findById(developer.getId()).orElse(null);
            assertNotNull(found);
            
            found.setPosition("수석");
            developerRepository.update(found);
            Developer updated = developerRepository.findById(found.getId()).orElse(null);
            assertEquals("수석", updated.getPosition());
            
            developerRepository.delete(found.getId());
            Developer deleted = developerRepository.findById(found.getId()).orElse(null);
            assertNull(deleted);
            
            System.out.println("  ✓ Repository Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("2-3. Service Layer: Developer 비즈니스 로직")
    void testDeveloperServiceLayer() {
        System.out.println("\n[2-3] Service Layer: Developer 비즈니스 로직");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            Developer developer = developerService.createDeveloper(
                "Service 테스트 개발자 " + System.currentTimeMillis(),
                "선임",
                "백엔드 개발",
                "개발팀",
                "dev@test.com",
                "010-1111-2222",
                "010-1111-2223",
                LocalDate.of(2024, 3, 1),
                "Service 테스트"
            );
            assertNotNull(developer);
            
            Developer found = developerService.getDeveloperById(developer.getId());
            assertNotNull(found);
            
            found.setPosition("책임");
            developerService.updateDeveloper(found);
            Developer updated = developerService.getDeveloperById(found.getId());
            assertEquals("책임", updated.getPosition());
            
            developerService.deleteDeveloper(developer.getId());
            Developer deleted = developerService.getDeveloperById(developer.getId());
            assertNull(deleted);
            
            System.out.println("  ✓ Service Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 3. 근태 (Attendance) DDD 테스트 ====================
    
    @Test
    @DisplayName("3-1. Domain Layer: Attendance 엔티티 생성 및 검증")
    void testAttendanceDomainLayer() {
        System.out.println("\n[3-1] Domain Layer: Attendance 엔티티 생성 및 검증");
        
        Attendance attendance = new Attendance();
        attendance.setId(UUID.randomUUID().toString());
        attendance.setCompanyId(testCompany.getId());
        attendance.setDeveloperId(testDeveloper.getId());
        attendance.setDate(LocalDate.now());
        attendance.setCheckIn(LocalTime.of(9, 0));
        attendance.setCheckOut(LocalTime.of(18, 0));
        attendance.setType("정상");
        
        assertNotNull(attendance.getId());
        assertNotNull(attendance.getDeveloperId());
        assertNotNull(attendance.getDate());
        assertTrue(attendance.getCheckIn().isBefore(attendance.getCheckOut()), "출근 시간은 퇴근 시간보다 이전이어야 합니다");
        
        System.out.println("  ✓ Domain Layer 검증 완료");
    }
    
    @Test
    @DisplayName("3-2. Repository Layer: Attendance 데이터 영속성")
    void testAttendanceRepositoryLayer() {
        System.out.println("\n[3-2] Repository Layer: Attendance 데이터 영속성");
        
        try {
            Attendance attendance = new Attendance();
            attendance.setId(UUID.randomUUID().toString());
            attendance.setCompanyId(testCompany.getId());
            attendance.setDeveloperId(testDeveloper.getId());
            attendance.setDeveloperName(testDeveloper.getName());
            attendance.setDate(LocalDate.now());
            attendance.setCheckIn(LocalTime.of(9, 0));
            attendance.setCheckOut(LocalTime.of(18, 0));
            attendance.setType("정상");
            
            attendanceRepository.save(attendance);
            List<Attendance> found = attendanceRepository.findByDeveloperId(testDeveloper.getId());
            assertFalse(found.isEmpty());
            
            attendance.setType("지각");
            attendanceRepository.update(attendance);
            List<Attendance> updated = attendanceRepository.findByDeveloperId(testDeveloper.getId());
            assertTrue(updated.stream().anyMatch(a -> a.getType().equals("지각")));
            
            attendanceRepository.delete(attendance.getId());
            List<Attendance> afterDelete = attendanceRepository.findByDeveloperId(testDeveloper.getId());
            assertFalse(afterDelete.stream().anyMatch(a -> a.getId().equals(attendance.getId())));
            
            System.out.println("  ✓ Repository Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("3-3. Service Layer: Attendance 비즈니스 로직")
    void testAttendanceServiceLayer() {
        System.out.println("\n[3-3] Service Layer: Attendance 비즈니스 로직");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            Attendance attendance = attendanceService.createAttendance(
                testDeveloper.getId(),
                testDeveloper.getName(),
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "정상",
                "Service 테스트"
            );
            assertNotNull(attendance);
            
            List<Attendance> attendances = attendanceService.getAttendanceByDeveloper(testDeveloper.getId());
            assertFalse(attendances.isEmpty());
            
            attendance.setType("지각");
            attendanceService.updateAttendance(attendance);
            
            attendanceService.deleteAttendance(attendance.getId());
            
            System.out.println("  ✓ Service Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 4. 주간보고서 (WeeklyReport) DDD 테스트 ====================
    
    @Test
    @DisplayName("4-1. Domain Layer: WeeklyReport 엔티티 생성 및 검증")
    void testWeeklyReportDomainLayer() {
        System.out.println("\n[4-1] Domain Layer: WeeklyReport 엔티티 생성 및 검증");
        
        WeeklyReport report = new WeeklyReport();
        report.setId(UUID.randomUUID().toString());
        report.setCompanyId(testCompany.getId());
        report.setTitle("도메인 테스트 보고서");
        report.setStartDate(LocalDate.now().minusDays(7));
        report.setEndDate(LocalDate.now().minusDays(3));
        report.setProjectName(testCompany.getProjectName());
        report.setReporter("테스터");
        report.setCreatedDate(LocalDate.now());
        
        assertNotNull(report.getId());
        assertNotNull(report.getCompanyId());
        assertTrue(report.getStartDate().isBefore(report.getEndDate()) || report.getStartDate().equals(report.getEndDate()));
        
        System.out.println("  ✓ Domain Layer 검증 완료");
    }
    
    @Test
    @DisplayName("4-2. Repository Layer: WeeklyReport 데이터 영속성")
    void testWeeklyReportRepositoryLayer() {
        System.out.println("\n[4-2] Repository Layer: WeeklyReport 데이터 영속성");
        
        try {
            WeeklyReport report = new WeeklyReport();
            report.setId(UUID.randomUUID().toString());
            report.setCompanyId(testCompany.getId());
            report.setTitle("Repository 테스트 보고서");
            report.setStartDate(LocalDate.now().minusDays(7));
            report.setEndDate(LocalDate.now().minusDays(3));
            report.setProjectName(testCompany.getProjectName());
            report.setReporter("테스터");
            report.setCreatedDate(LocalDate.now());
            
            weeklyReportRepository.save(report);
            WeeklyReport found = weeklyReportRepository.findById(report.getId()).orElse(null);
            assertNotNull(found);
            
            found.setTitle("수정된 보고서 제목");
            weeklyReportRepository.update(found);
            WeeklyReport updated = weeklyReportRepository.findById(found.getId()).orElse(null);
            assertEquals("수정된 보고서 제목", updated.getTitle());
            
            weeklyReportRepository.delete(report.getId());
            WeeklyReport deleted = weeklyReportRepository.findById(report.getId()).orElse(null);
            assertNull(deleted);
            
            System.out.println("  ✓ Repository Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("4-3. Service Layer: WeeklyReport 비즈니스 로직")
    void testWeeklyReportServiceLayer() {
        System.out.println("\n[4-3] Service Layer: WeeklyReport 비즈니스 로직");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            WeeklyReport report = weeklyReportService.createReport(
                "Service 테스트 보고서",
                LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(3),
                testCompany.getProjectName(),
                "테스터"
            );
            assertNotNull(report);
            
            List<WeeklyReport> reports = weeklyReportService.getAllReports();
            assertFalse(reports.isEmpty());
            
            report.setTitle("Service 수정된 보고서 제목");
            weeklyReportService.updateReport(report);
            
            weeklyReportService.deleteReport(report.getId());
            
            System.out.println("  ✓ Service Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 5. 이슈 (Issue) DDD 테스트 ====================
    
    @Test
    @DisplayName("5-1. Domain Layer: Issue 엔티티 생성 및 검증")
    void testIssueDomainLayer() {
        System.out.println("\n[5-1] Domain Layer: Issue 엔티티 생성 및 검증");
        
        Issue issue = new Issue();
        issue.setId(UUID.randomUUID().toString());
        issue.setCompanyId(testCompany.getId());
        issue.setTitle("도메인 테스트 이슈");
        issue.setDescription("테스트 설명");
        issue.setCategory("버그");
        issue.setSeverity("높음");
        issue.setStatus("OPEN");
        issue.setCreatedDate(LocalDateTime.now());
        
        assertNotNull(issue.getId());
        assertNotNull(issue.getCompanyId());
        assertNotNull(issue.getTitle());
        
        System.out.println("  ✓ Domain Layer 검증 완료");
    }
    
    @Test
    @DisplayName("5-2. Repository Layer: Issue 데이터 영속성")
    void testIssueRepositoryLayer() {
        System.out.println("\n[5-2] Repository Layer: Issue 데이터 영속성");
        
        try {
            Issue issue = new Issue();
            issue.setId(UUID.randomUUID().toString());
            issue.setCompanyId(testCompany.getId());
            issue.setTitle("Repository 테스트 이슈");
            issue.setDescription("테스트 설명");
            issue.setCategory("버그");
            issue.setSeverity("높음");
            issue.setStatus("OPEN");
            issue.setCreatedDate(LocalDateTime.now());
            
            issueRepository.save(issue);
            Issue found = issueRepository.findById(issue.getId()).orElse(null);
            assertNotNull(found);
            
            found.setStatus("RESOLVED");
            issueRepository.update(found);
            Issue updated = issueRepository.findById(found.getId()).orElse(null);
            assertEquals("RESOLVED", updated.getStatus());
            
            issueRepository.delete(found.getId());
            Issue deleted = issueRepository.findById(found.getId()).orElse(null);
            assertNull(deleted);
            
            System.out.println("  ✓ Repository Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("5-3. Service Layer: Issue 비즈니스 로직")
    void testIssueServiceLayer() {
        System.out.println("\n[5-3] Service Layer: Issue 비즈니스 로직");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            Issue issue = issueService.createIssue(
                "Service 테스트 이슈",
                "테스트 설명",
                "버그",
                "높음",
                "테스터",
                testDeveloper.getId(),
                "Service 테스트"
            );
            assertNotNull(issue);
            
            List<Issue> issues = issueService.getAllIssues();
            assertFalse(issues.isEmpty());
            
            issue.setStatus("RESOLVED");
            issueService.updateIssue(issue);
            
            issueService.deleteIssue(issue.getId());
            
            System.out.println("  ✓ Service Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 6. 고객소통 (CustomerCommunication) DDD 테스트 ====================
    
    @Test
    @DisplayName("6-1. Domain Layer: CustomerCommunication 엔티티 생성 및 검증")
    void testCustomerCommunicationDomainLayer() {
        System.out.println("\n[6-1] Domain Layer: CustomerCommunication 엔티티 생성 및 검증");
        
        CustomerCommunication comm = new CustomerCommunication();
        comm.setId(UUID.randomUUID().toString());
        comm.setCompanyId(testCompany.getId());
        comm.setType("회의");
        comm.setTitle("도메인 테스트 소통");
        comm.setContent("테스트 내용");
        comm.setCommunicationDate(LocalDateTime.now());
        comm.setStatus("PENDING");
        
        assertNotNull(comm.getId());
        assertNotNull(comm.getCompanyId());
        assertNotNull(comm.getType());
        
        System.out.println("  ✓ Domain Layer 검증 완료");
    }
    
    @Test
    @DisplayName("6-2. Repository Layer: CustomerCommunication 데이터 영속성")
    void testCustomerCommunicationRepositoryLayer() {
        System.out.println("\n[6-2] Repository Layer: CustomerCommunication 데이터 영속성");
        
        try {
            CustomerCommunication comm = new CustomerCommunication();
            comm.setId(UUID.randomUUID().toString());
            comm.setCompanyId(testCompany.getId());
            comm.setType("회의");
            comm.setTitle("Repository 테스트 소통");
            comm.setContent("테스트 내용");
            comm.setCommunicationDate(LocalDateTime.now());
            comm.setStatus("PENDING");
            
            communicationRepository.save(comm);
            CustomerCommunication found = communicationRepository.findById(comm.getId()).orElse(null);
            assertNotNull(found);
            
            found.setStatus("COMPLETED");
            communicationRepository.update(found);
            CustomerCommunication updated = communicationRepository.findById(found.getId()).orElse(null);
            assertEquals("COMPLETED", updated.getStatus());
            
            communicationRepository.delete(found.getId());
            CustomerCommunication deleted = communicationRepository.findById(found.getId()).orElse(null);
            assertNull(deleted);
            
            System.out.println("  ✓ Repository Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("6-3. Service Layer: CustomerCommunication 비즈니스 로직")
    void testCustomerCommunicationServiceLayer() {
        System.out.println("\n[6-3] Service Layer: CustomerCommunication 비즈니스 로직");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            CustomerCommunication comm = communicationService.createCommunication(
                "회의",
                "Service 테스트 소통",
                "테스트 내용",
                "고객 담당자",
                "우리 담당자",
                LocalDateTime.now(),
                "높음",
                LocalDateTime.now().plusDays(7),
                "Service 테스트"
            );
            assertNotNull(comm);
            
            List<CustomerCommunication> communications = communicationService.getAllCommunications();
            assertFalse(communications.isEmpty());
            
            comm.setStatus("COMPLETED");
            communicationService.updateCommunication(comm);
            
            communicationService.deleteCommunication(comm.getId());
            
            System.out.println("  ✓ Service Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 7. 공통코드 (CommonCode) DDD 테스트 ====================
    
    @Test
    @DisplayName("7-1. Domain Layer: CommonCode 엔티티 생성 및 검증")
    void testCommonCodeDomainLayer() {
        System.out.println("\n[7-1] Domain Layer: CommonCode 엔티티 생성 및 검증");
        
        CommonCode code = new CommonCode();
        code.setId(UUID.randomUUID().toString());
        code.setCategory("TEST_TYPE");
        code.setCode("TEST_CODE");
        code.setName("도메인 테스트 코드");
        code.setIsActive(true);
        
        assertNotNull(code.getId());
        assertNotNull(code.getCategory());
        assertNotNull(code.getCode());
        
        System.out.println("  ✓ Domain Layer 검증 완료");
    }
    
    @Test
    @DisplayName("7-2. Repository Layer: CommonCode 데이터 영속성")
    void testCommonCodeRepositoryLayer() {
        System.out.println("\n[7-2] Repository Layer: CommonCode 데이터 영속성");
        
        try {
            CommonCode code = new CommonCode();
            code.setId(UUID.randomUUID().toString());
            code.setCategory("TEST_TYPE");
            code.setCode("TEST_CODE_" + System.currentTimeMillis());
            code.setName("Repository 테스트 코드");
            code.setIsActive(true);
            
            commonCodeRepository.save(code);
            CommonCode found = commonCodeRepository.findById(code.getId());
            assertNotNull(found);
            
            found.setName("수정된 코드명");
            commonCodeRepository.update(found);
            CommonCode updated = commonCodeRepository.findById(found.getId());
            assertEquals("수정된 코드명", updated.getName());
            
            commonCodeRepository.delete(found.getId());
            CommonCode deleted = commonCodeRepository.findById(found.getId());
            assertNull(deleted);
            
            System.out.println("  ✓ Repository Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Repository Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Repository Layer 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("7-3. Service Layer: CommonCode 비즈니스 로직")
    void testCommonCodeServiceLayer() {
        System.out.println("\n[7-3] Service Layer: CommonCode 비즈니스 로직");
        
        try {
            String codeValue = "TEST_CODE_" + System.currentTimeMillis();
            CommonCode code = commonCodeService.createCode(
                "TEST_TYPE",
                codeValue,
                "Service 테스트 코드",
                "테스트용 코드",
                1
            );
            assertNotNull(code);
            
            List<CommonCode> codes = commonCodeService.getAllCodes();
            assertFalse(codes.isEmpty());
            
            code.setName("Service 수정된 코드명");
            commonCodeService.updateCode(code);
            
            commonCodeService.deleteCode(code.getId());
            
            System.out.println("  ✓ Service Layer CRUD 완료");
            
        } catch (Exception e) {
            System.err.println("  ✗ Service Layer 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("Service Layer 테스트 실패", e);
        }
    }
    
    // ==================== 8. 통합 테스트 (Integration) ====================
    
    @Test
    @DisplayName("8. Integration: 전체 플로우 테스트")
    void testIntegrationFlow() {
        System.out.println("\n[8] Integration: 전체 플로우 테스트");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            // 1. 회사 생성 (Service)
            Company company = companyService.createCompany(
                "통합 테스트 회사",
                "통합 테스트 프로젝트",
                "파견",
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 12, 31),
                "통합 테스트"
            );
            AppContext.getInstance().setCurrentCompany(company);
            
            // 2. 개발자 생성 (Service) - AppContext에 회사가 설정되어 있어야 함
            AppContext.getInstance().setCurrentCompany(company);
            Developer developer = developerService.createDeveloper(
                "통합 테스트 개발자",
                "수석",
                "풀스택 개발",
                "개발팀",
                "integration@test.com",
                "010-9999-8888",
                "010-9999-8889",
                LocalDate.of(2024, 9, 1),
                "통합 테스트"
            );
            
            // 3. 근태 생성 (Service)
            Attendance attendance = attendanceService.createAttendance(
                developer.getId(),
                developer.getName(),
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "정상",
                "통합 테스트"
            );
            
            // 4. 주간보고서 생성 (Service)
            WeeklyReport report = weeklyReportService.createReport(
                "통합 테스트 보고서",
                LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(3),
                company.getProjectName(),
                "테스터"
            );
            
            // 5. 이슈 생성 (Service)
            Issue issue = issueService.createIssue(
                "통합 테스트 이슈",
                "테스트 설명",
                "버그",
                "높음",
                "테스터",
                developer.getId(),
                "통합 테스트"
            );
            
            // 6. 고객소통 생성 (Service)
            CustomerCommunication comm = communicationService.createCommunication(
                "회의",
                "통합 테스트 소통",
                "테스트 내용",
                "고객 담당자",
                "우리 담당자",
                LocalDateTime.now(),
                "높음",
                LocalDateTime.now().plusDays(7),
                "통합 테스트"
            );
            
            // 7. 공통코드 생성 (Service)
            CommonCode code = commonCodeService.createCode(
                "INTEGRATION_TYPE",
                "INTEGRATION_CODE_" + System.currentTimeMillis(),
                "통합 테스트 코드",
                "통합 테스트용",
                1
            );
            
            // 8. Repository를 통한 검증
            assertNotNull(companyRepository.findById(company.getId()).orElse(null));
            assertNotNull(developerRepository.findById(developer.getId()).orElse(null));
            assertFalse(attendanceRepository.findByDeveloperId(developer.getId()).isEmpty());
            assertNotNull(weeklyReportRepository.findById(report.getId()).orElse(null));
            assertNotNull(issueRepository.findById(issue.getId()).orElse(null));
            assertNotNull(communicationRepository.findById(comm.getId()).orElse(null));
            assertNotNull(commonCodeRepository.findById(code.getId()));
            
            System.out.println("  ✓ 전체 플로우 테스트 완료");
            System.out.println("    - 회사: " + company.getName());
            System.out.println("    - 개발자: " + developer.getName());
            System.out.println("    - 근태: " + attendance.getId());
            System.out.println("    - 주간보고서: " + report.getTitle());
            System.out.println("    - 이슈: " + issue.getTitle());
            System.out.println("    - 고객소통: " + comm.getType());
            System.out.println("    - 공통코드: " + code.getName());
            
        } catch (Exception e) {
            System.err.println("  ✗ 통합 테스트 실패: " + e.getMessage());
            System.err.println("  예외 타입: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("  원인: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // Foreign Key 제약조건 오류는 예상 가능한 경우이므로 테스트를 계속 진행
            String errorMsg = e.getMessage() != null ? e.getMessage() : "";
            String causeMsg = e.getCause() != null && e.getCause().getMessage() != null ? e.getCause().getMessage() : "";
            if (errorMsg.contains("FOREIGN KEY") || causeMsg.contains("FOREIGN KEY") || 
                errorMsg.contains("foreign key") || causeMsg.contains("foreign key")) {
                System.out.println("  ⚠️ Foreign Key 제약조건 오류 (예상 가능) - 테스트 데이터 정리 필요");
                // 테스트는 실패로 표시하되, 원인을 명확히 함
                fail("통합 테스트 실패: Foreign Key 제약조건 오류 - " + causeMsg, e);
            } else {
                fail("통합 테스트 실패: " + errorMsg, e);
            }
        }
    }
    
    @AfterAll
    void tearDownAll() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DDD 기반 전체 CRUD 통합 테스트 완료");
        System.out.println("=".repeat(80) + "\n");
    }
}

