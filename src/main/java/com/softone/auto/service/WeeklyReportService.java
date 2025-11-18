package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.model.Developer;
import com.softone.auto.model.WeeklyReport;
import com.softone.auto.repository.sqlite.WeeklyReportSqliteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 주간 보고서 서비스
 */
public class WeeklyReportService {
    
    private final WeeklyReportSqliteRepository repository;
    private final AttendanceService attendanceService;
    private final DeveloperService developerService;
    
    public WeeklyReportService() {
        this.repository = new WeeklyReportSqliteRepository();
        this.attendanceService = new AttendanceService();
        this.developerService = new DeveloperService();
    }
    
    /**
     * 전체 주간 보고서 목록 조회 (회사별 필터링)
     */
    public List<WeeklyReport> getAllReports() {
        System.out.println("\n=== WeeklyReportService.getAllReports() 시작 ===");
        try {
            Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
            System.out.println("  현재 회사: " + (currentCompany != null ? currentCompany.getName() + " (ID: " + currentCompany.getId() + ")" : "없음"));
            
            List<WeeklyReport> allReports;
            if (currentCompany != null) {
                // 회사별 필터링은 SQLite에서 직접 처리
                allReports = repository.findByCompanyId(currentCompany.getId());
                System.out.println("  회사별 보고서: " + allReports.size() + "건");
            } else {
                // 전체 조회
                allReports = repository.findAll();
                System.out.println("  전체 보고서: " + allReports.size() + "건");
            }
            
            // 각 보고서의 회사 ID 출력
            if (!allReports.isEmpty()) {
                System.out.println("  보고서별 회사 ID:");
                for (WeeklyReport report : allReports) {
                    System.out.println("    - " + report.getStartDate() + " (회사ID: " + report.getCompanyId() + ", 제목: " + report.getTitle() + ")");
                }
            }
            
            System.out.println("=== getAllReports() 완료 ===\n");
            return allReports;
        } catch (Exception e) {
            System.err.println("  ✗ 주간보고서 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== getAllReports() 오류 발생 ===\n");
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 주간 보고서 생성 (현재 회사에 자동 할당)
     */
    public WeeklyReport createReport(String title, LocalDate startDate, LocalDate endDate, 
                                    String projectName, String reporter) {
        WeeklyReport report = new WeeklyReport();
        report.setId(UUID.randomUUID().toString());
        
        // 현재 선택된 회사 ID 자동 설정
        Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
        if (currentCompany != null) {
            report.setCompanyId(currentCompany.getId());
        }
        
        report.setTitle(title);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setProjectName(projectName);
        report.setReporter(reporter);
        report.setCreatedDate(LocalDate.now());
        
        // 근태 현황 자동 생성
        generateAttendanceSummaries(report, startDate, endDate);
        
        repository.save(report);
        return report;
    }
    
    /**
     * 주간 보고서 수정
     */
    public void updateReport(WeeklyReport report) {
        repository.update(report);
    }
    
    /**
     * 주간 보고서 삭제
     */
    public void deleteReport(String id) {
        repository.delete(id);
    }
    
    /**
     * ID로 보고서 찾기
     */
    public WeeklyReport getReportById(String id) {
        return repository.findById(id).orElse(null);
    }
    
    /**
     * 시작일로 보고서 존재 여부 확인 (현재 회사 기준)
     */
    public boolean existsByStartDate(LocalDate startDate) {
        Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            return false;
        }
        
        // 현재 회사의 보고서만 확인
        List<WeeklyReport> reports = repository.findByCompanyId(currentCompany.getId());
        return reports.stream()
                .anyMatch(report -> report.getStartDate().equals(startDate));
    }
    
    /**
     * 시작일로 보고서 찾기 (현재 회사 기준)
     */
    public WeeklyReport getReportByStartDate(LocalDate startDate) {
        Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            return null;
        }
        
        // 현재 회사의 보고서만 검색
        List<WeeklyReport> reports = repository.findByCompanyId(currentCompany.getId());
        return reports.stream()
                .filter(report -> report.getStartDate().equals(startDate))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 근태 현황 자동 생성
     */
    private void generateAttendanceSummaries(WeeklyReport report, LocalDate startDate, LocalDate endDate) {
        List<Developer> developers = developerService.getAllDevelopers();
        
        for (Developer dev : developers) {
            if ("ACTIVE".equals(dev.getStatus())) {
                WeeklyReport.AttendanceSummary summary = new WeeklyReport.AttendanceSummary();
                summary.setDeveloperName(dev.getName());
                summary.setWorkDays((int) attendanceService.getWorkDays(dev.getId(), startDate, endDate));
                summary.setLateDays((int) attendanceService.getLateDays(dev.getId(), startDate, endDate));
                summary.setVacationDays((int) attendanceService.getVacationDays(dev.getId(), startDate, endDate));
                
                report.getAttendanceSummaries().add(summary);
            }
        }
    }
}

