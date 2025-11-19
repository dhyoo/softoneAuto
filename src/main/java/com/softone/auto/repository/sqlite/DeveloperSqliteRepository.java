package com.softone.auto.repository.sqlite;

import com.softone.auto.model.Developer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 개발자 정보 저장소 (SQLite)
 */
@Slf4j
public class DeveloperSqliteRepository {
    
    private final Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public DeveloperSqliteRepository() {
        try {
            // Connection Pool 사용 (읽기는 Pool, 쓰기는 단일 연결)
            this.connection = SqliteConnectionPool.getInstance().getWriteConnection();
            
            createTable();
            checkIntegrity();
            
        } catch (SQLException e) {
            log.error("SQLite 연결 실패", e);
            e.printStackTrace();
            throw new RuntimeException("데이터베이스 연결 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("DeveloperSqliteRepository 초기화 실패", e);
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS developers (
                id TEXT PRIMARY KEY,
                company_id TEXT NOT NULL,
                name TEXT NOT NULL,
                position TEXT,
                role TEXT,
                team TEXT,
                email TEXT,
                phone TEXT,
                emergency_phone TEXT,
                join_date TEXT,
                status TEXT NOT NULL DEFAULT 'ACTIVE',
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
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_developers_company_id ON developers(company_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_developers_status ON developers(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_developers_name ON developers(name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_developers_company_status ON developers(company_id, status)");
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
    
    public List<Developer> findAll() {
        List<Developer> results = new ArrayList<>();
        String sql = "SELECT * FROM developers";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
        } catch (SQLException e) {
            log.error("개발자 목록 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public Optional<Developer> findById(String id) {
        String sql = "SELECT * FROM developers WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("개발자 조회 실패: {}", id, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    public Optional<Developer> findByName(String name) {
        String sql = "SELECT * FROM developers WHERE name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("개발자 조회 실패: {}", name, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    public void save(Developer developer) {
        String sql = """
            INSERT OR REPLACE INTO developers 
            (id, company_id, name, position, role, team, email, phone, emergency_phone, 
             join_date, status, notes, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, developer.getId());
            stmt.setString(2, developer.getCompanyId());
            stmt.setString(3, developer.getName());
            stmt.setString(4, developer.getPosition());
            stmt.setString(5, developer.getRole());
            stmt.setString(6, developer.getTeam());
            stmt.setString(7, developer.getEmail());
            stmt.setString(8, developer.getPhone());
            stmt.setString(9, developer.getEmergencyPhone());
            stmt.setString(10, formatDate(developer.getJoinDate()));
            stmt.setString(11, developer.getStatus() != null ? developer.getStatus() : "ACTIVE");
            stmt.setString(12, developer.getNotes());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("개발자 저장 실패", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    public void update(Developer developer) {
        String sql = """
            UPDATE developers SET
                company_id = ?, name = ?, position = ?, role = ?, team = ?,
                email = ?, phone = ?, emergency_phone = ?, join_date = ?,
                status = ?, notes = ?, updated_at = datetime('now', 'localtime')
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, developer.getCompanyId());
            stmt.setString(2, developer.getName());
            stmt.setString(3, developer.getPosition());
            stmt.setString(4, developer.getRole());
            stmt.setString(5, developer.getTeam());
            stmt.setString(6, developer.getEmail());
            stmt.setString(7, developer.getPhone());
            stmt.setString(8, developer.getEmergencyPhone());
            stmt.setString(9, formatDate(developer.getJoinDate()));
            stmt.setString(10, developer.getStatus());
            stmt.setString(11, developer.getNotes());
            stmt.setString(12, developer.getId());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("개발자 업데이트 실패", e);
            throw new RuntimeException("데이터 업데이트 실패", e);
        }
    }
    
    public void delete(String id) {
        String sql = "DELETE FROM developers WHERE id = ?";
        
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
            log.error("개발자 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    private Developer mapRow(ResultSet rs) throws SQLException {
        Developer developer = new Developer();
        developer.setId(rs.getString("id"));
        developer.setCompanyId(rs.getString("company_id"));
        developer.setName(rs.getString("name"));
        developer.setPosition(rs.getString("position"));
        developer.setRole(rs.getString("role"));
        developer.setTeam(rs.getString("team"));
        developer.setEmail(rs.getString("email"));
        developer.setPhone(rs.getString("phone"));
        developer.setEmergencyPhone(rs.getString("emergency_phone"));
        developer.setJoinDate(parseDate(rs.getString("join_date")));
        developer.setStatus(rs.getString("status"));
        developer.setNotes(rs.getString("notes"));
        return developer;
    }
    
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
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
    
    public void close() {
        // Connection은 SqliteConnectionPool이 관리하므로 여기서 닫지 않음
        // 필요시 SqliteConnectionPool.getInstance().closeAllConnections() 호출
    }
}

