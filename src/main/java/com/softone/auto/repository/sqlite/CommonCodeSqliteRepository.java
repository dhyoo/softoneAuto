package com.softone.auto.repository.sqlite;

import com.softone.auto.model.CommonCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 공통코드 저장소 (SQLite)
 */
@Slf4j
public class CommonCodeSqliteRepository {
    
    private final Connection connection;
    
    public CommonCodeSqliteRepository() {
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
            log.error("CommonCodeSqliteRepository 초기화 실패", e);
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS common_codes (
                id TEXT PRIMARY KEY,
                category TEXT NOT NULL,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                sort_order INTEGER DEFAULT 0,
                is_active INTEGER DEFAULT 1,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                updated_at TEXT DEFAULT (datetime('now', 'localtime')),
                UNIQUE(category, code)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_common_codes_category ON common_codes(category)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_common_codes_is_active ON common_codes(is_active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_common_codes_category_active ON common_codes(category, is_active)");
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
    
    public List<CommonCode> findAll() {
        List<CommonCode> results = new ArrayList<>();
        String sql = "SELECT * FROM common_codes";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
        } catch (SQLException e) {
            log.error("공통코드 목록 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public CommonCode findById(String id) {
        String sql = "SELECT * FROM common_codes WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            
        } catch (SQLException e) {
            log.error("공통코드 조회 실패: {}", id, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return null;
    }
    
    public List<CommonCode> findByCategory(String category) {
        String sql = "SELECT * FROM common_codes WHERE category = ? AND is_active = 1 ORDER BY sort_order";
        List<CommonCode> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("카테고리별 공통코드 조회 실패: {}", category, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public void save(CommonCode code) {
        String sql = """
            INSERT OR REPLACE INTO common_codes 
            (id, category, code, name, description, sort_order, is_active, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code.getId());
            stmt.setString(2, code.getCategory());
            stmt.setString(3, code.getCode());
            stmt.setString(4, code.getName());
            stmt.setString(5, code.getDescription());
            stmt.setObject(6, code.getSortOrder());
            stmt.setInt(7, toInt(code.getIsActive()));
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("공통코드 저장 실패", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    public void update(CommonCode code) {
        String sql = """
            UPDATE common_codes SET
                category = ?, code = ?, name = ?, description = ?,
                sort_order = ?, is_active = ?, updated_at = datetime('now', 'localtime')
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code.getCategory());
            stmt.setString(2, code.getCode());
            stmt.setString(3, code.getName());
            stmt.setString(4, code.getDescription());
            stmt.setObject(5, code.getSortOrder());
            stmt.setInt(6, toInt(code.getIsActive()));
            stmt.setString(7, code.getId());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("공통코드 업데이트 실패", e);
            throw new RuntimeException("데이터 업데이트 실패", e);
        }
    }
    
    public void delete(String id) {
        String sql = "DELETE FROM common_codes WHERE id = ?";
        
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
            log.error("공통코드 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    private CommonCode mapRow(ResultSet rs) throws SQLException {
        CommonCode code = new CommonCode();
        code.setId(rs.getString("id"));
        code.setCategory(rs.getString("category"));
        code.setCode(rs.getString("code"));
        code.setName(rs.getString("name"));
        code.setDescription(rs.getString("description"));
        code.setSortOrder(rs.getInt("sort_order"));
        code.setIsActive(toBoolean(rs.getInt("is_active")));
        return code;
    }
    
    private int toInt(Boolean value) {
        return (value != null && value) ? 1 : 0;
    }
    
    private Boolean toBoolean(int value) {
        return value == 1;
    }
    
    public void close() {
        // 공유 Connection은 SqliteConnectionManager가 관리하므로 여기서 닫지 않음
        // 필요시 SqliteConnectionManager.getInstance().close() 호출
    }
}

