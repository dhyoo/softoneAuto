package com.softone.auto.repository.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite Connection 관리자 (싱글톤)
 * 모든 Repository가 같은 Connection을 공유하도록 함
 */
public class SqliteConnectionManager {
    
    private static SqliteConnectionManager instance;
    private Connection connection;
    private final String dbPath;
    
    private SqliteConnectionManager() {
        try {
            String basePath = com.softone.auto.util.DataPathManager.getDataPath();
            this.dbPath = basePath + File.separator + "data" + File.separator + "softone.db";
            
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            System.out.println("=== SqliteConnectionManager 초기화 ===");
            System.out.println("  데이터베이스 경로: " + dbPath);
            System.out.println("  절대 경로: " + dbFile.getAbsolutePath());
            
            // Connection 타임아웃 설정 (30초)
            String connectionUrl = "jdbc:sqlite:" + dbPath + "?busy_timeout=30000";
            this.connection = DriverManager.getConnection(connectionUrl);
            
            // SQLite 설정 (트랜잭션 시작 전에 실행해야 함)
            try (Statement stmt = connection.createStatement()) {
                // WAL 모드 활성화 (동시 읽기/쓰기 지원)
                stmt.execute("PRAGMA journal_mode = WAL");
                // Foreign Key 활성화
                stmt.execute("PRAGMA foreign_keys = ON");
                // 잠금 타임아웃 설정
                stmt.execute("PRAGMA busy_timeout = 30000");
                // 읽기 일관성 보장 (WAL 모드에서)
                stmt.execute("PRAGMA synchronous = NORMAL");
                System.out.println("  → SQLite 설정 완료 (WAL 모드, Foreign Key, 타임아웃)");
            }
            
            // 트랜잭션 수동 관리 시작
            this.connection.setAutoCommit(false);
            
            System.out.println("  ✓ SQLite Connection 생성 완료");
            System.out.println("========================================\n");
            
        } catch (SQLException e) {
            System.err.println("SQLite Connection 생성 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("데이터베이스 연결 실패", e);
        }
    }
    
    /**
     * 싱글톤 인스턴스 가져오기
     */
    public static synchronized SqliteConnectionManager getInstance() {
        if (instance == null) {
            instance = new SqliteConnectionManager();
        }
        return instance;
    }
    
    /**
     * Connection 가져오기
     */
    public Connection getConnection() {
        try {
            // Connection이 닫혔는지 확인
            if (connection.isClosed()) {
                System.err.println("Connection이 닫혔습니다. 재연결합니다...");
                reconnect();
            }
        } catch (SQLException e) {
            System.err.println("Connection 상태 확인 실패: " + e.getMessage());
            reconnect();
        }
        return connection;
    }
    
    /**
     * 재연결
     */
    private synchronized void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            String connectionUrl = "jdbc:sqlite:" + dbPath + "?busy_timeout=30000";
            this.connection = DriverManager.getConnection(connectionUrl);
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA busy_timeout = 30000");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
            
            this.connection.setAutoCommit(false);
            System.out.println("  ✓ SQLite Connection 재연결 완료");
            
        } catch (SQLException e) {
            System.err.println("재연결 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("데이터베이스 재연결 실패", e);
        }
    }
    
    /**
     * Connection 닫기 (애플리케이션 종료 시)
     */
    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("SQLite Connection 닫기 완료");
            }
        } catch (SQLException e) {
            System.err.println("Connection 닫기 실패: " + e.getMessage());
        }
    }
}

