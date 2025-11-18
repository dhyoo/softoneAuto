package com.softone.auto.repository.sqlite;

import com.softone.auto.model.Issue;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 이슈 저장소 (SQLite)
 */
@Slf4j
public class IssueSqliteRepository {
    
    private final Connection connection;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public IssueSqliteRepository() {
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
            log.error("IssueSqliteRepository 초기화 실패", e);
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS issues (
                id TEXT PRIMARY KEY,
                company_id TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                category TEXT,
                severity TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'OPEN',
                reporter TEXT,
                assignee TEXT,
                created_date TEXT NOT NULL,
                updated_date TEXT,
                resolved_date TEXT,
                resolution TEXT,
                notes TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                updated_at TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_company_id ON issues(company_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_status ON issues(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_severity ON issues(severity)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_category ON issues(category)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_assignee ON issues(assignee)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_reporter ON issues(reporter)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_company_status ON issues(company_id, status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_issues_created_date ON issues(created_date)");
        }
        
        connection.commit();
    }
    
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
    
    public List<Issue> findAll() {
        List<Issue> results = new ArrayList<>();
        String sql = "SELECT * FROM issues";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
        } catch (SQLException e) {
            log.error("이슈 목록 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public Optional<Issue> findById(String id) {
        String sql = "SELECT * FROM issues WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("이슈 조회 실패: {}", id, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    public List<Issue> findByStatus(String status) {
        String sql = "SELECT * FROM issues WHERE status = ?";
        List<Issue> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("상태별 이슈 조회 실패: {}", status, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public List<Issue> findBySeverity(String severity) {
        String sql = "SELECT * FROM issues WHERE severity = ?";
        List<Issue> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, severity);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("심각도별 이슈 조회 실패: {}", severity, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public void save(Issue issue) {
        String sql = """
            INSERT OR REPLACE INTO issues 
            (id, company_id, title, description, category, severity, status, reporter, assignee,
             created_date, updated_date, resolved_date, resolution, notes, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, issue.getId());
            stmt.setString(2, issue.getCompanyId());
            stmt.setString(3, issue.getTitle());
            stmt.setString(4, issue.getDescription());
            stmt.setString(5, issue.getCategory());
            stmt.setString(6, issue.getSeverity() != null ? issue.getSeverity() : "보통");
            stmt.setString(7, issue.getStatus() != null ? issue.getStatus() : "OPEN");
            stmt.setString(8, issue.getReporter());
            stmt.setString(9, issue.getAssignee());
            // created_date는 NOT NULL이므로 null이면 현재 시간 사용
            String createdDate = formatDateTime(issue.getCreatedDate());
            if (createdDate == null) {
                createdDate = LocalDateTime.now().format(DATETIME_FORMATTER);
            }
            stmt.setString(10, createdDate);
            stmt.setString(11, formatDateTime(issue.getUpdatedDate()));
            stmt.setString(12, formatDateTime(issue.getResolvedDate()));
            stmt.setString(13, issue.getResolution());
            stmt.setString(14, issue.getNotes());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("이슈 저장 실패", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    public void update(Issue issue) {
        String sql = """
            UPDATE issues SET
                company_id = ?, title = ?, description = ?, category = ?, severity = ?,
                status = ?, reporter = ?, assignee = ?, created_date = ?,
                updated_date = ?, resolved_date = ?, resolution = ?, notes = ?,
                updated_at = datetime('now', 'localtime')
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, issue.getCompanyId());
            stmt.setString(2, issue.getTitle());
            stmt.setString(3, issue.getDescription());
            stmt.setString(4, issue.getCategory());
            stmt.setString(5, issue.getSeverity());
            stmt.setString(6, issue.getStatus());
            stmt.setString(7, issue.getReporter());
            stmt.setString(8, issue.getAssignee());
            stmt.setString(9, formatDateTime(issue.getCreatedDate()));
            stmt.setString(10, formatDateTime(issue.getUpdatedDate()));
            stmt.setString(11, formatDateTime(issue.getResolvedDate()));
            stmt.setString(12, issue.getResolution());
            stmt.setString(13, issue.getNotes());
            stmt.setString(14, issue.getId());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("이슈 업데이트 실패", e);
            throw new RuntimeException("데이터 업데이트 실패", e);
        }
    }
    
    public void delete(String id) {
        String sql = "DELETE FROM issues WHERE id = ?";
        
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
            log.error("이슈 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    private Issue mapRow(ResultSet rs) throws SQLException {
        Issue issue = new Issue();
        issue.setId(rs.getString("id"));
        issue.setCompanyId(rs.getString("company_id"));
        issue.setTitle(rs.getString("title"));
        issue.setDescription(rs.getString("description"));
        issue.setCategory(rs.getString("category"));
        issue.setSeverity(rs.getString("severity"));
        issue.setStatus(rs.getString("status"));
        issue.setReporter(rs.getString("reporter"));
        issue.setAssignee(rs.getString("assignee"));
        issue.setCreatedDate(parseDateTime(rs.getString("created_date")));
        issue.setUpdatedDate(parseDateTime(rs.getString("updated_date")));
        issue.setResolvedDate(parseDateTime(rs.getString("resolved_date")));
        issue.setResolution(rs.getString("resolution"));
        issue.setNotes(rs.getString("notes"));
        return issue;
    }
    
    private String formatDateTime(LocalDateTime datetime) {
        return datetime != null ? datetime.format(DATETIME_FORMATTER) : null;
    }
    
    private LocalDateTime parseDateTime(String datetimeStr) {
        if (datetimeStr == null || datetimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(datetimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            log.warn("날짜시간 파싱 실패: {}", datetimeStr);
            return null;
        }
    }
    
    public void close() {
        // 공유 Connection은 SqliteConnectionManager가 관리하므로 여기서 닫지 않음
        // 필요시 SqliteConnectionManager.getInstance().close() 호출
    }
}

