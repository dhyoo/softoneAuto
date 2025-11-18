package com.softone.auto.repository.sqlite;

import com.softone.auto.model.Company;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 회사 데이터 저장소 (SQLite)
 */
@Slf4j
public class CompanySqliteRepository {
    
    private final Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public CompanySqliteRepository() {
        try {
            // 공유 Connection 사용
            this.connection = SqliteConnectionManager.getInstance().getConnection();
            
            createTable();
            checkIntegrity();
            
        } catch (SQLException e) {
            log.error("SQLite 연결 실패", e);
            e.printStackTrace();
            throw new RuntimeException("데이터베이스 연결 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("CompanySqliteRepository 초기화 실패", e);
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 테이블 생성
     */
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS companies (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                project_name TEXT,
                contract_type TEXT,
                start_date TEXT,
                end_date TEXT,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
                notes TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                updated_at TEXT DEFAULT (datetime('now', 'localtime'))
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        // 인덱스 생성
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_companies_status ON companies(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name)");
        }
        
        connection.commit();
    }
    
    /**
     * 무결성 검사
     */
    private void checkIntegrity() {
        try {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA quick_check")) {
                if (rs.next() && !"ok".equals(rs.getString(1).toLowerCase())) {
                    log.warn("데이터베이스 무결성 오류 감지");
                }
            }
        } catch (SQLException e) {
            log.error("무결성 검사 실패", e);
        }
    }
    
    /**
     * 모든 회사 조회
     */
    public List<Company> findAll() {
        System.out.println("  [CompanySqliteRepository.findAll] 회사 목록 조회 시작");
        List<Company> results = new ArrayList<>();
        String sql = "SELECT * FROM companies";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
            System.out.println("    → 조회된 회사 수: " + results.size());
            for (Company c : results) {
                System.out.println("      - " + c.getName() + " (ID: " + c.getId() + ")");
            }
            
        } catch (SQLException e) {
            System.err.println("    ✗ 회사 목록 조회 실패: " + e.getMessage());
            log.error("회사 목록 조회 실패", e);
            e.printStackTrace();
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        System.out.println("  [CompanySqliteRepository.findAll] 회사 목록 조회 완료");
        return results;
    }
    
    /**
     * 활성 회사만 조회
     */
    public List<Company> findAllActive() {
        System.out.println("  [CompanySqliteRepository.findAllActive] 활성 회사 조회 시작");
        String sql = "SELECT * FROM companies WHERE status = 'ACTIVE'";
        List<Company> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
            System.out.println("    → 조회된 활성 회사 수: " + results.size());
            for (Company c : results) {
                System.out.println("      - " + c.getName() + " (ID: " + c.getId() + ")");
            }
            
        } catch (SQLException e) {
            System.err.println("    ✗ 활성 회사 조회 실패: " + e.getMessage());
            log.error("활성 회사 조회 실패", e);
            e.printStackTrace();
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        System.out.println("  [CompanySqliteRepository.findAllActive] 활성 회사 조회 완료");
        return results;
    }
    
    /**
     * ID로 회사 조회
     */
    public Optional<Company> findById(String id) {
        String sql = "SELECT * FROM companies WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("회사 조회 실패: {}", id, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 이름으로 회사 조회
     */
    public Optional<Company> findByName(String name) {
        String sql = "SELECT * FROM companies WHERE name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("회사 조회 실패: {}", name, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 회사 저장
     */
    public Company save(Company company) {
        if (company.getId() == null || company.getId().isEmpty()) {
            company.setId(UUID.randomUUID().toString());
        }
        
        System.out.println("  [CompanySqliteRepository.save] 회사 저장 시작: " + company.getName());
        System.out.println("    ID: " + company.getId());
        System.out.println("    프로젝트: " + company.getProjectName());
        
        // 기존 회사인지 확인 (ID로 조회)
        Optional<Company> existingCompany = findById(company.getId());
        if (existingCompany.isPresent()) {
            // 기존 회사 수정인 경우: ID가 절대 변경되지 않도록 보장
            Company existing = existingCompany.get();
            System.out.println("    → 기존 회사 수정: " + existing.getName() + " (ID: " + existing.getId() + ")");
            
            // ID가 변경되었는지 확인 (절대 변경되면 안 됨)
            if (!existing.getId().equals(company.getId())) {
                System.err.println("    ✗ 치명적 오류: 회사 ID가 변경되었습니다!");
                System.err.println("      기존 ID: " + existing.getId());
                System.err.println("      새 ID: " + company.getId());
                throw new IllegalStateException("회사 ID는 변경할 수 없습니다. 기존 ID를 유지해야 합니다.");
            }
            
            // 기존 회사의 ID를 강제로 유지 (혹시 모를 변경 방지)
            company.setId(existing.getId());
            System.out.println("    → 회사 ID 검증 완료: " + company.getId());
        } else {
            System.out.println("    → 신규 회사 생성");
        }
        
        String sql = """
            INSERT OR REPLACE INTO companies 
            (id, name, project_name, contract_type, start_date, end_date, status, notes, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, company.getId());
            stmt.setString(2, company.getName());
            stmt.setString(3, company.getProjectName());
            stmt.setString(4, company.getContractType());
            stmt.setString(5, formatDate(company.getStartDate()));
            stmt.setString(6, formatDate(company.getEndDate()));
            stmt.setString(7, company.getStatus() != null ? company.getStatus() : "ACTIVE");
            stmt.setString(8, company.getNotes());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("    → SQL 실행 완료 (영향받은 행: " + rowsAffected + ")");
            
            connection.commit();
            System.out.println("    → 트랜잭션 커밋 완료");
            
            // 저장 후 즉시 확인 및 ID 검증
            Optional<Company> savedCompany = findById(company.getId());
            if (savedCompany.isPresent()) {
                Company saved = savedCompany.get();
                if (!saved.getId().equals(company.getId())) {
                    System.err.println("    ✗ 치명적 오류: 저장된 회사의 ID가 원래 ID와 다릅니다!");
                    throw new IllegalStateException("회사 ID가 변경되었습니다. 데이터 무결성 오류입니다.");
                }
                System.out.println("    → 저장 후 ID 검증 완료: " + saved.getId());
            }
            
            List<Company> all = findAll();
            System.out.println("    → 저장 후 전체 회사 수: " + all.size());
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                System.err.println("    ✗ 롤백 실행");
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            System.err.println("    ✗ 회사 저장 실패: " + e.getMessage());
            log.error("회사 저장 실패", e);
            e.printStackTrace();
            throw new RuntimeException("데이터 저장 실패", e);
        }
        
        System.out.println("  [CompanySqliteRepository.save] 회사 저장 완료: " + company.getName() + " (ID: " + company.getId() + ")");
        return company;
    }
    
    /**
     * 회사 삭제
     */
    public void deleteById(String id) {
        String sql = "DELETE FROM companies WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("회사 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    /**
     * ResultSet을 Company 객체로 변환
     */
    private Company mapRow(ResultSet rs) throws SQLException {
        Company company = new Company();
        company.setId(rs.getString("id"));
        company.setName(rs.getString("name"));
        company.setProjectName(rs.getString("project_name"));
        company.setContractType(rs.getString("contract_type"));
        company.setStartDate(parseDate(rs.getString("start_date")));
        company.setEndDate(parseDate(rs.getString("end_date")));
        company.setStatus(rs.getString("status"));
        company.setNotes(rs.getString("notes"));
        return company;
    }
    
    /**
     * 날짜를 TEXT로 변환
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    /**
     * TEXT를 날짜로 변환
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }
    
    /**
     * 연결 종료
     */
    public void close() {
        // 공유 Connection은 SqliteConnectionManager가 관리하므로 여기서 닫지 않음
        // 필요시 SqliteConnectionManager.getInstance().close() 호출
    }
}

