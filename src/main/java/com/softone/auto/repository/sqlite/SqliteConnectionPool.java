package com.softone.auto.repository.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SQLite Connection Pool
 * SQLite는 단일 파일이지만, 읽기 성능 향상을 위해 여러 Connection을 관리
 */
public class SqliteConnectionPool {
    
    private static SqliteConnectionPool instance;
    private final String dbPath;
    private final BlockingQueue<Connection> availableConnections;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final int maxConnections;
    private final int minConnections;
    private volatile boolean isShutdown = false;
    
    // SQLite는 단일 파일이므로 읽기 전용 연결은 여러 개 가능하지만,
    // 쓰기는 단일 연결로 제한하는 것이 안전
    private static final int DEFAULT_MAX_CONNECTIONS = 5;
    private static final int DEFAULT_MIN_CONNECTIONS = 2;
    private static final long CONNECTION_TIMEOUT_SECONDS = 30;
    
    private SqliteConnectionPool() {
        try {
            String basePath = com.softone.auto.util.AppConfig.getInstance().getOrSelectDataPath();
            this.dbPath = basePath + File.separator + "data" + File.separator + "softone.db";
            
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            this.maxConnections = DEFAULT_MAX_CONNECTIONS;
            this.minConnections = DEFAULT_MIN_CONNECTIONS;
            this.availableConnections = new LinkedBlockingQueue<>(maxConnections);
            
            System.out.println("=== SqliteConnectionPool 초기화 ===");
            System.out.println("  데이터베이스 경로: " + dbPath);
            System.out.println("  최대 연결 수: " + maxConnections);
            System.out.println("  최소 연결 수: " + minConnections);
            
            // 초기 연결 생성
            for (int i = 0; i < minConnections; i++) {
                Connection conn = createConnection();
                if (conn != null) {
                    availableConnections.offer(conn);
                    activeConnections.incrementAndGet();
                }
            }
            
            System.out.println("  ✓ Connection Pool 생성 완료 (" + activeConnections.get() + "개 연결)");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("Connection Pool 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Connection Pool 초기화 실패", e);
        }
    }
    
    /**
     * 싱글톤 인스턴스 가져오기
     */
    public static synchronized SqliteConnectionPool getInstance() {
        if (instance == null) {
            instance = new SqliteConnectionPool();
        }
        return instance;
    }
    
    /**
     * Connection 생성
     */
    private Connection createConnection() {
        try {
            String connectionUrl = "jdbc:sqlite:" + dbPath + "?busy_timeout=30000";
            Connection conn = DriverManager.getConnection(connectionUrl);
            
            // SQLite 설정
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA busy_timeout = 30000");
                stmt.execute("PRAGMA synchronous = NORMAL");
                // 읽기 성능 최적화
                stmt.execute("PRAGMA cache_size = -64000"); // 64MB 캐시
                stmt.execute("PRAGMA temp_store = MEMORY");
            }
            
            // 읽기 전용 연결은 AutoCommit 활성화 (성능 향상)
            conn.setAutoCommit(true);
            
            return conn;
        } catch (SQLException e) {
            System.err.println("Connection 생성 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Connection 가져오기 (읽기 전용)
     */
    public Connection getReadConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Connection Pool이 종료되었습니다");
        }
        
        Connection conn = availableConnections.poll();
        
        if (conn == null) {
            // 사용 가능한 연결이 없으면 새로 생성 (최대치까지)
            int current = activeConnections.get();
            if (current < maxConnections) {
                conn = createConnection();
                if (conn != null) {
                    activeConnections.incrementAndGet();
                }
            } else {
                // 최대치에 도달했으면 대기
                try {
                    conn = availableConnections.poll(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (conn == null) {
                        throw new SQLException("Connection 획득 타임아웃");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Connection 획득 중단", e);
                }
            }
        }
        
        // Connection 유효성 검사
        try {
            if (conn.isClosed() || !conn.isValid(1)) {
                conn = createConnection();
            }
        } catch (SQLException e) {
            conn = createConnection();
        }
        
        return conn;
    }
    
    /**
     * Connection 반환
     */
    public void returnConnection(Connection conn) {
        if (conn == null || isShutdown) {
            return;
        }
        
        try {
            if (!conn.isClosed() && conn.isValid(1)) {
                // 읽기 전용 연결은 롤백 불필요
                availableConnections.offer(conn);
            } else {
                // 유효하지 않은 연결은 제거하고 새로 생성
                activeConnections.decrementAndGet();
                Connection newConn = createConnection();
                if (newConn != null) {
                    availableConnections.offer(newConn);
                    activeConnections.incrementAndGet();
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection 반환 실패: " + e.getMessage());
            activeConnections.decrementAndGet();
        }
    }
    
    /**
     * 쓰기 전용 Connection (단일 연결, 트랜잭션 관리)
     */
    private Connection writeConnection;
    
    public synchronized Connection getWriteConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Connection Pool이 종료되었습니다");
        }
        
        if (writeConnection == null || writeConnection.isClosed()) {
            String connectionUrl = "jdbc:sqlite:" + dbPath + "?busy_timeout=30000";
            writeConnection = DriverManager.getConnection(connectionUrl);
            
            try (Statement stmt = writeConnection.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA busy_timeout = 30000");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
            
            writeConnection.setAutoCommit(false);
        }
        
        return writeConnection;
    }
    
    /**
     * Connection Pool 종료
     */
    public synchronized void shutdown() {
        isShutdown = true;
        
        // 모든 읽기 연결 종료
        Connection conn;
        while ((conn = availableConnections.poll()) != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Connection 종료 실패: " + e.getMessage());
            }
            activeConnections.decrementAndGet();
        }
        
        // 쓰기 연결 종료
        if (writeConnection != null) {
            try {
                if (!writeConnection.isClosed()) {
                    writeConnection.close();
                }
            } catch (SQLException e) {
                System.err.println("Write Connection 종료 실패: " + e.getMessage());
            }
            writeConnection = null;
        }
        
        System.out.println("Connection Pool 종료 완료");
    }
    
    /**
     * Connection Pool 상태 정보
     */
    public String getStatus() {
        return String.format("Connection Pool 상태: 활성=%d, 사용 가능=%d, 최대=%d",
            activeConnections.get(), availableConnections.size(), maxConnections);
    }
}

