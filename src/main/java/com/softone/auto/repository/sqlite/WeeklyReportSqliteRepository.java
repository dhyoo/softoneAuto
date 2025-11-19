package com.softone.auto.repository.sqlite;

import com.softone.auto.model.WeeklyReport;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 주간보고서 저장소 (SQLite)
 */
@Slf4j
public class WeeklyReportSqliteRepository {
    
    private final Connection connection;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public WeeklyReportSqliteRepository() {
        try {
            // Connection Pool 사용 (읽기는 Pool, 쓰기는 단일 연결)
            this.connection = SqliteConnectionPool.getInstance().getWriteConnection();
            
            createTable();
            checkIntegrity();
            
        } catch (SQLException e) {
            log.error("WeeklyReportSqliteRepository 초기화 실패", e);
            throw new RuntimeException("Repository 초기화 실패: " + e.getMessage(), e);
        }
    }
    
    private void createTable() throws SQLException {
        // 메인 테이블
        String sql = """
            CREATE TABLE IF NOT EXISTS weekly_reports (
                id TEXT PRIMARY KEY,
                company_id TEXT NOT NULL,
                title TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                project_name TEXT,
                reporter TEXT,
                additional_notes TEXT,
                created_date TEXT,
                this_week_request_count INTEGER DEFAULT 0,
                this_week_complete_count INTEGER DEFAULT 0,
                next_week_request_count INTEGER DEFAULT 0,
                next_week_complete_count INTEGER DEFAULT 0,
                this_week_tasks_text TEXT,
                next_week_tasks_text TEXT,
                check_items TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                updated_at TEXT DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                UNIQUE(company_id, start_date)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        
        // 업무 항목 테이블
        String workItemsSql = """
            CREATE TABLE IF NOT EXISTS weekly_report_work_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                report_id TEXT NOT NULL,
                item_type TEXT NOT NULL,
                task TEXT NOT NULL,
                assignee TEXT,
                status TEXT,
                progress INTEGER,
                notes TEXT,
                display_order INTEGER DEFAULT 0,
                FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(workItemsSql);
        }
        
        // 이슈 항목 테이블
        String issuesSql = """
            CREATE TABLE IF NOT EXISTS weekly_report_issues (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                report_id TEXT NOT NULL,
                issue TEXT NOT NULL,
                severity TEXT,
                status TEXT,
                action TEXT,
                display_order INTEGER DEFAULT 0,
                FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(issuesSql);
        }
        
        // 근태 요약 테이블
        String attendanceSql = """
            CREATE TABLE IF NOT EXISTS weekly_report_attendance_summaries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                report_id TEXT NOT NULL,
                developer_name TEXT NOT NULL,
                work_days INTEGER DEFAULT 0,
                late_days INTEGER DEFAULT 0,
                vacation_days INTEGER DEFAULT 0,
                notes TEXT,
                display_order INTEGER DEFAULT 0,
                FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(attendanceSql);
        }
        
        // 인덱스 생성
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_weekly_reports_company_id ON weekly_reports(company_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_weekly_reports_start_date ON weekly_reports(start_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_weekly_reports_company_start ON weekly_reports(company_id, start_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_work_items_report_id ON weekly_report_work_items(report_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_work_items_report_type ON weekly_report_work_items(report_id, item_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_report_issues_report_id ON weekly_report_issues(report_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attendance_summaries_report_id ON weekly_report_attendance_summaries(report_id)");
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
    
    public void save(WeeklyReport report) {
        try {
            // 메인 보고서 저장
            String sql = """
                INSERT OR REPLACE INTO weekly_reports 
                (id, company_id, title, start_date, end_date, project_name, reporter,
                 additional_notes, created_date, this_week_request_count, this_week_complete_count,
                 next_week_request_count, next_week_complete_count, this_week_tasks_text,
                 next_week_tasks_text, check_items, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, report.getId());
                stmt.setString(2, report.getCompanyId());
                stmt.setString(3, report.getTitle());
                stmt.setString(4, convertDateToString(report.getStartDate()));
                stmt.setString(5, convertDateToString(report.getEndDate()));
                stmt.setString(6, report.getProjectName());
                stmt.setString(7, report.getReporter());
                stmt.setString(8, report.getAdditionalNotes());
                stmt.setString(9, convertDateToString(report.getCreatedDate()));
                stmt.setInt(10, report.getThisWeekRequestCount() != null ? report.getThisWeekRequestCount() : 0);
                stmt.setInt(11, report.getThisWeekCompleteCount() != null ? report.getThisWeekCompleteCount() : 0);
                stmt.setInt(12, report.getNextWeekRequestCount() != null ? report.getNextWeekRequestCount() : 0);
                stmt.setInt(13, report.getNextWeekCompleteCount() != null ? report.getNextWeekCompleteCount() : 0);
                stmt.setString(14, report.getThisWeekTasksText());
                stmt.setString(15, report.getNextWeekTasksText());
                stmt.setString(16, convertCheckItemsToString(report.getCheckItems()));
                
                stmt.executeUpdate();
            }
            
            // 기존 하위 항목 삭제
            deleteWorkItems(report.getId());
            deleteIssues(report.getId());
            deleteAttendanceSummaries(report.getId());
            
            // 업무 항목 저장
            if (report.getLastWeekWork() != null) {
                for (int i = 0; i < report.getLastWeekWork().size(); i++) {
                    saveWorkItem(report.getId(), "LAST_WEEK", report.getLastWeekWork().get(i), i);
                }
            }
            if (report.getThisWeekPlan() != null) {
                for (int i = 0; i < report.getThisWeekPlan().size(); i++) {
                    saveWorkItem(report.getId(), "THIS_WEEK", report.getThisWeekPlan().get(i), i);
                }
            }
            
            // 이슈 항목 저장
            if (report.getIssues() != null) {
                for (int i = 0; i < report.getIssues().size(); i++) {
                    saveIssueItem(report.getId(), report.getIssues().get(i), i);
                }
            }
            
            // 근태 요약 저장
            if (report.getAttendanceSummaries() != null) {
                for (int i = 0; i < report.getAttendanceSummaries().size(); i++) {
                    saveAttendanceSummary(report.getId(), report.getAttendanceSummaries().get(i), i);
                }
            }
            
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                log.error("롤백 실패", rollbackEx);
            }
            log.error("주간보고서 저장 실패", e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    private void saveWorkItem(String reportId, String itemType, WeeklyReport.WorkItem item, int order) throws SQLException {
        String sql = """
            INSERT INTO weekly_report_work_items 
            (report_id, item_type, task, assignee, status, progress, notes, display_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.setString(2, itemType);
            stmt.setString(3, item.getTask());
            stmt.setString(4, item.getAssignee());
            stmt.setString(5, item.getStatus());
            stmt.setObject(6, item.getProgress());
            stmt.setString(7, item.getNotes());
            stmt.setInt(8, order);
            stmt.executeUpdate();
        }
    }
    
    private void saveIssueItem(String reportId, WeeklyReport.IssueItem item, int order) throws SQLException {
        String sql = """
            INSERT INTO weekly_report_issues 
            (report_id, issue, severity, status, action, display_order)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.setString(2, item.getIssue());
            stmt.setString(3, item.getSeverity());
            stmt.setString(4, item.getStatus());
            stmt.setString(5, item.getAction());
            stmt.setInt(6, order);
            stmt.executeUpdate();
        }
    }
    
    private void saveAttendanceSummary(String reportId, WeeklyReport.AttendanceSummary summary, int order) throws SQLException {
        String sql = """
            INSERT INTO weekly_report_attendance_summaries 
            (report_id, developer_name, work_days, late_days, vacation_days, notes, display_order)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.setString(2, summary.getDeveloperName());
            stmt.setInt(3, summary.getWorkDays() != null ? summary.getWorkDays() : 0);
            stmt.setInt(4, summary.getLateDays() != null ? summary.getLateDays() : 0);
            stmt.setInt(5, summary.getVacationDays() != null ? summary.getVacationDays() : 0);
            stmt.setString(6, summary.getNotes());
            stmt.setInt(7, order);
            stmt.executeUpdate();
        }
    }
    
    private void deleteWorkItems(String reportId) throws SQLException {
        String sql = "DELETE FROM weekly_report_work_items WHERE report_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.executeUpdate();
        }
    }
    
    private void deleteIssues(String reportId) throws SQLException {
        String sql = "DELETE FROM weekly_report_issues WHERE report_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.executeUpdate();
        }
    }
    
    private void deleteAttendanceSummaries(String reportId) throws SQLException {
        String sql = "DELETE FROM weekly_report_attendance_summaries WHERE report_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.executeUpdate();
        }
    }
    
    public List<WeeklyReport> findAll() {
        List<WeeklyReport> results = new ArrayList<>();
        String sql = "SELECT * FROM weekly_reports ORDER BY start_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                WeeklyReport report = mapRow(rs);
                loadSubItems(report);
                results.add(report);
            }
            
        } catch (SQLException e) {
            log.error("주간보고서 목록 조회 실패", e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public Optional<WeeklyReport> findById(String id) {
        String sql = "SELECT * FROM weekly_reports WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    WeeklyReport report = mapRow(rs);
                    loadSubItems(report);
                    return Optional.of(report);
                }
            }
            
        } catch (SQLException e) {
            log.error("주간보고서 조회 실패: {}", id, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    public Optional<WeeklyReport> findByStartDate(LocalDate startDate) {
        String sql = "SELECT * FROM weekly_reports WHERE start_date = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, convertDateToString(startDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    WeeklyReport report = mapRow(rs);
                    loadSubItems(report);
                    return Optional.of(report);
                }
            }
            
        } catch (SQLException e) {
            log.error("주간보고서 조회 실패: {}", startDate, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return Optional.empty();
    }
    
    public List<WeeklyReport> findByCompanyId(String companyId) {
        List<WeeklyReport> results = new ArrayList<>();
        String sql = "SELECT * FROM weekly_reports WHERE company_id = ? ORDER BY start_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, companyId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WeeklyReport report = mapRow(rs);
                    loadSubItems(report);
                    results.add(report);
                }
            }
            
        } catch (SQLException e) {
            log.error("회사별 주간보고서 조회 실패: {}", companyId, e);
            throw new RuntimeException("데이터 조회 실패", e);
        }
        
        return results;
    }
    
    public void update(WeeklyReport report) {
        save(report); // INSERT OR REPLACE로 처리
    }
    
    public void delete(String id) {
        // CASCADE로 하위 항목도 자동 삭제됨
        String sql = "DELETE FROM weekly_reports WHERE id = ?";
        
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
            log.error("주간보고서 삭제 실패: {}", id, e);
            throw new RuntimeException("데이터 삭제 실패", e);
        }
    }
    
    private WeeklyReport mapRow(ResultSet rs) throws SQLException {
        WeeklyReport report = new WeeklyReport();
        report.setId(rs.getString("id"));
        report.setCompanyId(rs.getString("company_id"));
        report.setTitle(rs.getString("title"));
        report.setStartDate(convertStringToDate(rs.getString("start_date")));
        report.setEndDate(convertStringToDate(rs.getString("end_date")));
        report.setProjectName(rs.getString("project_name"));
        report.setReporter(rs.getString("reporter"));
        report.setAdditionalNotes(rs.getString("additional_notes"));
        report.setCreatedDate(convertStringToDate(rs.getString("created_date")));
        report.setThisWeekRequestCount(rs.getInt("this_week_request_count"));
        report.setThisWeekCompleteCount(rs.getInt("this_week_complete_count"));
        report.setNextWeekRequestCount(rs.getInt("next_week_request_count"));
        report.setNextWeekCompleteCount(rs.getInt("next_week_complete_count"));
        report.setThisWeekTasksText(rs.getString("this_week_tasks_text"));
        report.setNextWeekTasksText(rs.getString("next_week_tasks_text"));
        report.setCheckItems(convertStringToCheckItems(rs.getString("check_items")));
        return report;
    }
    
    private void loadSubItems(WeeklyReport report) throws SQLException {
        // 업무 항목 로드
        report.setLastWeekWork(loadWorkItems(report.getId(), "LAST_WEEK"));
        report.setThisWeekPlan(loadWorkItems(report.getId(), "THIS_WEEK"));
        
        // 이슈 항목 로드
        report.setIssues(loadIssueItems(report.getId()));
        
        // 근태 요약 로드
        report.setAttendanceSummaries(loadAttendanceSummaries(report.getId()));
    }
    
    private List<WeeklyReport.WorkItem> loadWorkItems(String reportId, String itemType) throws SQLException {
        List<WeeklyReport.WorkItem> items = new ArrayList<>();
        String sql = "SELECT * FROM weekly_report_work_items WHERE report_id = ? AND item_type = ? ORDER BY display_order";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            stmt.setString(2, itemType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WeeklyReport.WorkItem item = new WeeklyReport.WorkItem();
                    item.setTask(rs.getString("task"));
                    item.setAssignee(rs.getString("assignee"));
                    item.setStatus(rs.getString("status"));
                    item.setProgress(rs.getObject("progress", Integer.class));
                    item.setNotes(rs.getString("notes"));
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    private List<WeeklyReport.IssueItem> loadIssueItems(String reportId) throws SQLException {
        List<WeeklyReport.IssueItem> items = new ArrayList<>();
        String sql = "SELECT * FROM weekly_report_issues WHERE report_id = ? ORDER BY display_order";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WeeklyReport.IssueItem item = new WeeklyReport.IssueItem();
                    item.setIssue(rs.getString("issue"));
                    item.setSeverity(rs.getString("severity"));
                    item.setStatus(rs.getString("status"));
                    item.setAction(rs.getString("action"));
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    private List<WeeklyReport.AttendanceSummary> loadAttendanceSummaries(String reportId) throws SQLException {
        List<WeeklyReport.AttendanceSummary> summaries = new ArrayList<>();
        String sql = "SELECT * FROM weekly_report_attendance_summaries WHERE report_id = ? ORDER BY display_order";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reportId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WeeklyReport.AttendanceSummary summary = new WeeklyReport.AttendanceSummary();
                    summary.setDeveloperName(rs.getString("developer_name"));
                    summary.setWorkDays(rs.getInt("work_days"));
                    summary.setLateDays(rs.getInt("late_days"));
                    summary.setVacationDays(rs.getInt("vacation_days"));
                    summary.setNotes(rs.getString("notes"));
                    summaries.add(summary);
                }
            }
        }
        
        return summaries;
    }
    
    private String convertCheckItemsToString(List<Boolean> checkItems) {
        if (checkItems == null || checkItems.isEmpty()) {
            return null;
        }
        // 간단한 JSON 배열 형식: "[true,false,true,...]"
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < checkItems.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(checkItems.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private List<Boolean> convertStringToCheckItems(String checkItemsStr) {
        if (checkItemsStr == null || checkItemsStr.trim().isEmpty() || checkItemsStr.equals("[]")) {
            return new ArrayList<>();
        }
        List<Boolean> items = new ArrayList<>();
        // 간단한 파싱: "[true,false,true]" 형식
        String content = checkItemsStr.trim();
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
            String[] parts = content.split(",");
            for (String part : parts) {
                items.add(Boolean.parseBoolean(part.trim()));
            }
        }
        return items;
    }
    
    private String convertDateToString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    private LocalDate convertStringToDate(String dateStr) {
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

