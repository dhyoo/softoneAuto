package com.softone.auto.repository.sqlite;

import com.softone.auto.model.Attendance;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 근태 정보 저장소 (SQLite)
 */
@Slf4j
public class AttendanceSqliteRepository {
    
    private final Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    
    public AttendanceSqliteRepository() {
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
            log.error("AttendanceSqliteRepository 초기화 실패", e);
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 테이블 생성
     */
    private void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS attendances (
                id TEXT PRIMARY KEY,
                company_id TEXT NOT NULL,
                developer_id TEXT NOT NULL,
                developer_name TEXT NOT NULL,
                date TEXT NOT NULL,
                check_in TEXT,
                check_out TEXT,
                type TEXT NOT NULL DEFAULT 'NORMAL',
                notes TEXT,
                work_minutes INTEGER,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                updated_at TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                FOREIGN KEY (developer_id) REFERENCES developers(id) ON DELETE CASCADE,
                UNIQUE(developer_id, date)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        // 인덱스 생성
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendances_company_id ON attendances(company_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendances_developer_id ON attendances(developer_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendances_date ON attendances(date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendances_type ON attendances(type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendances_company_date ON attendances(company_id, date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendances_developer_date ON attendances(developer_id, date)");
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
     * 전체 목록 조회
     */
    public List<Attendance> findAll() {
        List<Attendance> results = new ArrayList<>();
        String sql = "SELECT * FROM attendances";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            
        } catch (SQLException e) {
            log.error("근태 목록 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    /**
     * 특정 개발자의 근태 조회
     */
    public List<Attendance> findByDeveloperId(String developerId) {
        String sql = "SELECT * FROM attendances WHERE developer_id = ? ORDER BY date DESC";
        List<Attendance> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, developerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("개발자 근태 조회 실패: {}", developerId, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    /**
     * 특정 기간의 근태 조회
     */
    public List<Attendance> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM attendances WHERE date >= ? AND date <= ? ORDER BY date DESC";
        List<Attendance> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, formatDate(startDate));
            stmt.setString(2, formatDate(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("기간별 근태 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    /**
     * 특정 개발자의 특정 기간 근태 조회
     */
    public List<Attendance> findByDeveloperAndDateRange(String developerId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM attendances WHERE developer_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";
        List<Attendance> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, developerId);
            stmt.setString(2, formatDate(startDate));
            stmt.setString(3, formatDate(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            
        } catch (SQLException e) {
            log.error("개발자 기간별 근태 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    /**
     * 근태 저장
     */
    public void save(Attendance attendance) {
        String sql = """
            INSERT OR REPLACE INTO attendances 
            (id, company_id, developer_id, developer_name, date, check_in, check_out, type, notes, work_minutes, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, attendance.getId());
            stmt.setString(2, attendance.getCompanyId());
            stmt.setString(3, attendance.getDeveloperId());
            stmt.setString(4, attendance.getDeveloperName());
            stmt.setString(5, formatDate(attendance.getDate()));
            stmt.setString(6, formatTime(attendance.getCheckIn()));
            stmt.setString(7, formatTime(attendance.getCheckOut()));
            stmt.setString(8, attendance.getType() != null ? attendance.getType() : "NORMAL");
            stmt.setString(9, attendance.getNotes());
            stmt.setObject(10, attendance.getWorkMinutes());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("근태 저장 실패", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    /**
     * 근태 업데이트
     */
    public void update(Attendance attendance) {
        String sql = """
            UPDATE attendances SET
                company_id = ?, developer_id = ?, developer_name = ?, date = ?,
                check_in = ?, check_out = ?, type = ?, notes = ?, work_minutes = ?,
                updated_at = datetime('now', 'localtime')
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, attendance.getCompanyId());
            stmt.setString(2, attendance.getDeveloperId());
            stmt.setString(3, attendance.getDeveloperName());
            stmt.setString(4, formatDate(attendance.getDate()));
            stmt.setString(5, formatTime(attendance.getCheckIn()));
            stmt.setString(6, formatTime(attendance.getCheckOut()));
            stmt.setString(7, attendance.getType());
            stmt.setString(8, attendance.getNotes());
            stmt.setObject(9, attendance.getWorkMinutes());
            stmt.setString(10, attendance.getId());
            
            stmt.executeUpdate();
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("근태 업데이트 실패", e);
            throw new RuntimeException("데이터 업데이트 실패", e);
        }
    }
    
    /**
     * 근태 삭제
     */
    public void delete(String id) {
        String sql = "DELETE FROM attendances WHERE id = ?";
        
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
            log.error("근태 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    /**
     * ResultSet을 Attendance 객체로 변환
     */
    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getString("id"));
        attendance.setCompanyId(rs.getString("company_id"));
        attendance.setDeveloperId(rs.getString("developer_id"));
        attendance.setDeveloperName(rs.getString("developer_name"));
        attendance.setDate(parseDate(rs.getString("date")));
        attendance.setCheckIn(parseTime(rs.getString("check_in")));
        attendance.setCheckOut(parseTime(rs.getString("check_out")));
        attendance.setType(rs.getString("type"));
        attendance.setNotes(rs.getString("notes"));
        
        Object workMinutes = rs.getObject("work_minutes");
        if (workMinutes != null) {
            attendance.setWorkMinutes(rs.getInt("work_minutes"));
        }
        
        return attendance;
    }
    
    /**
     * 날짜를 TEXT로 변환
     */
    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    /**
     * 시간을 TEXT로 변환
     */
    private String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : null;
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
     * TEXT를 시간으로 변환
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("시간 파싱 실패: {}", timeStr);
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

