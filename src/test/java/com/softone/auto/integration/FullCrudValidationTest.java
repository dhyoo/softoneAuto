package com.softone.auto.integration;

import com.softone.auto.model.*;
import com.softone.auto.repository.*;
import com.softone.auto.service.*;
import com.softone.auto.util.AppContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 전체 탭 CRUD 데이터 검증 통합 테스트
 * 
 * 테스트 대상:
 * 1. 회사 (Company)
 * 2. 개발자 (Developer)
 * 3. 근태 (Attendance)
 * 4. 주간보고서 (WeeklyReport)
 * 5. 이슈 (Issue)
 * 6. 고객소통 (CustomerCommunication)
 * 7. 공통코드 (CommonCode)
 */
@DisplayName("전체 탭 CRUD 데이터 검증 통합 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FullCrudValidationTest {
    
    private CompanyService companyService;
    private DeveloperService developerService;
    private AttendanceService attendanceService;
    private WeeklyReportService weeklyReportService;
    private IssueService issueService;
    private CustomerCommunicationService communicationService;
    private CommonCodeService commonCodeService;
    
    private Company testCompany;
    private Developer testDeveloper;
    
    @BeforeAll
    void setUpAll() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("전체 탭 CRUD 데이터 검증 통합 테스트 시작");
        System.out.println("=".repeat(70) + "\n");
    }
    
    @BeforeEach
    void setUp() {
        System.out.println("\n--- 테스트 케이스 시작 ---");
        try {
            companyService = new CompanyService();
            developerService = new DeveloperService();
            attendanceService = new AttendanceService();
            weeklyReportService = new WeeklyReportService();
            issueService = new IssueService();
            communicationService = new CustomerCommunicationService();
            commonCodeService = new CommonCodeService();
            
            // 테스트용 회사 생성 또는 조회
            List<Company> companies = companyService.getAllCompanies();
            if (companies.isEmpty()) {
                System.out.println("  → 테스트용 회사 생성 중...");
                testCompany = companyService.createCompany(
                    "CRUD 테스트 회사",
                    "CRUD 테스트 프로젝트",
                    "파견",
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31),
                    "CRUD 테스트용"
                );
            } else {
                testCompany = companies.get(0);
            }
            
            // 테스트용 개발자 생성 또는 조회
            List<Developer> developers = developerService.getAllDevelopers();
            if (developers.isEmpty()) {
                System.out.println("  → 테스트용 개발자 생성 중...");
                AppContext.getInstance().setCurrentCompany(testCompany);
                testDeveloper = developerService.createDeveloper(
                    "CRUD 테스트 개발자",
                    "수석",
                    "백엔드 개발",
                    "테스트팀",
                    "test@test.com",
                    "010-0000-0000",
                    "010-0000-0001",
                    LocalDate.of(2024, 1, 1),
                    "CRUD 테스트용"
                );
            } else {
                testDeveloper = developers.get(0);
            }
            
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            System.out.println("✓ 모든 서비스 초기화 완료");
            System.out.println("  테스트 회사: " + testCompany.getName() + " (ID: " + testCompany.getId() + ")");
            System.out.println("  테스트 개발자: " + testDeveloper.getName() + " (ID: " + testDeveloper.getId() + ")");
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
    @DisplayName("1. 회사 (Company) CRUD 테스트")
    void testCompanyCrud() {
        System.out.println("\n[1] 회사 CRUD 테스트");
        
        try {
            // CREATE
            System.out.println("  [CREATE] 회사 생성");
            Company newCompany = companyService.createCompany(
                "CRUD 테스트 회사 " + System.currentTimeMillis(),
                "CRUD 테스트 프로젝트",
                "용역",
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 31),
                "CRUD 테스트"
            );
            assertNotNull(newCompany);
            assertNotNull(newCompany.getId());
            System.out.println("    ✓ 생성 완료: " + newCompany.getName() + " (ID: " + newCompany.getId() + ")");
            
            // READ
            System.out.println("  [READ] 회사 조회");
            Company found = companyService.getCompanyById(newCompany.getId());
            assertNotNull(found);
            assertEquals(newCompany.getName(), found.getName());
            System.out.println("    ✓ 조회 완료: " + found.getName());
            
            // UPDATE
            System.out.println("  [UPDATE] 회사 수정");
            found.setProjectName("수정된 프로젝트명");
            companyService.updateCompany(found);
            Company updated = companyService.getCompanyById(found.getId());
            assertNotNull(updated);
            assertEquals("수정된 프로젝트명", updated.getProjectName());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 회사 삭제");
            companyService.deleteCompany(newCompany.getId());
            Company deleted = companyService.getCompanyById(newCompany.getId());
            assertNull(deleted);
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 회사 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 회사 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("회사 CRUD 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("2. 개발자 (Developer) CRUD 테스트")
    void testDeveloperCrud() {
        System.out.println("\n[2] 개발자 CRUD 테스트");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            // CREATE
            System.out.println("  [CREATE] 개발자 생성");
            Developer newDeveloper = developerService.createDeveloper(
                "CRUD 테스트 개발자 " + System.currentTimeMillis(),
                "선임",
                "프론트엔드 개발",
                "개발팀",
                "dev@test.com",
                "010-1111-2222",
                "010-1111-2223",
                LocalDate.of(2024, 3, 1),
                "CRUD 테스트"
            );
            assertNotNull(newDeveloper);
            assertNotNull(newDeveloper.getId());
            assertEquals(testCompany.getId(), newDeveloper.getCompanyId());
            System.out.println("    ✓ 생성 완료: " + newDeveloper.getName() + " (ID: " + newDeveloper.getId() + ")");
            
            // READ
            System.out.println("  [READ] 개발자 조회");
            Developer found = developerService.getDeveloperById(newDeveloper.getId());
            assertNotNull(found);
            assertEquals(newDeveloper.getName(), found.getName());
            System.out.println("    ✓ 조회 완료: " + found.getName());
            
            // UPDATE
            System.out.println("  [UPDATE] 개발자 수정");
            found.setPosition("책임");
            developerService.updateDeveloper(found);
            Developer updated = developerService.getDeveloperById(found.getId());
            assertNotNull(updated);
            assertEquals("책임", updated.getPosition());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 개발자 삭제");
            developerService.deleteDeveloper(newDeveloper.getId());
            Developer deleted = developerService.getDeveloperById(newDeveloper.getId());
            assertNull(deleted);
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 개발자 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 개발자 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("개발자 CRUD 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("3. 근태 (Attendance) CRUD 테스트")
    void testAttendanceCrud() {
        System.out.println("\n[3] 근태 CRUD 테스트");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            // CREATE
            System.out.println("  [CREATE] 근태 생성");
            LocalDate today = LocalDate.now();
            Attendance newAttendance = attendanceService.createAttendance(
                testDeveloper.getId(),
                testDeveloper.getName(),
                today,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "정상",
                "CRUD 테스트"
            );
            assertNotNull(newAttendance);
            assertNotNull(newAttendance.getId());
            assertEquals(testDeveloper.getId(), newAttendance.getDeveloperId());
            System.out.println("    ✓ 생성 완료: " + newAttendance.getDate() + " (ID: " + newAttendance.getId() + ")");
            
            // READ
            System.out.println("  [READ] 근태 조회");
            List<Attendance> attendances = attendanceService.getAttendanceByDeveloper(testDeveloper.getId());
            assertFalse(attendances.isEmpty());
            assertTrue(attendances.stream().anyMatch(a -> a.getId().equals(newAttendance.getId())));
            System.out.println("    ✓ 조회 완료: " + attendances.size() + "건");
            
            // UPDATE
            System.out.println("  [UPDATE] 근태 수정");
            newAttendance.setType("지각");
            attendanceService.updateAttendance(newAttendance);
            List<Attendance> updatedList = attendanceService.getAttendanceByDeveloper(testDeveloper.getId());
            Attendance updated = updatedList.stream()
                .filter(a -> a.getId().equals(newAttendance.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(updated);
            assertEquals("지각", updated.getType());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 근태 삭제");
            attendanceService.deleteAttendance(newAttendance.getId());
            List<Attendance> afterDelete = attendanceService.getAttendanceByDeveloper(testDeveloper.getId());
            assertFalse(afterDelete.stream().anyMatch(a -> a.getId().equals(newAttendance.getId())));
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 근태 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 근태 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("근태 CRUD 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("4. 주간보고서 (WeeklyReport) CRUD 테스트")
    void testWeeklyReportCrud() {
        System.out.println("\n[4] 주간보고서 CRUD 테스트");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            // CREATE
            System.out.println("  [CREATE] 주간보고서 생성");
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = startDate.plusDays(4);
            
            WeeklyReport newReport = weeklyReportService.createReport(
                "CRUD 테스트 보고서",
                startDate,
                endDate,
                testCompany.getProjectName(),
                "테스터"
            );
            assertNotNull(newReport);
            assertNotNull(newReport.getId());
            assertEquals(testCompany.getId(), newReport.getCompanyId());
            System.out.println("    ✓ 생성 완료: " + newReport.getTitle() + " (ID: " + newReport.getId() + ")");
            
            // READ
            System.out.println("  [READ] 주간보고서 조회");
            List<WeeklyReport> reports = weeklyReportService.getAllReports();
            assertFalse(reports.isEmpty());
            assertTrue(reports.stream().anyMatch(r -> r.getId().equals(newReport.getId())));
            System.out.println("    ✓ 조회 완료: " + reports.size() + "건");
            
            WeeklyReport found = reports.stream()
                .filter(r -> r.getId().equals(newReport.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(found);
            assertEquals(newReport.getTitle(), found.getTitle());
            System.out.println("    ✓ ID로 조회 완료");
            
            // UPDATE
            System.out.println("  [UPDATE] 주간보고서 수정");
            found.setTitle("수정된 보고서 제목");
            weeklyReportService.updateReport(found);
            List<WeeklyReport> updatedList = weeklyReportService.getAllReports();
            WeeklyReport updated = updatedList.stream()
                .filter(r -> r.getId().equals(found.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(updated);
            assertEquals("수정된 보고서 제목", updated.getTitle());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 주간보고서 삭제");
            weeklyReportService.deleteReport(newReport.getId());
            List<WeeklyReport> afterDelete = weeklyReportService.getAllReports();
            assertFalse(afterDelete.stream().anyMatch(r -> r.getId().equals(newReport.getId())));
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 주간보고서 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 주간보고서 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("주간보고서 CRUD 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("5. 이슈 (Issue) CRUD 테스트")
    void testIssueCrud() {
        System.out.println("\n[5] 이슈 CRUD 테스트");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            // CREATE
            System.out.println("  [CREATE] 이슈 생성");
            Issue newIssue = issueService.createIssue(
                "CRUD 테스트 이슈",
                "테스트 이슈 설명",
                "버그",
                "높음",
                "테스터",
                testDeveloper.getId(),
                "CRUD 테스트"
            );
            assertNotNull(newIssue);
            assertNotNull(newIssue.getId());
            assertEquals(testCompany.getId(), newIssue.getCompanyId());
            System.out.println("    ✓ 생성 완료: " + newIssue.getTitle() + " (ID: " + newIssue.getId() + ")");
            
            // READ
            System.out.println("  [READ] 이슈 조회");
            List<Issue> issues = issueService.getAllIssues();
            assertFalse(issues.isEmpty());
            assertTrue(issues.stream().anyMatch(i -> i.getId().equals(newIssue.getId())));
            System.out.println("    ✓ 조회 완료: " + issues.size() + "건");
            
            Issue found = issues.stream()
                .filter(i -> i.getId().equals(newIssue.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(found);
            assertEquals(newIssue.getTitle(), found.getTitle());
            System.out.println("    ✓ ID로 조회 완료");
            
            // UPDATE
            System.out.println("  [UPDATE] 이슈 수정");
            found.setStatus("RESOLVED");
            issueService.updateIssue(found);
            List<Issue> updatedList = issueService.getAllIssues();
            Issue updated = updatedList.stream()
                .filter(i -> i.getId().equals(found.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(updated);
            assertEquals("RESOLVED", updated.getStatus());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 이슈 삭제");
            issueService.deleteIssue(newIssue.getId());
            List<Issue> afterDelete = issueService.getAllIssues();
            assertFalse(afterDelete.stream().anyMatch(i -> i.getId().equals(newIssue.getId())));
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 이슈 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 이슈 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("이슈 CRUD 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("6. 고객소통 (CustomerCommunication) CRUD 테스트")
    void testCustomerCommunicationCrud() {
        System.out.println("\n[6] 고객소통 CRUD 테스트");
        
        try {
            AppContext.getInstance().setCurrentCompany(testCompany);
            
            // CREATE
            System.out.println("  [CREATE] 고객소통 생성");
            LocalDateTime commDate = LocalDateTime.now();
            CustomerCommunication newComm = communicationService.createCommunication(
                "회의",
                "프로젝트 진행 상황 논의",
                "프로젝트 진행 상황에 대해 논의했습니다.",
                "고객 담당자",
                "우리 담당자",
                commDate,
                "높음",
                commDate.plusDays(7),
                "CRUD 테스트"
            );
            assertNotNull(newComm);
            assertNotNull(newComm.getId());
            assertEquals(testCompany.getId(), newComm.getCompanyId());
            System.out.println("    ✓ 생성 완료: " + newComm.getType() + " (ID: " + newComm.getId() + ")");
            
            // READ
            System.out.println("  [READ] 고객소통 조회");
            List<CustomerCommunication> communications = communicationService.getAllCommunications();
            assertFalse(communications.isEmpty());
            assertTrue(communications.stream().anyMatch(c -> c.getId().equals(newComm.getId())));
            System.out.println("    ✓ 조회 완료: " + communications.size() + "건");
            
            CustomerCommunication found = communications.stream()
                .filter(c -> c.getId().equals(newComm.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(found);
            assertEquals(newComm.getType(), found.getType());
            System.out.println("    ✓ ID로 조회 완료");
            
            // UPDATE
            System.out.println("  [UPDATE] 고객소통 수정");
            found.setStatus("COMPLETED");
            communicationService.updateCommunication(found);
            List<CustomerCommunication> updatedList = communicationService.getAllCommunications();
            CustomerCommunication updated = updatedList.stream()
                .filter(c -> c.getId().equals(found.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(updated);
            assertEquals("COMPLETED", updated.getStatus());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 고객소통 삭제");
            communicationService.deleteCommunication(newComm.getId());
            List<CustomerCommunication> afterDelete = communicationService.getAllCommunications();
            assertFalse(afterDelete.stream().anyMatch(c -> c.getId().equals(newComm.getId())));
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 고객소통 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("고객소통 CRUD 테스트 실패", e);
        }
    }
    
    @Test
    @DisplayName("7. 공통코드 (CommonCode) CRUD 테스트")
    void testCommonCodeCrud() {
        System.out.println("\n[7] 공통코드 CRUD 테스트");
        
        try {
            // CREATE
            System.out.println("  [CREATE] 공통코드 생성");
            String codeValue = "TEST_CODE_" + System.currentTimeMillis();
            CommonCode newCode = commonCodeService.createCode(
                "TEST_TYPE",
                codeValue,
                "CRUD 테스트 코드",
                "테스트용 코드",
                1
            );
            assertNotNull(newCode);
            assertNotNull(newCode.getId());
            System.out.println("    ✓ 생성 완료: " + newCode.getName() + " (ID: " + newCode.getId() + ")");
            
            // READ
            System.out.println("  [READ] 공통코드 조회");
            List<CommonCode> codes = commonCodeService.getAllCodes();
            assertFalse(codes.isEmpty());
            assertTrue(codes.stream().anyMatch(c -> c.getId().equals(newCode.getId())));
            System.out.println("    ✓ 조회 완료: " + codes.size() + "건");
            
            CommonCode found = codes.stream()
                .filter(c -> c.getId().equals(newCode.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(found);
            assertEquals(newCode.getName(), found.getName());
            System.out.println("    ✓ ID로 조회 완료");
            
            // UPDATE
            System.out.println("  [UPDATE] 공통코드 수정");
            found.setName("수정된 코드명");
            commonCodeService.updateCode(found);
            List<CommonCode> updatedList = commonCodeService.getAllCodes();
            CommonCode updated = updatedList.stream()
                .filter(c -> c.getId().equals(found.getId()))
                .findFirst()
                .orElse(null);
            assertNotNull(updated);
            assertEquals("수정된 코드명", updated.getName());
            System.out.println("    ✓ 수정 완료");
            
            // DELETE
            System.out.println("  [DELETE] 공통코드 삭제");
            commonCodeService.deleteCode(newCode.getId());
            List<CommonCode> afterDelete = commonCodeService.getAllCodes();
            assertFalse(afterDelete.stream().anyMatch(c -> c.getId().equals(newCode.getId())));
            System.out.println("    ✓ 삭제 완료");
            
            System.out.println("  ✓ 공통코드 CRUD 테스트 성공");
            
        } catch (Exception e) {
            System.err.println("  ✗ 공통코드 CRUD 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("공통코드 CRUD 테스트 실패", e);
        }
    }
    
    @AfterAll
    void tearDownAll() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("전체 탭 CRUD 데이터 검증 통합 테스트 완료");
        System.out.println("=".repeat(70) + "\n");
    }
}
