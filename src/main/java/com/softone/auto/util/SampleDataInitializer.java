package com.softone.auto.util;

import com.softone.auto.model.Company;
import com.softone.auto.model.Developer;
import com.softone.auto.model.Issue;
import com.softone.auto.model.Attendance;
import com.softone.auto.model.CustomerCommunication;
import com.softone.auto.model.WeeklyReport;
import com.softone.auto.service.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 샘플 데이터 초기화 유틸리티
 * 데모 목적으로 샘플 데이터를 생성합니다
 */
public class SampleDataInitializer {
    
    private final CompanyService companyService;
    private final DeveloperService developerService;
    private final IssueService issueService;
    private final AttendanceService attendanceService;
    private final CustomerCommunicationService communicationService;
    private final WeeklyReportService reportService;
    
    public SampleDataInitializer() {
        System.out.println("SampleDataInitializer 초기화 시작...");
        try {
            System.out.println("  → CompanyService 초기화 중...");
            this.companyService = new CompanyService();
            System.out.println("  ✓ CompanyService 초기화 완료");
            
            System.out.println("  → DeveloperService 초기화 중...");
            this.developerService = new DeveloperService();
            System.out.println("  ✓ DeveloperService 초기화 완료");
            
            System.out.println("  → IssueService 초기화 중...");
            this.issueService = new IssueService();
            System.out.println("  ✓ IssueService 초기화 완료");
            
            System.out.println("  → AttendanceService 초기화 중...");
            this.attendanceService = new AttendanceService();
            System.out.println("  ✓ AttendanceService 초기화 완료");
            
            System.out.println("  → CustomerCommunicationService 초기화 중...");
            this.communicationService = new CustomerCommunicationService();
            System.out.println("  ✓ CustomerCommunicationService 초기화 완료");
            
            System.out.println("  → WeeklyReportService 초기화 중...");
            this.reportService = new WeeklyReportService();
            System.out.println("  ✓ WeeklyReportService 초기화 완료");
            
            System.out.println("SampleDataInitializer 초기화 완료!\n");
        } catch (Exception e) {
            System.err.println("SampleDataInitializer 초기화 실패!");
            System.err.println("오류 메시지: " + e.getMessage());
            System.err.println("오류 클래스: " + e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("SampleDataInitializer 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 모든 샘플 데이터 초기화
     */
    public void initializeAllSampleData() {
        System.out.println("========================================");
        System.out.println("샘플 데이터 초기화 시작...");
        System.out.println("========================================");
        
        int successCount = 0;
        int failCount = 0;
        
        // 회사 데이터 초기화
        try {
            System.out.println("\n[1/5] 회사 데이터 초기화 시작");
            initializeCompaniesSafely();
            // 생성 확인
            var companies = companyService.getAllCompanies();
            System.out.println("  → 생성된 회사 수: " + companies.size());
            if (companies.size() > 0) {
                successCount++;
                System.out.println("  ✓ 회사 데이터 초기화 성공");
            } else {
                failCount++;
                System.err.println("  ✗ 회사 데이터가 생성되지 않았습니다!");
            }
        } catch (Exception e) {
            failCount++;
            System.err.println("  ✗ 회사 데이터 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 개발자 데이터 초기화
        try {
            System.out.println("\n[2/5] 개발자 데이터 초기화 시작");
            initializeDevelopersSafely();
            // 생성 확인
            var developers = developerService.getAllDevelopers();
            System.out.println("  → 생성된 개발자 수: " + developers.size());
            if (developers.size() > 0) {
                successCount++;
                System.out.println("  ✓ 개발자 데이터 초기화 성공");
            } else {
                failCount++;
                System.err.println("  ✗ 개발자 데이터가 생성되지 않았습니다!");
            }
        } catch (Exception e) {
            failCount++;
            System.err.println("  ✗ 개발자 데이터 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 이슈 데이터 초기화
        try {
            System.out.println("\n[3/5] 이슈 데이터 초기화 시작");
            initializeIssuesSafely();
            // 생성 확인
            var issues = issueService.getAllIssues();
            System.out.println("  → 생성된 이슈 수: " + issues.size());
            if (issues.size() > 0) {
                successCount++;
                System.out.println("  ✓ 이슈 데이터 초기화 성공");
            } else {
                failCount++;
                System.err.println("  ✗ 이슈 데이터가 생성되지 않았습니다!");
            }
        } catch (Exception e) {
            failCount++;
            System.err.println("  ✗ 이슈 데이터 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 고객 소통 데이터 초기화
        try {
            System.out.println("\n[4/5] 고객 소통 데이터 초기화 시작");
            initializeCommunicationsSafely();
            // 생성 확인
            var communications = communicationService.getAllCommunications();
            System.out.println("  → 생성된 고객 소통 수: " + communications.size());
            if (communications.size() > 0) {
                successCount++;
                System.out.println("  ✓ 고객 소통 데이터 초기화 성공");
            } else {
                failCount++;
                System.err.println("  ✗ 고객 소통 데이터가 생성되지 않았습니다!");
            }
        } catch (Exception e) {
            failCount++;
            System.err.println("  ✗ 고객 소통 데이터 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 주간보고서 데이터 초기화
        try {
            System.out.println("\n[5/5] 주간보고서 데이터 초기화 시작");
            initializeWeeklyReportsSafely();
            successCount++;
            System.out.println("  ✓ 주간보고서 데이터 초기화 성공");
        } catch (Exception e) {
            failCount++;
            System.err.println("  ✗ 주간보고서 데이터 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n========================================");
        System.out.println("샘플 데이터 초기화 완료!");
        System.out.println("  성공: " + successCount + "개");
        System.out.println("  실패: " + failCount + "개");
        System.out.println("========================================\n");
    }
    
    /**
     * 회사 데이터 안전 초기화
     */
    private void initializeCompaniesSafely() throws Exception {
        try {
            var existing = companyService.getAllCompanies();
            if (existing == null || existing.isEmpty()) {
                System.out.println("  → 회사 데이터가 없어 초기화를 진행합니다...");
                initializeCompanies();
            } else {
                System.out.println("  → 회사 데이터가 이미 존재합니다 (" + existing.size() + "개). 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("  → 회사 데이터 확인 중 오류 발생: " + e.getMessage());
            System.err.println("  → 강제로 초기화를 시도합니다...");
            e.printStackTrace();
            initializeCompanies(); // 재시도
        }
    }
    
    /**
     * 개발자 데이터 안전 초기화
     */
    private void initializeDevelopersSafely() throws Exception {
        try {
            var existing = developerService.getAllDevelopers();
            if (existing == null || existing.isEmpty()) {
                System.out.println("  → 개발자 데이터가 없어 초기화를 진행합니다...");
                initializeDevelopers();
                initializeAttendance();
            } else {
                System.out.println("  → 개발자 데이터가 이미 존재합니다 (" + existing.size() + "개). 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("  → 개발자 데이터 확인 중 오류 발생: " + e.getMessage());
            System.err.println("  → 강제로 초기화를 시도합니다...");
            e.printStackTrace();
            initializeDevelopers();
            initializeAttendance();
        }
    }
    
    /**
     * 이슈 데이터 안전 초기화
     */
    private void initializeIssuesSafely() throws Exception {
        try {
            var existing = issueService.getAllIssues();
            if (existing == null || existing.isEmpty()) {
                System.out.println("  → 이슈 데이터가 없어 초기화를 진행합니다...");
                initializeIssues();
            } else {
                System.out.println("  → 이슈 데이터가 이미 존재합니다 (" + existing.size() + "개). 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("  → 이슈 데이터 확인 중 오류 발생: " + e.getMessage());
            System.err.println("  → 강제로 초기화를 시도합니다...");
            e.printStackTrace();
            initializeIssues();
        }
    }
    
    /**
     * 고객 소통 데이터 안전 초기화
     */
    private void initializeCommunicationsSafely() throws Exception {
        try {
            var existing = communicationService.getAllCommunications();
            if (existing == null || existing.isEmpty()) {
                System.out.println("  → 고객 소통 데이터가 없어 초기화를 진행합니다...");
                initializeCommunications();
            } else {
                System.out.println("  → 고객 소통 데이터가 이미 존재합니다 (" + existing.size() + "개). 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("  → 고객 소통 데이터 확인 중 오류 발생: " + e.getMessage());
            System.err.println("  → 강제로 초기화를 시도합니다...");
            e.printStackTrace();
            initializeCommunications();
        }
    }
    
    /**
     * 주간보고서 데이터 안전 초기화
     */
    private void initializeWeeklyReportsSafely() throws Exception {
        try {
            var allReports = new com.softone.auto.repository.sqlite.WeeklyReportSqliteRepository().findAll();
            if (allReports == null || allReports.isEmpty()) {
                System.out.println("  → 주간보고서 데이터가 없어 초기화를 진행합니다...");
                initializeWeeklyReportsForAllCompanies();
            } else {
                System.out.println("  → 주간보고서 데이터가 이미 존재합니다 (" + allReports.size() + "개). 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("  → 주간보고서 데이터 확인 중 오류 발생: " + e.getMessage());
            System.err.println("  → 강제로 초기화를 시도합니다...");
            e.printStackTrace();
            initializeWeeklyReportsForAllCompanies();
        }
    }
    
    /**
     * 안전하게 컬렉션이 비어있는지 확인 (예외 발생 시 true 반환)
     */
    private <T> boolean isEmptySafely(java.util.function.Supplier<java.util.List<T>> supplier) {
        try {
            java.util.List<T> list = supplier.get();
            return list == null || list.isEmpty();
        } catch (Exception e) {
            System.err.println("데이터 조회 중 오류 발생 (비어있는 것으로 간주): " + e.getMessage());
            return true; // 오류 발생 시 비어있는 것으로 간주하여 초기화 진행
        }
    }
    
    /**
     * 모든 회사의 주간보고서 초기화 (1년치)
     */
    private void initializeWeeklyReportsForAllCompanies() {
        System.out.println("=== 회사별 주간보고서 1년치 생성 시작 ===");
        
        // 현재 선택된 회사 백업 (복원용)
        Company originalCompany = AppContext.getInstance().getCurrentCompany();
        System.out.println("  현재 선택된 회사 백업: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        var companies = companyService.getAllCompanies();
        System.out.println("  → 생성 대상 회사 수: " + companies.size());
        
        if (companies.isEmpty()) {
            System.out.println("  ⚠️ 회사 데이터가 없어 주간보고서 생성을 건너뜁니다.");
            return;
        }
        
        for (Company company : companies) {
            try {
                AppContext.getInstance().setCurrentCompany(company);
                System.out.println("\n  → " + company.getName() + " 주간보고서 생성 시작");
                System.out.println("    회사 ID: " + company.getId());
                System.out.println("    프로젝트: " + company.getProjectName());
                
                // 각 회사별 최근 12주치 보고서 생성 (3개월)
                int reportCount = 0;
                for (int weekOffset = 12; weekOffset >= 1; weekOffset--) {
                    try {
                        LocalDate weekStart = LocalDate.now().minusWeeks(weekOffset)
                            .minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
                        
                        // 이미 존재하는 보고서는 건너뛰기
                        if (reportService.existsByStartDate(weekStart)) {
                            System.out.println("    [주차 " + weekOffset + "] 이미 존재: " + weekStart);
                            continue;
                        }
                        
                        WeeklyReport report = reportService.createReport(
                            "주간 업무 보고서 - " + weekStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            weekStart,
                            weekStart.plusDays(4),
                            company.getProjectName(),
                            "관리자"
                        );
                        
                        // 회사별 맞춤 업무 항목 생성
                        generateCompanySpecificWorkItems(report, company, weekOffset);
                        
                        // 체크리스트 초기화 (10개 항목, 진행 상황에 따라 다르게)
                        report.getCheckItems().clear();
                        for (int i = 0; i < 10; i++) {
                            // 초기 주차는 완료율이 낮고, 최근 주차는 완료율이 높음
                            boolean completed = (weekOffset <= 4) ? (i < 7) : (i < 5);
                            report.getCheckItems().add(completed);
                        }
                        
                        // 추가 노트 생성
                        String content = generateYearlyWeeklyContent(weekOffset, company.getName());
                        report.setAdditionalNotes(content);
                        
                        // 금주 업무 텍스트 생성
                        String thisWeekTasks = generateThisWeekTasksText(company, weekOffset);
                        report.setThisWeekTasksText(thisWeekTasks);
                        
                        reportService.updateReport(report);
                        reportCount++;
                        
                        if (reportCount % 4 == 0) {
                            System.out.println("    → 진행: " + reportCount + "주치 보고서 생성 완료");
                        }
                        
                    } catch (Exception e) {
                        System.err.println("    ✗ 주차 " + weekOffset + " 보고서 생성 실패: " + e.getMessage());
                        e.printStackTrace();
                        // 계속 진행
                    }
                }
                
                System.out.println("    ✓ " + company.getName() + ": " + reportCount + "주치 보고서 생성 완료");
                
            } catch (Exception e) {
                System.err.println("  ✗ " + company.getName() + " 주간보고서 생성 중 오류: " + e.getMessage());
                e.printStackTrace();
                // 다음 회사로 계속 진행
            }
        }
        
        // 원래 선택된 회사로 복원
        AppContext.getInstance().setCurrentCompany(originalCompany);
        System.out.println("\n  원래 회사로 복원: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        System.out.println("=== 회사별 주간보고서 생성 완료 ===\n");
    }
    
    /**
     * 회사별 맞춤 업무 항목 생성
     */
    private void generateCompanySpecificWorkItems(WeeklyReport report, Company company, int weekOffset) {
        String companyName = company.getName();
        
        // 회사별로 다른 업무 항목 생성
        if (companyName.contains("LG전자")) {
            // LG전자: 스마트홈 플랫폼 관련 업무
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "스마트TV 연동 모듈 개발", "김철수", getProgressStatus(weekOffset, 80), getProgress(weekOffset, 80), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "IoT 디바이스 통신 프로토콜 구현", "이영희", getProgressStatus(weekOffset, 70), getProgress(weekOffset, 70), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "홈 오토메이션 시나리오 테스트", "박지성", getProgressStatus(weekOffset, 60), getProgress(weekOffset, 60), ""
            ));
            
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "음성 인식 기능 개선", "김철수", "계획", 0, ""
            ));
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "보안 취약점 점검", "이영희", "계획", 0, ""
            ));
            
        } else if (companyName.contains("SK텔레콤")) {
            // SK텔레콤: 5G 네트워크 관리 시스템
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "5G 기지국 모니터링 대시보드 개발", "김철수", getProgressStatus(weekOffset, 75), getProgress(weekOffset, 75), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "네트워크 트래픽 분석 알고리즘 구현", "이영희", getProgressStatus(weekOffset, 65), getProgress(weekOffset, 65), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "장애 알림 시스템 구축", "박지성", getProgressStatus(weekOffset, 55), getProgress(weekOffset, 55), ""
            ));
            
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "성능 최적화 작업", "김철수", "계획", 0, ""
            ));
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "부하 테스트 진행", "이영희", "계획", 0, ""
            ));
            
        } else if (companyName.contains("삼성SDS")) {
            // 삼성SDS: ERP 시스템
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "재무 관리 모듈 고도화", "김철수", getProgressStatus(weekOffset, 85), getProgress(weekOffset, 85), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "인사 관리 시스템 통합", "이영희", getProgressStatus(weekOffset, 70), getProgress(weekOffset, 70), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "데이터 마이그레이션 스크립트 작성", "박지성", getProgressStatus(weekOffset, 60), getProgress(weekOffset, 60), ""
            ));
            
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "보고서 생성 기능 개선", "김철수", "계획", 0, ""
            ));
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "사용자 권한 관리 강화", "이영희", "계획", 0, ""
            ));
            
        } else {
            // 기본 업무 항목
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "코어 모듈 개발", "김철수", getProgressStatus(weekOffset, 70), getProgress(weekOffset, 70), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "API 테스트", "이영희", getProgressStatus(weekOffset, 80), getProgress(weekOffset, 80), ""
            ));
            report.getThisWeekPlan().add(new WeeklyReport.WorkItem(
                "버그 수정", "박지성", getProgressStatus(weekOffset, 50), getProgress(weekOffset, 50), ""
            ));
            
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "통합 테스트", "김철수", "계획", 0, ""
            ));
            report.getLastWeekWork().add(new WeeklyReport.WorkItem(
                "UI 개선", "이영희", "계획", 0, ""
            ));
        }
    }
    
    /**
     * 진행 상태 반환 (주차에 따라)
     */
    private String getProgressStatus(int weekOffset, int baseProgress) {
        // 최근 주차일수록 완료율이 높음
        int adjustedProgress = baseProgress + (13 - weekOffset) * 2;
        if (adjustedProgress >= 100) return "완료";
        if (adjustedProgress >= 80) return "진행중";
        if (adjustedProgress >= 50) return "진행중";
        return "계획";
    }
    
    /**
     * 진행률 반환 (주차에 따라)
     */
    private int getProgress(int weekOffset, int baseProgress) {
        int adjustedProgress = baseProgress + (13 - weekOffset) * 2;
        return Math.min(100, Math.max(0, adjustedProgress));
    }
    
    /**
     * 금주 업무 텍스트 생성
     */
    private String generateThisWeekTasksText(Company company, int weekOffset) {
        String companyName = company.getName();
        StringBuilder tasks = new StringBuilder();
        
        if (companyName.contains("LG전자")) {
            tasks.append("1. 스마트TV 연동 모듈 개발 진행\n");
            tasks.append("   - TV 제어 API 구현 완료\n");
            tasks.append("   - 디바이스 인식 로직 개선\n\n");
            tasks.append("2. IoT 디바이스 통신 프로토콜 구현\n");
            tasks.append("   - MQTT 브로커 연동 테스트\n");
            tasks.append("   - 메시지 큐 최적화\n\n");
            tasks.append("3. 홈 오토메이션 시나리오 테스트\n");
            tasks.append("   - 조명 제어 시나리오 검증\n");
            tasks.append("   - 온도 조절 자동화 테스트");
            
        } else if (companyName.contains("SK텔레콤")) {
            tasks.append("1. 5G 기지국 모니터링 대시보드 개발\n");
            tasks.append("   - 실시간 데이터 수집 모듈 완성\n");
            tasks.append("   - 차트 라이브러리 통합\n\n");
            tasks.append("2. 네트워크 트래픽 분석 알고리즘 구현\n");
            tasks.append("   - 패턴 인식 알고리즘 개발\n");
            tasks.append("   - 이상 탐지 로직 추가\n\n");
            tasks.append("3. 장애 알림 시스템 구축\n");
            tasks.append("   - SMS/이메일 알림 기능 구현\n");
            tasks.append("   - 알림 규칙 엔진 개발");
            
        } else if (companyName.contains("삼성SDS")) {
            tasks.append("1. 재무 관리 모듈 고도화\n");
            tasks.append("   - 예산 관리 기능 추가\n");
            tasks.append("   - 결산 리포트 자동화\n\n");
            tasks.append("2. 인사 관리 시스템 통합\n");
            tasks.append("   - 조직도 연동 완료\n");
            tasks.append("   - 급여 계산 모듈 통합\n\n");
            tasks.append("3. 데이터 마이그레이션 스크립트 작성\n");
            tasks.append("   - 레거시 데이터 변환 로직 개발\n");
            tasks.append("   - 검증 스크립트 작성");
            
        } else {
            tasks.append("1. 코어 모듈 개발\n");
            tasks.append("   - 핵심 기능 구현 진행\n");
            tasks.append("   - 단위 테스트 작성\n\n");
            tasks.append("2. API 테스트\n");
            tasks.append("   - 통합 테스트 시나리오 작성\n");
            tasks.append("   - 성능 테스트 진행\n\n");
            tasks.append("3. 버그 수정\n");
            tasks.append("   - 이슈 트래커 확인 및 수정\n");
            tasks.append("   - 코드 리뷰 진행");
        }
        
        return tasks.toString();
    }
    
    /**
     * 1년치 주간보고서 내용 생성
     */
    private static String generateYearlyWeeklyContent(int weekOffset, String companyName) {
        // 분기별로 다른 내용 생성
        if (weekOffset > 39) {  // 13주 이전 (1분기)
            return "■ 전체 프로젝트\n" +
                   "  - 프로젝트 계획 및 설계 단계\n" +
                   "  - 요구사항 분석 진행\n\n" +
                   "■ 당사 참여부문\n" +
                   "  - 아키텍처 설계\n" +
                   "  - 개발 환경 구축\n\n" +
                   "■ 고객사 주요 동향\n" +
                   "  - 프로젝트 킥오프 미팅\n\n" +
                   "■ 경쟁회사 주요 동향\n" +
                   "  - 특이사항 없음\n\n" +
                   "■ 기타\n" +
                   "  - " + companyName + " 정상 진행";
        } else if (weekOffset > 26) {  // 26주 이전 (2분기)
            return "■ 전체 프로젝트\n" +
                   "  - 핵심 기능 개발 진행 중\n" +
                   "  - API 서버 구축 (진척도 " + (70 - weekOffset) + "%)\n\n" +
                   "■ 당사 참여부문\n" +
                   "  - 백엔드 개발 진행\n" +
                   "  - 단위 테스트 작성\n\n" +
                   "■ 고객사 주요 동향\n" +
                   "  - 중간 점검 미팅\n\n" +
                   "■ 경쟁회사 주요 동향\n" +
                   "  - 타사 프로젝트 진행 상황 모니터링\n\n" +
                   "■ 기타\n" +
                   "  - 정상 진행 중";
        } else if (weekOffset > 13) {  // 13주 이전 (3분기)
            return "■ 전체 프로젝트\n" +
                   "  - 통합 테스트 진행\n" +
                   "  - 성능 최적화 작업\n\n" +
                   "■ 당사 참여부문\n" +
                   "  - 버그 수정 및 안정화\n" +
                   "  - 사용자 매뉴얼 작성\n\n" +
                   "■ 고객사 주요 동향\n" +
                   "  - UAT 준비\n\n" +
                   "■ 경쟁회사 주요 동향\n" +
                   "  - 특이사항 없음\n\n" +
                   "■ 기타\n" +
                   "  - 테스트 진행 중";
        } else {  // 최근 13주 (4분기)
            return "■ 전체 프로젝트\n" +
                   "  - 고객사 일정 단축 요청\n" +
                   "  - 최종 점검 및 배포 준비\n\n" +
                   "■ 당사 참여부문\n" +
                   "  - 최종 버그 수정\n" +
                   "  - 운영 이관 준비\n\n" +
                   "■ 고객사 주요 동향\n" +
                   "  - 인사 이동: 신임 부장 취임\n" +
                   "  - 최종 검수 진행\n\n" +
                   "■ 경쟁회사 주요 동향\n" +
                   "  - 경쟁사 유사 프로젝트 완료\n\n" +
                   "■ 기타\n" +
                   "  - 릴리즈 준비 중";
        }
    }
    
    /**
     * 회사 샘플 데이터 초기화
     */
    private void initializeCompanies() {
        System.out.println("회사 샘플 데이터 생성 중...");
        
        try {
            companyService.createCompany(
                "LG전자",
                "스마트홈 플랫폼 구축",
                "파견",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "스마트TV 연동 플랫폼 개발"
            );
            System.out.println("  ✓ LG전자 생성 완료");
        } catch (Exception e) {
            System.err.println("  ✗ LG전자 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            companyService.createCompany(
                "SK텔레콤",
                "5G 기지국 관리 시스템",
                "용역",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 11, 30),
                "네트워크 모니터링 시스템 구축"
            );
            System.out.println("  ✓ SK텔레콤 생성 완료");
        } catch (Exception e) {
            System.err.println("  ✗ SK텔레콤 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            companyService.createCompany(
                "삼성SDS",
                "ERP 시스템 고도화",
                "파견",
                LocalDate.of(2023, 9, 1),
                LocalDate.of(2024, 8, 31),
                "차세대 ERP 시스템 개발"
            );
            System.out.println("  ✓ 삼성SDS 생성 완료");
        } catch (Exception e) {
            System.err.println("  ✗ 삼성SDS 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("회사 샘플 데이터 생성 완료");
    }
    
    /**
     * 개발자 샘플 데이터 초기화 (회사별)
     */
    private void initializeDevelopers() {
        System.out.println("개발자 샘플 데이터 생성 중...");
        
        // 현재 선택된 회사 백업 (복원용)
        Company originalCompany = AppContext.getInstance().getCurrentCompany();
        System.out.println("  현재 선택된 회사 백업: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        // 회사 목록 가져오기
        var companies = companyService.getAllCompanies();
        if (companies.isEmpty()) {
            System.out.println("회사 데이터가 없어 개발자 생성 건너뛰기");
            return;
        }
        
        // LG전자에 개발자 2명
        Company lg = companyService.getCompanyByName("LG전자");
        if (lg != null) {
            AppContext.getInstance().setCurrentCompany(lg);
            
            developerService.createDeveloper(
                "김철수", "수석", "백엔드 개발", "스마트홈팀",
                "kim.cs@lg.com", "010-1234-5678", "010-9999-1111",
                LocalDate.of(2024, 1, 15),
                "Spring Boot 전문가, IoT 경험"
            );
            
            developerService.createDeveloper(
                "이영희", "선임", "프론트엔드 개발", "스마트홈팀",
                "lee.yh@lg.com", "010-2345-6789", "010-9999-2222",
                LocalDate.of(2024, 2, 1),
                "React 전문가, TV 앱 개발 경험"
            );
        }
        
        // SK텔레콤에 개발자 2명
        Company sk = companyService.getCompanyByName("SK텔레콤");
        if (sk != null) {
            AppContext.getInstance().setCurrentCompany(sk);
            
            developerService.createDeveloper(
                "박지성", "책임", "시스템 개발", "네트워크팀",
                "park.js@sk.com", "010-3456-7890", "010-9999-3333",
                LocalDate.of(2024, 3, 15),
                "5G 네트워크 시스템 경험"
            );
            
            developerService.createDeveloper(
                "정수진", "선임", "데이터베이스 관리", "인프라팀",
                "jung.sj@sk.com", "010-4567-8901", "010-9999-4444",
                LocalDate.of(2024, 4, 1),
                "대용량 DB 운영 경험"
            );
        }
        
        // 삼성SDS에 개발자 2명
        Company samsung = companyService.getCompanyByName("삼성SDS");
        if (samsung != null) {
            AppContext.getInstance().setCurrentCompany(samsung);
            
            developerService.createDeveloper(
                "최민호", "선임", "ERP 개발", "ERP팀",
                "choi.mh@samsung.com", "010-5678-9012", "010-9999-5555",
                LocalDate.of(2023, 9, 15),
                "SAP/Java 개발 경험"
            );
            
            developerService.createDeveloper(
                "강서연", "사원", "QA 엔지니어", "품질관리팀",
                "kang.sy@samsung.com", "010-6789-0123", "010-9999-6666",
                LocalDate.of(2023, 10, 1),
                "ERP 테스트 자동화"
            );
        }
        
        // 원래 선택된 회사로 복원
        AppContext.getInstance().setCurrentCompany(originalCompany);
        System.out.println("  원래 회사로 복원: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        System.out.println("개발자 6명 생성 완료 (회사별 2명씩)");
    }
    
    /**
     * 주간보고서 샘플 데이터 생성 (호환성 유지용, 사용 안 함)
     * @deprecated Use initializeWeeklyReportsForAllCompanies() instead
     */
    @Deprecated
    public static void initializeWeeklyReports(WeeklyReportService reportService) {
        // 더 이상 사용하지 않음 - initializeWeeklyReportsForAllCompanies()가 자동 호출됨
        System.out.println("initializeWeeklyReports() 호출됨 - 스킵 (자동 생성됨)");
    }
    
    /**
     * 주차별 보고서 내용 생성 (사용 안 함)
     */
    @Deprecated
    private static String generateWeeklyContent(int weekOffset) {
        switch (weekOffset) {
            case 8:
                return "■ 전체 프로젝트\n" +
                       "  - 프로젝트 계획 수립\n" +
                       "  - 팀원 구성 및 역할 배분\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 요구사항 분석 착수\n" +
                       "  - 기술 스택 선정 회의\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 초기 미팅 진행\n\n" +
                       "■ 기타\n" +
                       "  - 특이사항 없음";
            case 7:
                return "■ 전체 프로젝트\n" +
                       "  - 프로젝트 킥오프 미팅 완료\n" +
                       "  - 개발 환경 구축 완료\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 기본 아키텍처 설계 완료\n" +
                       "  - 데이터베이스 스키마 초안 작성\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 고객사 담당자 배정 완료\n\n" +
                       "■ 기타\n" +
                       "  - 특이사항 없음";
            case 6:
                return "■ 전체 프로젝트\n" +
                       "  - API 명세서 작성 완료\n" +
                       "  - 화면 설계서 1차 검토\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 인증/권한 모듈 개발 착수\n" +
                       "  - 공통 컴포넌트 라이브러리 구축\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 고객사 요구사항 추가 1건\n\n" +
                       "■ 기타\n" +
                       "  - 개발 서버 구축 완료";
            case 5:
                return "■ 전체 프로젝트\n" +
                       "  - API 서버 개발 진행중 (진척도 30%)\n" +
                       "  - 프론트엔드 UI/UX 디자인 완료\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 백엔드 REST API 10개 개발 완료\n" +
                       "  - 데이터베이스 마이그레이션 스크립트 작성\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 고객사 중간 점검 미팅 진행\n" +
                       "  - 추가 요구사항 2건 접수\n\n" +
                       "■ 기타\n" +
                       "  - 개발 서버 일시 장애 발생 및 복구 완료";
            case 4:
                return "■ 전체 프로젝트\n" +
                       "  - 백엔드 API 개발 50% 완료\n" +
                       "  - 프론트엔드 컴포넌트 개발 착수\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 사용자 관리 모듈 개발 완료\n" +
                       "  - 디바이스 연동 API 테스트 중\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 고객사 데모 데이 일정 확정\n\n" +
                       "■ 기타\n" +
                       "  - 코드 리뷰 프로세스 도입";
            case 3:
                return "■ 전체 프로젝트\n" +
                       "  - 통합 테스트 환경 구축\n" +
                       "  - 성능 테스트 시나리오 작성\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 모바일 앱 연동 기능 구현\n" +
                       "  - 알림 서비스 개발 완료\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 고객사 UAT 일정 협의\n\n" +
                       "■ 기타\n" +
                       "  - 보안 취약점 점검 실시";
            case 2:
                return "■ 전체 프로젝트\n" +
                       "  - 고객사 전체 프로젝트 일정 단축 요청\n" +
                       "  - 보안 사고 발생 (협력사)\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 고객사 전체 일정 단축 요청 (12월 15일까지)\n" +
                       "  - 추가 인력 투입 검토중\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 인사 이동: 신임 부장 부임\n" +
                       "  - 고객사 보안 감사 진행\n\n" +
                       "■ 기타\n" +
                       "  - 특이사항 없음";
            case 1:
                return "■ 전체 프로젝트\n" +
                       "  - 최종 통합 테스트 진행\n" +
                       "  - 배포 준비 체크리스트 작성\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 버그 수정 완료 (10건)\n" +
                       "  - 운영 매뉴얼 작성 중\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 고객사 최종 검수 준비\n\n" +
                       "■ 기타\n" +
                       "  - 릴리즈 노트 작성 완료";
            default:
                return "■ 전체 프로젝트\n" +
                       "  - 정상 진행 중\n\n" +
                       "■ 당사 참여부문\n" +
                       "  - 정상 진행 중\n\n" +
                       "■ 고객사/CNS 주요 동향\n" +
                       "  - 특이사항 없음\n\n" +
                       "■ 기타\n" +
                       "  - 특이사항 없음";
        }
    }
    
    /**
     * 이슈 샘플 데이터 초기화 (회사별)
     */
    private void initializeIssues() {
        System.out.println("이슈 샘플 데이터 생성 중...");
        
        // 현재 선택된 회사 백업 (복원용)
        Company originalCompany = AppContext.getInstance().getCurrentCompany();
        System.out.println("  현재 선택된 회사 백업: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        int createdCount = 0;
        
        // LG전자 이슈
        try {
            Company lg = companyService.getCompanyByName("LG전자");
            if (lg != null) {
                AppContext.getInstance().setCurrentCompany(lg);
                
                issueService.createIssue(
                    "TV 앱 로딩 속도 개선",
                    "스마트TV 앱 초기 로딩이 5초 이상 소요됩니다.",
                    "기술",
                    "높음",
                    "김철수",
                    "이영희",
                    "이미지 최적화 진행 중"
                );
                createdCount++;
                System.out.println("  ✓ LG전자 이슈 생성 완료");
            }
        } catch (Exception e) {
            System.err.println("  ✗ LG전자 이슈 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // SK텔레콤 이슈
        try {
            Company sk = companyService.getCompanyByName("SK텔레콤");
            if (sk != null) {
                AppContext.getInstance().setCurrentCompany(sk);
                
                issueService.createIssue(
                    "기지국 데이터 동기화 문제",
                    "실시간 데이터 동기화에서 간헐적 오류 발생",
                    "기술",
                    "높음",
                    "박지성",
                    "정수진",
                    "로그 분석 중"
                );
                createdCount++;
                
                var skIssue = issueService.createIssue(
                    "모니터링 대시보드 구축",
                    "5G 기지국 상태 모니터링 화면 필요",
                    "기술",
                    "보통",
                    "정수진",
                    "박지성",
                    "설계 완료"
                );
                skIssue.setStatus("RESOLVED");
                skIssue.setResolution("대시보드 구축 완료");
                issueService.updateIssue(skIssue);
                createdCount++;
                System.out.println("  ✓ SK텔레콤 이슈 생성 완료");
            }
        } catch (Exception e) {
            System.err.println("  ✗ SK텔레콤 이슈 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 삼성SDS 이슈
        try {
            Company samsung = companyService.getCompanyByName("삼성SDS");
            if (samsung != null) {
                AppContext.getInstance().setCurrentCompany(samsung);
                
                issueService.createIssue(
                    "ERP 결산 모듈 오류",
                    "월말 결산 처리 시 오류 발생",
                    "기술",
                    "높음",
                    "최민호",
                    "최민호",
                    "긴급 패치 준비 중"
                );
                createdCount++;
                
                issueService.createIssue(
                    "사용자 권한 관리 개선",
                    "부서별 권한 설정 기능 추가 요청",
                    "일정",
                    "보통",
                    "강서연",
                    "최민호",
                    "요구사항 분석 중"
                );
                createdCount++;
                System.out.println("  ✓ 삼성SDS 이슈 생성 완료");
            }
        } catch (Exception e) {
            System.err.println("  ✗ 삼성SDS 이슈 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 원래 선택된 회사로 복원
        AppContext.getInstance().setCurrentCompany(originalCompany);
        System.out.println("  원래 회사로 복원: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        System.out.println("이슈 " + createdCount + "개 생성 완료 (회사별)");
    }
    
    /**
     * 근태 샘플 데이터 초기화 (최근 5일)
     */
    private void initializeAttendance() {
        System.out.println("근태 샘플 데이터 생성 중...");
        
        try {
            var developers = developerService.getAllDevelopers();
            if (developers == null || developers.isEmpty()) {
                System.out.println("  개발자 데이터가 없어 근태 데이터를 생성할 수 없습니다.");
                return;
            }
            
            LocalDate today = LocalDate.now();
            
            // 최근 5일간의 근태 데이터 생성
            for (int day = 4; day >= 0; day--) {
                LocalDate date = today.minusDays(day);
                
                // 주말은 제외
                if (date.getDayOfWeek().getValue() >= 6) {
                    continue;
                }
                
                for (var dev : developers) {
                    try {
                        LocalTime checkIn = LocalTime.of(9, 0).plusMinutes((int)(Math.random() * 30));
                        LocalTime checkOut = LocalTime.of(18, 0).plusMinutes((int)(Math.random() * 60));
                        
                        String type = "NORMAL";
                        String notes = "";
                        
                        // 10% 확률로 지각
                        if (Math.random() < 0.1) {
                            checkIn = LocalTime.of(9, 30).plusMinutes((int)(Math.random() * 30));
                            type = "LATE";
                            notes = "교통 체증";
                        }
                        
                        // 5% 확률로 조퇴
                        if (Math.random() < 0.05) {
                            checkOut = LocalTime.of(16, 0).plusMinutes((int)(Math.random() * 60));
                            type = "EARLY_LEAVE";
                            notes = "개인 사정";
                        }
                        
                        attendanceService.createAttendance(
                            dev.getId(),
                            dev.getName(),
                            date,
                            checkIn,
                            checkOut,
                            type,
                            notes
                        );
                    } catch (Exception e) {
                        System.err.println("  근태 데이터 생성 실패 (개발자: " + dev.getName() + "): " + e.getMessage());
                        // 개별 오류는 무시하고 계속 진행
                    }
                }
            }
            
            System.out.println("근태 데이터 생성 완료");
        } catch (Exception e) {
            System.err.println("근태 데이터 초기화 중 오류: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생해도 계속 진행
        }
    }
    
    /**
     * 고객 소통 샘플 데이터 초기화
     */
    private void initializeCommunications() {
        System.out.println("고객 소통 샘플 데이터 생성 중...");
        
        // 현재 선택된 회사 백업 (복원용)
        Company originalCompany = AppContext.getInstance().getCurrentCompany();
        System.out.println("  현재 선택된 회사 백업: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        // 회사 목록 가져오기
        var companies = companyService.getAllCompanies();
        if (companies.isEmpty()) {
            System.out.println("  회사 데이터가 없어 고객소통 생성 건너뛰기");
            return;
        }
        
        // 첫 번째 회사 선택 (또는 LG전자)
        Company targetCompany = companyService.getCompanyByName("LG전자");
        if (targetCompany == null && !companies.isEmpty()) {
            targetCompany = companies.get(0);
        }
        
        if (targetCompany == null) {
            System.out.println("  회사를 찾을 수 없어 고객소통 생성 건너뛰기");
            return;
        }
        
        AppContext.getInstance().setCurrentCompany(targetCompany);
        System.out.println("  선택된 회사: " + targetCompany.getName());
        
        LocalDateTime now = LocalDateTime.now();
        int createdCount = 0;
        
        try {
            communicationService.createCommunication(
                "MEETING",
                "프로젝트 킥오프 미팅",
                "프로젝트 범위, 일정, 리소스에 대한 초기 미팅을 진행했습니다. " +
                "고객사에서 주요 요구사항을 발표하고 우리측에서 제안서를 설명했습니다.",
                "김고객",
                "박지성",
                now.minusDays(7),
                "MEDIUM",
                null,
                "회의록 작성 완료"
            );
            createdCount++;
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 생성 실패 (킥오프 미팅): " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            var comm1 = communicationService.createCommunication(
                "REQUEST",
                "추가 기능 개발 요청",
                "고객사에서 사용자 권한 관리 기능의 세분화를 요청했습니다. " +
                "현재는 관리자/일반사용자만 구분되지만, 부서별/직급별 권한 설정이 필요하다고 합니다.",
                "이담당",
                "김철수",
                now.minusDays(3),
                "HIGH",
                now.plusDays(7),
                "일정 검토 필요"
            );
            createdCount++;
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 생성 실패 (기능 요청): " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            communicationService.createCommunication(
                "QA",
                "시스템 성능 관련 문의",
                "동시 접속자 1000명 이상 처리 가능한지 문의가 왔습니다.",
                "박고객",
                "정수진",
                now.minusDays(2),
                "MEDIUM",
                now.plusDays(3),
                "성능 테스트 결과 공유 예정"
            );
            createdCount++;
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 생성 실패 (성능 문의): " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            var comm2 = communicationService.createCommunication(
                "EMAIL",
                "월간 진척 보고",
                "이번 달 프로젝트 진척 현황을 이메일로 보고했습니다.",
                "최팀장",
                "박지성",
                now.minusDays(5),
                "LOW",
                null,
                ""
            );
            comm2.setStatus("COMPLETED");
            comm2.setResponse("진척률 85% 보고 완료, 고객사에서 만족 표시");
            communicationService.updateCommunication(comm2);
            createdCount++;
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 생성 실패 (진척 보고): " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            communicationService.createCommunication(
                "PHONE",
                "긴급 버그 수정 요청",
                "운영 환경에서 특정 조건에서 결제가 실패하는 버그 발견",
                "김매니저",
                "김철수",
                now.minusHours(3),
                "HIGH",
                now.plusHours(24),
                "긴급 패치 준비 중"
            );
            createdCount++;
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 생성 실패 (긴급 요청): " + e.getMessage());
            e.printStackTrace();
        }
        
        // 원래 선택된 회사로 복원
        AppContext.getInstance().setCurrentCompany(originalCompany);
        System.out.println("  원래 회사로 복원: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        System.out.println("고객 소통 " + createdCount + "개 생성 완료");
    }
}

