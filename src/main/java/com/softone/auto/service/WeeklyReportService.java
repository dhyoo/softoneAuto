package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.model.Developer;
import com.softone.auto.model.WeeklyReport;
import com.softone.auto.repository.sqlite.WeeklyReportSqliteRepository;
import com.softone.auto.util.AuditLogger;
import com.softone.auto.util.PrivacyMaskingUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 주간 보고서 서비스
 */
@Slf4j
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
        try {
            Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
            String companyName = currentCompany != null ? PrivacyMaskingUtil.maskName(currentCompany.getName()) : "없음";
            log.debug("주간보고서 목록 조회 시작 - 회사: {}", companyName);
            
            List<WeeklyReport> allReports;
            if (currentCompany != null) {
                // 회사별 필터링은 SQLite에서 직접 처리
                allReports = repository.findByCompanyId(currentCompany.getId());
                log.debug("회사별 보고서: {}건", allReports.size());
            } else {
                // 전체 조회
                allReports = repository.findAll();
                log.debug("전체 보고서: {}건", allReports.size());
            }
            
            log.debug("주간보고서 목록 조회 완료 - 회사: {}, 조회된 보고서 수: {}건", companyName, allReports.size());
            AuditLogger.logDataAccess("SYSTEM", "READ", "WeeklyReport", String.valueOf(allReports.size()));
            
            return allReports;
        } catch (Exception e) {
            log.error("주간보고서 데이터 조회 오류: {}", e.getMessage(), e);
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
        
        // 감사 로그 기록 (개인정보 마스킹)
        String maskedReporter = PrivacyMaskingUtil.maskName(reporter);
        String maskedProjectName = PrivacyMaskingUtil.maskName(projectName);
        log.info("주간보고서 생성 완료 - 제목: {}, 작성자: {}, 프로젝트: {}", 
            title, maskedReporter, maskedProjectName);
        AuditLogger.logDataModification("SYSTEM", "CREATE", "WeeklyReport", report.getId(), 
            "제목: " + title + ", 작성자: " + maskedReporter);
        
        return report;
    }
    
    /**
     * 주간 보고서 수정
     */
    public void updateReport(WeeklyReport report) {
        String maskedReporter = report.getReporter() != null ? 
            PrivacyMaskingUtil.maskName(report.getReporter()) : "N/A";
        log.info("주간보고서 수정 완료 - ID: {}, 제목: {}, 작성자: {}", 
            report.getId(), report.getTitle(), maskedReporter);
        
        repository.update(report);
        
        AuditLogger.logDataModification("SYSTEM", "UPDATE", "WeeklyReport", report.getId(), 
            "제목: " + report.getTitle() + ", 작성자: " + maskedReporter);
    }
    
    /**
     * 주간 보고서 삭제
     */
    public void deleteReport(String id) {
        log.info("주간보고서 삭제 - ID: {}", id);
        repository.delete(id);
        AuditLogger.logDataModification("SYSTEM", "DELETE", "WeeklyReport", id, null);
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

