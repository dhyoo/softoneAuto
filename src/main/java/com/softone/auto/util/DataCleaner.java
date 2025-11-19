package com.softone.auto.util;

// Deprecated repository 패키지 제거됨 - 직접 SqliteRepository 사용
import com.softone.auto.service.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 샘플 데이터 삭제 및 재생성 유틸리티
 */
public class DataCleaner {
    
    /**
     * 모든 샘플 데이터 삭제
     */
    public static void deleteAllSampleData() {
        System.out.println("=== 샘플 데이터 삭제 시작 ===");
        
        try {
            // 주간보고서 삭제
            deleteWeeklyReports();
            
            // 근태 데이터 삭제
            deleteAttendance();
            
            // 이슈 데이터 삭제
            deleteIssues();
            
            // 고객 소통 데이터 삭제
            deleteCommunications();
            
            // 개발자 데이터 삭제
            deleteDevelopers();
            
            // 회사 데이터 삭제
            deleteCompanies();
            
            System.out.println("=== 샘플 데이터 삭제 완료 ===");
        } catch (Exception e) {
            System.err.println("샘플 데이터 삭제 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("샘플 데이터 삭제 실패", e);
        }
    }
    
    /**
     * 주간보고서 삭제
     */
    private static void deleteWeeklyReports() {
        try {
            System.out.println("주간보고서 삭제 중...");
            com.softone.auto.repository.sqlite.WeeklyReportSqliteRepository repository = new com.softone.auto.repository.sqlite.WeeklyReportSqliteRepository();
            var allReports = repository.findAll();
            
            for (var report : allReports) {
                try {
                    repository.delete(report.getId());
                } catch (Exception e) {
                    System.err.println("  보고서 삭제 실패 (ID: " + report.getId() + "): " + e.getMessage());
                }
            }
            
            // 파일 직접 삭제 시도
            try {
                String dataPath = AppConfig.getInstance().getOrSelectDataPath();
                Path reportsDir = Paths.get(dataPath, "weekly_reports");
                if (Files.exists(reportsDir)) {
                    deleteDirectory(reportsDir.toFile());
                    System.out.println("  주간보고서 디렉토리 삭제 완료");
                }
            } catch (Exception e) {
                System.err.println("  주간보고서 디렉토리 삭제 실패: " + e.getMessage());
            }
            
            System.out.println("  주간보고서 " + allReports.size() + "건 삭제 완료");
        } catch (Exception e) {
            System.err.println("주간보고서 삭제 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 근태 데이터 삭제
     */
    private static void deleteAttendance() {
        try {
            System.out.println("근태 데이터 삭제 중...");
            AttendanceService service = new AttendanceService();
            var allAttendance = service.getAllAttendance();
            
            for (var att : allAttendance) {
                try {
                    service.deleteAttendance(att.getId());
                } catch (Exception e) {
                    System.err.println("  근태 삭제 실패 (ID: " + att.getId() + "): " + e.getMessage());
                }
            }
            
            // 파일 직접 삭제 시도
            try {
                String dataPath = AppConfig.getInstance().getOrSelectDataPath();
                Path attendanceFile = Paths.get(dataPath, "attendance.json");
                if (Files.exists(attendanceFile)) {
                    Files.delete(attendanceFile);
                    System.out.println("  근태 파일 삭제 완료");
                }
            } catch (Exception e) {
                System.err.println("  근태 파일 삭제 실패: " + e.getMessage());
            }
            
            System.out.println("  근태 데이터 " + allAttendance.size() + "건 삭제 완료");
        } catch (Exception e) {
            System.err.println("근태 데이터 삭제 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 이슈 데이터 삭제
     */
    private static void deleteIssues() {
        try {
            System.out.println("이슈 데이터 삭제 중...");
            IssueService service = new IssueService();
            var allIssues = service.getAllIssues();
            
            for (var issue : allIssues) {
                try {
                    service.deleteIssue(issue.getId());
                } catch (Exception e) {
                    System.err.println("  이슈 삭제 실패 (ID: " + issue.getId() + "): " + e.getMessage());
                }
            }
            
            System.out.println("  이슈 데이터 " + allIssues.size() + "건 삭제 완료");
        } catch (Exception e) {
            System.err.println("이슈 데이터 삭제 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 고객 소통 데이터 삭제
     */
    private static void deleteCommunications() {
        try {
            System.out.println("고객 소통 데이터 삭제 중...");
            CustomerCommunicationService service = new CustomerCommunicationService();
            var allComms = service.getAllCommunications();
            
            for (var comm : allComms) {
                try {
                    service.deleteCommunication(comm.getId());
                } catch (Exception e) {
                    System.err.println("  소통 삭제 실패 (ID: " + comm.getId() + "): " + e.getMessage());
                }
            }
            
            System.out.println("  고객 소통 데이터 " + allComms.size() + "건 삭제 완료");
        } catch (Exception e) {
            System.err.println("고객 소통 데이터 삭제 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 개발자 데이터 삭제
     */
    private static void deleteDevelopers() {
        try {
            System.out.println("개발자 데이터 삭제 중...");
            DeveloperService service = new DeveloperService();
            var allDevelopers = service.getAllDevelopers();
            
            for (var dev : allDevelopers) {
                try {
                    service.deleteDeveloper(dev.getId());
                } catch (Exception e) {
                    System.err.println("  개발자 삭제 실패 (ID: " + dev.getId() + "): " + e.getMessage());
                }
            }
            
            System.out.println("  개발자 데이터 " + allDevelopers.size() + "건 삭제 완료");
        } catch (Exception e) {
            System.err.println("개발자 데이터 삭제 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 회사 데이터 삭제
     */
    private static void deleteCompanies() {
        try {
            System.out.println("회사 데이터 삭제 중...");
            CompanyService service = new CompanyService();
            var allCompanies = service.getAllCompanies();
            
            for (var company : allCompanies) {
                try {
                    service.deleteCompany(company.getId());
                } catch (Exception e) {
                    System.err.println("  회사 삭제 실패 (ID: " + company.getId() + "): " + e.getMessage());
                }
            }
            
            System.out.println("  회사 데이터 " + allCompanies.size() + "건 삭제 완료");
        } catch (Exception e) {
            System.err.println("회사 데이터 삭제 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 디렉토리 재귀 삭제
     */
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    /**
     * 샘플 데이터 삭제 후 재생성
     */
    public static void resetSampleData() {
        System.out.println("=== 샘플 데이터 리셋 시작 ===");
        
        try {
            // 1. 모든 샘플 데이터 삭제
            deleteAllSampleData();
            
            // 2. 잠시 대기 (파일 시스템 동기화)
            Thread.sleep(500);
            
            // 3. 샘플 데이터 재생성
            System.out.println("샘플 데이터 재생성 중...");
            SampleDataInitializer initializer = new SampleDataInitializer();
            initializer.initializeAllSampleData();
            
            System.out.println("=== 샘플 데이터 리셋 완료 ===");
        } catch (Exception e) {
            System.err.println("샘플 데이터 리셋 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("샘플 데이터 리셋 실패", e);
        }
    }
}

