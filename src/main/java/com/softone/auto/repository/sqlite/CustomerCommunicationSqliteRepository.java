package com.softone.auto.repository.sqlite;

import com.softone.auto.model.CustomerCommunication;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 고객소통 저장소 (SQLite)
 */
@Slf4j
public class CustomerCommunicationSqliteRepository {
    
    private final Connection connection;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public CustomerCommunicationSqliteRepository() {
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
            log.error("CustomerCommunicationSqliteRepository 초기화 실패", e);
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS customer_communications (
                id TEXT PRIMARY KEY,
                company_id TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
                customer_name TEXT,
                our_representative TEXT,
                communication_date TEXT,
                status TEXT NOT NULL DEFAULT 'PENDING',
                priority TEXT,
                due_date TEXT,
                completed_date TEXT,
                response TEXT,
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
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_communications_company_id ON customer_communications(company_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_communications_status ON customer_communications(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_communications_type ON customer_communications(type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_communications_priority ON customer_communications(priority)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_communications_company_status ON customer_communications(company_id, status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_communications_due_date ON customer_communications(due_date)");
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
    
    public List<CustomerCommunication> findAll() {
        List<CustomerCommunication> results = new ArrayList<>();
        String sql = "SELECT * FROM customer_communications";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
        } catch (SQLException e) {
            log.error("고객소통 목록 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public Optional<CustomerCommunication> findById(String id) {
        String sql = "SELECT * FROM customer_communications WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("고객소통 조회 실패: {}", id, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    public List<CustomerCommunication> findByType(String type) {
        String sql = "SELECT * FROM customer_communications WHERE type = ?";
        List<CustomerCommunication> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("유형별 고객소통 조회 실패: {}", type, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public List<CustomerCommunication> findByStatus(String status) {
        String sql = "SELECT * FROM customer_communications WHERE status = ?";
        List<CustomerCommunication> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("상태별 고객소통 조회 실패: {}", status, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public void save(CustomerCommunication communication) {
        String sql = """
            INSERT OR REPLACE INTO customer_communications 
            (id, company_id, type, title, content, customer_name, our_representative,
             communication_date, status, priority, due_date, completed_date, response, notes, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, communication.getId());
            stmt.setString(2, communication.getCompanyId());
            stmt.setString(3, communication.getType());
            stmt.setString(4, communication.getTitle());
            stmt.setString(5, communication.getContent());
            stmt.setString(6, communication.getCustomerName());
            stmt.setString(7, communication.getOurRepresentative());
            stmt.setString(8, formatDateTime(communication.getCommunicationDate()));
            stmt.setString(9, communication.getStatus() != null ? communication.getStatus() : "PENDING");
            stmt.setString(10, communication.getPriority());
            stmt.setString(11, formatDateTime(communication.getDueDate()));
            stmt.setString(12, formatDateTime(communication.getCompletedDate()));
            stmt.setString(13, communication.getResponse());
            stmt.setString(14, communication.getNotes());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("고객소통 저장 실패", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    public void update(CustomerCommunication communication) {
        String sql = """
            UPDATE customer_communications SET
                company_id = ?, type = ?, title = ?, content = ?, customer_name = ?,
                our_representative = ?, communication_date = ?, status = ?, priority = ?,
                due_date = ?, completed_date = ?, response = ?, notes = ?,
                updated_at = datetime('now', 'localtime')
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, communication.getCompanyId());
            stmt.setString(2, communication.getType());
            stmt.setString(3, communication.getTitle());
            stmt.setString(4, communication.getContent());
            stmt.setString(5, communication.getCustomerName());
            stmt.setString(6, communication.getOurRepresentative());
            stmt.setString(7, formatDateTime(communication.getCommunicationDate()));
            stmt.setString(8, communication.getStatus());
            stmt.setString(9, communication.getPriority());
            stmt.setString(10, formatDateTime(communication.getDueDate()));
            stmt.setString(11, formatDateTime(communication.getCompletedDate()));
            stmt.setString(12, communication.getResponse());
            stmt.setString(13, communication.getNotes());
            stmt.setString(14, communication.getId());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("고객소통 업데이트 실패", e);
            throw new RuntimeException("데이터 업데이트 실패", e);
        }
    }
    
    public void delete(String id) {
        String sql = "DELETE FROM customer_communications WHERE id = ?";
        
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
            log.error("고객소통 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    private CustomerCommunication mapRow(ResultSet rs) throws SQLException {
        CustomerCommunication comm = new CustomerCommunication();
        comm.setId(rs.getString("id"));
        comm.setCompanyId(rs.getString("company_id"));
        comm.setType(rs.getString("type"));
        comm.setTitle(rs.getString("title"));
        comm.setContent(rs.getString("content"));
        comm.setCustomerName(rs.getString("customer_name"));
        comm.setOurRepresentative(rs.getString("our_representative"));
        comm.setCommunicationDate(parseDateTime(rs.getString("communication_date")));
        comm.setStatus(rs.getString("status"));
        comm.setPriority(rs.getString("priority"));
        comm.setDueDate(parseDateTime(rs.getString("due_date")));
        comm.setCompletedDate(parseDateTime(rs.getString("completed_date")));
        comm.setResponse(rs.getString("response"));
        comm.setNotes(rs.getString("notes"));
        return comm;
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

