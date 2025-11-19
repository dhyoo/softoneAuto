package com.softone.auto.util;

import com.softone.auto.service.*;

/**
 * 서비스 레지스트리
 * 
 * 애플리케이션 전역에서 사용되는 Service 인스턴스를 중앙에서 관리합니다.
 * 싱글톤 패턴을 사용하여 동일한 Service 인스턴스를 재사용합니다.
 * 
 * <p>사용 예:</p>
 * <pre>
 * DeveloperService developerService = ServiceRegistry.getDeveloperService();
 * </pre>
 * 
 * <p><b>주의:</b> 현재는 간단한 싱글톤 방식이지만, 향후 DI 프레임워크(Spring, Guice 등)로 전환 가능합니다.</p>
 */
public class ServiceRegistry {
    
    private static DeveloperService developerService;
    private static AttendanceService attendanceService;
    private static IssueService issueService;
    private static CustomerCommunicationService communicationService;
    private static WeeklyReportService weeklyReportService;
    private static CompanyService companyService;
    private static CommonCodeService commonCodeService;
    
    /**
     * 개발자 관리 서비스 가져오기
     */
    public static synchronized DeveloperService getDeveloperService() {
        if (developerService == null) {
            developerService = new DeveloperService();
        }
        return developerService;
    }
    
    /**
     * 근태 관리 서비스 가져오기
     */
    public static synchronized AttendanceService getAttendanceService() {
        if (attendanceService == null) {
            attendanceService = new AttendanceService();
        }
        return attendanceService;
    }
    
    /**
     * 이슈 관리 서비스 가져오기
     */
    public static synchronized IssueService getIssueService() {
        if (issueService == null) {
            issueService = new IssueService();
        }
        return issueService;
    }
    
    /**
     * 고객 소통 관리 서비스 가져오기
     */
    public static synchronized CustomerCommunicationService getCustomerCommunicationService() {
        if (communicationService == null) {
            communicationService = new CustomerCommunicationService();
        }
        return communicationService;
    }
    
    /**
     * 주간보고서 서비스 가져오기
     */
    public static synchronized WeeklyReportService getWeeklyReportService() {
        if (weeklyReportService == null) {
            weeklyReportService = new WeeklyReportService();
        }
        return weeklyReportService;
    }
    
    /**
     * 회사 관리 서비스 가져오기
     */
    public static synchronized CompanyService getCompanyService() {
        if (companyService == null) {
            companyService = new CompanyService();
        }
        return companyService;
    }
    
    /**
     * 공통코드 서비스 가져오기
     */
    public static synchronized CommonCodeService getCommonCodeService() {
        if (commonCodeService == null) {
            commonCodeService = new CommonCodeService();
        }
        return commonCodeService;
    }
    
    /**
     * 모든 서비스 인스턴스 초기화 (테스트용 또는 재시작 시)
     */
    public static synchronized void reset() {
        developerService = null;
        attendanceService = null;
        issueService = null;
        communicationService = null;
        weeklyReportService = null;
        companyService = null;
        commonCodeService = null;
    }
}

