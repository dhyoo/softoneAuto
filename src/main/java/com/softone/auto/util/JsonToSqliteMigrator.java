package com.softone.auto.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.softone.auto.model.*;
// Deprecated repository 패키지 제거됨 - 직접 SqliteRepository 사용
import com.softone.auto.repository.sqlite.*;
import com.softone.auto.repository.sqlite.IssueSqliteRepository;
import com.softone.auto.repository.sqlite.CustomerCommunicationSqliteRepository;
import com.softone.auto.repository.sqlite.CommonCodeSqliteRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 데이터를 SQLite로 마이그레이션하는 유틸리티
 * 
 * <p><b>주의:</b> 이 클래스는 일회성/관리자용 마이그레이션 도구입니다.</p>
 * <p>일반적인 애플리케이션 실행 시에는 사용되지 않으며,</p>
 * <p>기존 JSON 데이터를 SQLite로 전환할 때만 사용됩니다.</p>
 * 
 * <p>마이그레이션 완료 후에는 이 클래스를 사용할 필요가 없습니다.</p>
 * 
 * @deprecated 마이그레이션 완료 후 제거 예정 (선택적)
 */
@Slf4j
public class JsonToSqliteMigrator {
    
    private final Gson gson;
    
    public JsonToSqliteMigrator() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new com.softone.auto.repository.legacy.LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new com.softone.auto.repository.legacy.LocalDateTimeAdapter())
                .registerTypeAdapter(LocalTime.class, new com.softone.auto.repository.legacy.LocalTimeAdapter())
                .create();
    }
    
    /**
     * 모든 JSON 데이터를 SQLite로 마이그레이션
     */
    public void migrateAll() {
        System.out.println("=== JSON → SQLite 마이그레이션 시작 ===");
        
        try {
            // 1. 회사 데이터
            migrateCompanies();
            
            // 2. 개발자 데이터
            migrateDevelopers();
            
            // 3. 근태 데이터
            migrateAttendances();
            
            // 4. 이슈 데이터
            migrateIssues();
            
            // 5. 고객소통 데이터
            migrateCustomerCommunications();
            
            // 6. 공통코드 데이터
            migrateCommonCodes();
            
            System.out.println("=== 마이그레이션 완료 ===\n");
            
        } catch (Exception e) {
            log.error("마이그레이션 실패", e);
            throw new RuntimeException("마이그레이션 실패", e);
        }
    }
    
    /**
     * 회사 데이터 마이그레이션
     */
    private void migrateCompanies() {
        System.out.println("회사 데이터 마이그레이션 중...");
        
        try {
            List<Company> companies = loadJsonFile("data", "companies.json", 
                new TypeToken<ArrayList<Company>>(){}.getType());
            
            if (companies.isEmpty()) {
                System.out.println("  회사 데이터 없음");
                return;
            }
            
            CompanySqliteRepository repo = new CompanySqliteRepository();
            try {
                for (Company company : companies) {
                    repo.save(company);
                }
                System.out.println("  ✓ 회사 데이터 마이그레이션 완료: " + companies.size() + "건");
            } finally {
                repo.close();
            }
            
        } catch (Exception e) {
            System.err.println("  ✗ 회사 데이터 마이그레이션 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 개발자 데이터 마이그레이션
     */
    private void migrateDevelopers() {
        System.out.println("개발자 데이터 마이그레이션 중...");
        
        try {
            List<Developer> developers = loadJsonFile("data", "developers.json",
                new TypeToken<ArrayList<Developer>>(){}.getType());
            
            if (developers.isEmpty()) {
                System.out.println("  개발자 데이터 없음");
                return;
            }
            
            DeveloperSqliteRepository repo = new DeveloperSqliteRepository();
            try {
                for (Developer developer : developers) {
                    repo.save(developer);
                }
                System.out.println("  ✓ 개발자 데이터 마이그레이션 완료: " + developers.size() + "건");
            } finally {
                repo.close();
            }
            
        } catch (Exception e) {
            System.err.println("  ✗ 개발자 데이터 마이그레이션 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 근태 데이터 마이그레이션
     */
    private void migrateAttendances() {
        System.out.println("근태 데이터 마이그레이션 중...");
        
        try {
            List<Attendance> attendances = loadJsonFile("data", "attendance.json",
                new TypeToken<ArrayList<Attendance>>(){}.getType());
            
            if (attendances.isEmpty()) {
                System.out.println("  근태 데이터 없음");
                return;
            }
            
            AttendanceSqliteRepository repo = new AttendanceSqliteRepository();
            try {
                for (Attendance attendance : attendances) {
                    repo.save(attendance);
                }
                System.out.println("  ✓ 근태 데이터 마이그레이션 완료: " + attendances.size() + "건");
            } finally {
                repo.close();
            }
            
        } catch (Exception e) {
            System.err.println("  ✗ 근태 데이터 마이그레이션 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 이슈 데이터 마이그레이션
     */
    private void migrateIssues() {
        System.out.println("이슈 데이터 마이그레이션 중...");
        
        try {
            List<Issue> issues = loadJsonFile("data", "issues.json",
                new TypeToken<ArrayList<Issue>>(){}.getType());
            
            if (issues.isEmpty()) {
                System.out.println("  이슈 데이터 없음");
                return;
            }
            
            IssueSqliteRepository repo = new IssueSqliteRepository();
            try {
                for (Issue issue : issues) {
                    repo.save(issue);
                }
                System.out.println("  ✓ 이슈 데이터 마이그레이션 완료: " + issues.size() + "건");
            } finally {
                repo.close();
            }
            
        } catch (Exception e) {
            System.err.println("  ✗ 이슈 데이터 마이그레이션 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 고객소통 데이터 마이그레이션
     */
    private void migrateCustomerCommunications() {
        System.out.println("고객소통 데이터 마이그레이션 중...");
        
        try {
            List<CustomerCommunication> communications = loadJsonFile("data", "customer_communications.json",
                new TypeToken<ArrayList<CustomerCommunication>>(){}.getType());
            
            if (communications.isEmpty()) {
                System.out.println("  고객소통 데이터 없음");
                return;
            }
            
            CustomerCommunicationSqliteRepository repo = new CustomerCommunicationSqliteRepository();
            try {
                for (CustomerCommunication comm : communications) {
                    repo.save(comm);
                }
                System.out.println("  ✓ 고객소통 데이터 마이그레이션 완료: " + communications.size() + "건");
            } finally {
                repo.close();
            }
            
        } catch (Exception e) {
            System.err.println("  ✗ 고객소통 데이터 마이그레이션 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 공통코드 데이터 마이그레이션
     */
    private void migrateCommonCodes() {
        System.out.println("공통코드 데이터 마이그레이션 중...");
        
        try {
            List<CommonCode> codes = loadJsonFile("data", "common_codes.json",
                new TypeToken<ArrayList<CommonCode>>(){}.getType());
            
            if (codes.isEmpty()) {
                System.out.println("  공통코드 데이터 없음");
                return;
            }
            
            CommonCodeSqliteRepository repo = new CommonCodeSqliteRepository();
            try {
                for (CommonCode code : codes) {
                    repo.save(code);
                }
                System.out.println("  ✓ 공통코드 데이터 마이그레이션 완료: " + codes.size() + "건");
            } finally {
                repo.close();
            }
            
        } catch (Exception e) {
            System.err.println("  ✗ 공통코드 데이터 마이그레이션 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * JSON 파일 로드
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> loadJsonFile(String dataDir, String fileName, Type type) {
        try {
            String basePath = DataPathManager.getDataPath();
            String filePath = basePath + File.separator + dataDir + File.separator + fileName;
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("  파일 없음: " + filePath);
                return new ArrayList<>();
            }
            
            try (FileReader reader = new FileReader(file)) {
                List<T> list = gson.fromJson(reader, type);
                return list != null ? list : new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("JSON 파일 로드 실패: {}", fileName, e);
            return new ArrayList<>();
        }
    }
}

