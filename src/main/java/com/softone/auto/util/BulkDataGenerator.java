package com.softone.auto.util;

import com.softone.auto.model.*;
import com.softone.auto.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * 대량 샘플 데이터 생성 유틸리티 (1년치)
 */
public class BulkDataGenerator {
    
    private static final Random random = new Random();
    
    /**
     * 기존 데이터 기반 1년치 데이터 생성
     */
    public static void generateYearlyData() {
        System.out.println("=== 기존 데이터 기반 1년치 데이터 생성 시작 ===");
        
        // 현재 선택된 회사 백업 (복원용)
        Company originalCompany = AppContext.getInstance().getCurrentCompany();
        System.out.println("  현재 선택된 회사 백업: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        CompanyService companyService = new CompanyService();
        DeveloperService developerService = new DeveloperService();
        IssueService issueService = new IssueService();
        AttendanceService attendanceService = new AttendanceService();
        CustomerCommunicationService commService = new CustomerCommunicationService();
        
        // 모든 회사 조회
        List<Company> companies = companyService.getAllCompanies();
        
        if (companies.isEmpty()) {
            System.out.println("  ⚠ 회사 데이터 없음. 생성 중단.");
            return;
        }
        
        // 각 회사별로 데이터 생성
        for (Company company : companies) {
            System.out.println("\n=== " + company.getName() + " 1년치 데이터 생성 ===");
            AppContext.getInstance().setCurrentCompany(company);
            generateCompanyYearlyData(company, developerService, issueService, attendanceService, commService);
        }
        
        // 원래 선택된 회사로 복원
        AppContext.getInstance().setCurrentCompany(originalCompany);
        System.out.println("  원래 회사로 복원: " + (originalCompany != null ? originalCompany.getName() : "없음"));
        
        System.out.println("\n=== 전체 데이터 생성 완료 ===");
    }
    
    /**
     * 회사별 1년치 데이터 생성
     */
    private static void generateCompanyYearlyData(Company company, 
                                                  DeveloperService devService,
                                                  IssueService issueService,
                                                  AttendanceService attService,
                                                  CustomerCommunicationService commService) {
        
        // 해당 회사의 개발자 목록 가져오기
        var developers = devService.getAllDevelopers();
        
        if (developers.isEmpty()) {
            System.out.println("  ⚠ 개발자 없음. 데이터 생성 스킵.");
            return;
        }
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);
        
        // 1. 근태 데이터 생성 (1년치)
        generateYearlyAttendance(developers, attService, startDate, endDate);
        
        // 2. 이슈 데이터 생성 (월 평균 10개)
        generateYearlyIssues(developers, issueService, startDate, endDate);
        
        // 3. 고객 소통 데이터 생성 (월 평균 8개)
        generateYearlyCommunications(developers, commService, startDate, endDate);
    }
    
    /**
     * 1년치 근태 데이터 생성
     */
    private static void generateYearlyAttendance(java.util.List<Developer> developers,
                                                 AttendanceService attService,
                                                 LocalDate startDate,
                                                 LocalDate endDate) {
        System.out.println("  근태 데이터 생성 중...");
        int count = 0;
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            // 주말 제외
            if (current.getDayOfWeek().getValue() < 6) {
                for (Developer dev : developers) {
                    // 90% 확률로 정상 출근
                    if (random.nextInt(100) < 90) {
                        LocalTime checkIn = LocalTime.of(9, 0).plusMinutes(random.nextInt(30));
                        LocalTime checkOut = LocalTime.of(18, 0).plusMinutes(random.nextInt(60));
                        
                        String status = checkIn.isAfter(LocalTime.of(9, 10)) ? "LATE" : "PRESENT";
                        
                        attService.createAttendance(
                            dev.getId(),
                            dev.getName(),
                            current,
                            checkIn,
                            checkOut,
                            status,
                            ""
                        );
                        count++;
                    } else {
                        // 10% 확률로 휴가/병가
                        String status = random.nextBoolean() ? "VACATION" : "SICK";
                        attService.createAttendance(
                            dev.getId(),
                            dev.getName(),
                            current,
                            null,
                            null,
                            status,
                            status.equals("VACATION") ? "연차" : "병가"
                        );
                        count++;
                    }
                }
            }
            current = current.plusDays(1);
        }
        
        System.out.println("    → 근태 " + count + "건 생성");
    }
    
    /**
     * 1년치 이슈 데이터 생성
     */
    private static void generateYearlyIssues(java.util.List<Developer> developers,
                                            IssueService issueService,
                                            LocalDate startDate,
                                            LocalDate endDate) {
        System.out.println("  이슈 데이터 생성 중...");
        int count = 0;
        
        String[] categories = {"버그", "기능", "개선", "성능"};
        String[] severities = {"긴급", "높음", "보통", "낮음"};
        String[] titles = {
            "로그인 오류 수정",
            "API 응답 지연 개선",
            "UI 버튼 정렬 수정",
            "데이터베이스 쿼리 최적화",
            "파일 업로드 기능 추가",
            "검색 기능 개선",
            "알림 기능 버그 수정",
            "성능 모니터링 추가",
            "보안 취약점 패치",
            "메모리 누수 수정"
        };
        
        // 월별로 8~12개 이슈 생성
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int issuesPerMonth = 8 + random.nextInt(5);
            
            for (int i = 0; i < issuesPerMonth; i++) {
                Developer assignee = developers.get(random.nextInt(developers.size()));
                String title = titles[random.nextInt(titles.length)];
                String category = categories[random.nextInt(categories.length)];
                String severity = severities[random.nextInt(severities.length)];
                
                issueService.createIssue(
                    title + " #" + count,
                    "상세 내용: " + title,
                    category,
                    severity,
                    "고객사",
                    assignee.getName(),
                    ""
                );
                count++;
            }
            
            current = current.plusMonths(1);
        }
        
        System.out.println("    → 이슈 " + count + "건 생성");
    }
    
    /**
     * 1년치 고객 소통 데이터 생성
     */
    private static void generateYearlyCommunications(java.util.List<Developer> developers,
                                                    CustomerCommunicationService commService,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        System.out.println("  고객 소통 데이터 생성 중...");
        int count = 0;
        
        String[] types = {"전화", "이메일", "회의", "메신저"};
        String[] customers = {"김부장", "이과장", "박대리", "최팀장"};
        String[] subjects = {
            "프로젝트 일정 협의",
            "요구사항 변경 논의",
            "버그 수정 요청",
            "배포 일정 조율",
            "성능 개선 논의",
            "추가 기능 문의",
            "장애 대응 보고",
            "정기 점검 회의"
        };
        
        // 월별로 6~10개 소통 기록 생성
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int commsPerMonth = 6 + random.nextInt(5);
            
            for (int i = 0; i < commsPerMonth; i++) {
                Developer dev = developers.get(random.nextInt(developers.size()));
                String type = types[random.nextInt(types.length)];
                String customer = customers[random.nextInt(customers.length)];
                String subject = subjects[random.nextInt(subjects.length)];
                
                LocalDateTime commDate = current.plusDays(random.nextInt(28)).atTime(
                    9 + random.nextInt(9), random.nextInt(60)
                );
                
                String status = random.nextBoolean() ? "완료" : "대기";
                LocalDateTime dueDate = commDate.plusDays(random.nextInt(7));
                
                commService.createCommunication(
                    type,
                    subject,
                    "상세 내용: " + subject,
                    customer,
                    dev.getName(),
                    commDate,
                    "보통",
                    dueDate,
                    ""
                );
                count++;
            }
            
            current = current.plusMonths(1);
        }
        
        System.out.println("    → 고객 소통 " + count + "건 생성");
    }
}

