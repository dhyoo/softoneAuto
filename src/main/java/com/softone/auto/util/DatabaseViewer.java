package com.softone.auto.util;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 데이터베이스 확인 유틸리티
 */
public class DatabaseViewer {
    
    private static final String DB_PATH;
    
    static {
        String basePath = DataPathManager.getDataPath();
        DB_PATH = basePath + File.separator + "data" + File.separator + "softone.db";
    }
    
    /**
     * 데이터베이스 정보 출력
     */
    public static void printDatabaseInfo() {
        System.out.println("========================================");
        System.out.println("SQLite 데이터베이스 정보");
        System.out.println("========================================");
        System.out.println("데이터베이스 파일: " + DB_PATH);
        
        File dbFile = new File(DB_PATH);
        if (!dbFile.exists()) {
            System.out.println("[경고] 데이터베이스 파일이 존재하지 않습니다.");
            return;
        }
        
        System.out.println("파일 크기: " + (dbFile.length() / 1024) + " KB");
        System.out.println("수정 시간: " + new java.util.Date(dbFile.lastModified()));
        System.out.println("");
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            // 테이블 목록
            System.out.println("테이블 목록:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
                for (String table : tables) {
                    System.out.println("  - " + table);
                }
            }
            System.out.println("");
            
            // 각 테이블의 데이터 수
            System.out.println("테이블별 데이터 수:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
                while (rs.next()) {
                    String tableName = rs.getString("name");
                    try (Statement countStmt = conn.createStatement();
                         ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) as cnt FROM " + tableName)) {
                        if (countRs.next()) {
                            System.out.println("  - " + tableName + ": " + countRs.getInt("cnt") + "건");
                        }
                    }
                }
            }
            System.out.println("");
            
            // 회사별 개발자 수
            System.out.println("회사별 개발자 수:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT c.name, COUNT(d.id) as dev_count " +
                     "FROM companies c " +
                     "LEFT JOIN developers d ON c.id = d.company_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY c.name")) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + ": " + rs.getInt("dev_count") + "명");
                }
            }
            System.out.println("");
            
            // 회사별 근태 수
            System.out.println("회사별 근태 수:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT c.name, COUNT(a.id) as att_count " +
                     "FROM companies c " +
                     "LEFT JOIN attendances a ON c.id = a.company_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY c.name")) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + ": " + rs.getInt("att_count") + "건");
                }
            }
            System.out.println("");
            
            // 회사별 이슈 수
            System.out.println("회사별 이슈 수:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT c.name, COUNT(i.id) as issue_count " +
                     "FROM companies c " +
                     "LEFT JOIN issues i ON c.id = i.company_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY c.name")) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + ": " + rs.getInt("issue_count") + "건");
                }
            }
            System.out.println("");
            
            // 회사별 고객소통 수
            System.out.println("회사별 고객소통 수:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT c.name, COUNT(cc.id) as comm_count " +
                     "FROM companies c " +
                     "LEFT JOIN customer_communications cc ON c.id = cc.company_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY c.name")) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + ": " + rs.getInt("comm_count") + "건");
                }
            }
            System.out.println("");
            
            // 회사별 주간보고서 수
            System.out.println("회사별 주간보고서 수:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT c.name, COUNT(wr.id) as report_count " +
                     "FROM companies c " +
                     "LEFT JOIN weekly_reports wr ON c.id = wr.company_id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY c.name")) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("name") + ": " + rs.getInt("report_count") + "건");
                }
            }
            System.out.println("");
            
        } catch (SQLException e) {
            System.err.println("데이터베이스 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
    
    /**
     * 특정 회사의 데이터 확인
     */
    public static void printCompanyData(String companyName) {
        System.out.println("========================================");
        System.out.println("회사 데이터 확인: " + companyName);
        System.out.println("========================================");
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            // 회사 정보
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM companies WHERE name = ?")) {
                stmt.setString(1, companyName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("회사 ID: " + rs.getString("id"));
                        System.out.println("회사명: " + rs.getString("name"));
                        System.out.println("프로젝트명: " + rs.getString("project_name"));
                        System.out.println("상태: " + rs.getString("status"));
                    } else {
                        System.out.println("회사를 찾을 수 없습니다: " + companyName);
                        return;
                    }
                }
            }
            System.out.println("");
            
            // 개발자 목록
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT d.* FROM developers d " +
                    "INNER JOIN companies c ON d.company_id = c.id " +
                    "WHERE c.name = ?")) {
                stmt.setString(1, companyName);
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("개발자 목록:");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("  " + count + ". " + rs.getString("name") + 
                                         " (ID: " + rs.getString("id") + 
                                         ", 회사ID: " + rs.getString("company_id") + ")");
                    }
                    if (count == 0) {
                        System.out.println("  (개발자 없음)");
                    }
                }
            }
            System.out.println("");
            
            // 근태 목록
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT a.* FROM attendances a " +
                    "INNER JOIN companies c ON a.company_id = c.id " +
                    "WHERE c.name = ? " +
                    "ORDER BY a.date DESC LIMIT 10")) {
                stmt.setString(1, companyName);
                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("근태 목록 (최근 10건):");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("  " + count + ". " + rs.getString("date") + 
                                         " - " + rs.getString("developer_name") + 
                                         " (회사ID: " + rs.getString("company_id") + ")");
                    }
                    if (count == 0) {
                        System.out.println("  (근태 없음)");
                    }
                }
            }
            System.out.println("");
            
        } catch (SQLException e) {
            System.err.println("데이터베이스 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("========================================");
    }
    
    /**
     * 메인 메서드 (독립 실행용)
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            printCompanyData(args[0]);
        } else {
            printDatabaseInfo();
        }
    }
}

