package com.softone.auto;

import com.softone.auto.ui.MainFrame;
import com.softone.auto.util.CommandSecurityValidator;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * SoftOne Auto Manager 메인 애플리케이션
 * 현장대리인 업무 자동화 데스크톱 애플리케이션
 */
public class SoftoneApplication {
    
    private static FileLock lock;
    private static RandomAccessFile lockFile;
    
    public static void main(String[] args) {
        // UTF-8 인코딩 설정 (한글 깨짐 방지)
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        try {
            java.lang.reflect.Field charsetField = java.nio.charset.Charset.class.getDeclaredField("defaultCharset");
            charsetField.setAccessible(true);
            charsetField.set(null, java.nio.charset.Charset.forName("UTF-8"));
        } catch (Exception e) {
            // 인코딩 설정 실패 시 무시
        }
        
        // System.out과 System.err을 UTF-8로 재설정
        try {
            System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
            System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            // UTF-8 설정 실패 시 기존 스트림 유지
            System.err.println("UTF-8 인코딩 설정 실패 (기본 인코딩 사용): " + e.getMessage());
        }
        
        // 중복 실행 방지 - 기존 창 종료하고 새로 실행
        if (!acquireLock()) {
            int choice = JOptionPane.showConfirmDialog(null,
                "애플리케이션이 이미 실행 중입니다.\n\n" +
                "기존 창을 종료하고 새로 실행하시겠습니까?",
                "중복 실행 감지",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (choice == JOptionPane.YES_OPTION) {
                // 기존 프로세스 종료 시도
                try {
                    killExistingProcess();
                    Thread.sleep(1000); // 1초 대기
                    
                    // 다시 락 획득 시도
                    if (!acquireLock()) {
                        JOptionPane.showMessageDialog(null,
                            "기존 프로세스를 종료할 수 없습니다.\n" +
                            "작업 관리자에서 직접 종료해주세요.",
                            "오류",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                        return;
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "오류가 발생했습니다: " + e.getMessage(),
                        "오류",
                        JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                    return;
                }
            } else {
                System.exit(0);
                return;
            }
        }
        
        // Look and Feel 설정 (시스템 기본)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 공통코드 초기화 (GUI 전에)
        com.softone.auto.service.CommonCodeService commonCodeService = new com.softone.auto.service.CommonCodeService();
        com.softone.auto.util.CommonCodeInitializer.initializeDefaultCodes(commonCodeService);
        
        // GUI는 EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("MainFrame 생성 중 치명적 오류: " + e.getMessage());
                e.printStackTrace();
                // 최소한 에러 메시지라도 표시 (프로그램 종료하지 않음)
                try {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                            "프로그램을 시작하는 중 오류가 발생했습니다.\n\n" +
                            "일부 기능이 제한될 수 있습니다.\n\n" +
                            "오류: " + e.getMessage() + "\n\n" +
                            "자세한 내용은 콘솔 로그를 확인하세요.",
                            "시작 오류",
                            JOptionPane.WARNING_MESSAGE);
                    });
                } catch (Exception dialogEx) {
                    System.err.println("에러 다이얼로그 표시 실패: " + dialogEx.getMessage());
                }
                // System.exit() 제거 - 프로그램이 계속 실행되도록
            }
        });
        
        // 종료 시 락 해제
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            releaseLock();
        }));
    }
    
    /**
     * 중복 실행 방지를 위한 락 획득
     */
    private static boolean acquireLock() {
        File lockFileLocation = new File(System.getProperty("java.io.tmpdir"), ".softone_auto.lock");
        
        // 기존 락 정리
        cleanupOldLock(lockFileLocation);
        
        try {
            lockFile = new RandomAccessFile(lockFileLocation, "rw");
            lock = lockFile.getChannel().tryLock();
            
            if (lock == null) {
                closeLockFile();
                
                // 프로세스가 실행 중인지 확인
                if (!isProcessRunning()) {
                    // 락 파일 삭제 후 재시도
                    if (lockFileLocation.delete()) {
                        try {
                            lockFile = new RandomAccessFile(lockFileLocation, "rw");
                            lock = lockFile.getChannel().tryLock();
                            if (lock != null) {
                                return true;
                            }
                        } catch (Exception e) {
                            closeLockFile();
                        }
                    }
                }
                return false;
            }
            
            return true;
        } catch (Exception e) {
            closeLockFile();
            return false;
        }
    }
    
    /**
     * 오래된 락 파일 정리
     */
    private static void cleanupOldLock(File lockFileLocation) {
        if (lockFileLocation.exists()) {
            long age = System.currentTimeMillis() - lockFileLocation.lastModified();
            if (age > 5 * 60 * 1000) {
                try {
                    lockFileLocation.delete();
                } catch (Exception e) {
                    // 무시
                }
            }
        }
    }
    
    /**
     * 락 파일 및 락 해제
     */
    private static void closeLockFile() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
                lock = null;
            }
        } catch (Exception e) {
            // 로그만 기록
        }
        
        try {
            if (lockFile != null) {
                lockFile.close();
                lockFile = null;
            }
        } catch (Exception e) {
            // 로그만 기록
        }
    }
    
    /**
     * 실제 프로세스가 실행 중인지 확인
     */
    private static boolean isProcessRunning() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: wmic로 softoneAuto를 실행하는 프로세스 확인
                String[] commands = {
                    "cmd.exe", "/c",
                    "wmic process where \"name='java.exe' and commandline like '%softoneAuto%'\" get processid"
                };
                
                // 명령어 검증
                if (!CommandSecurityValidator.isValidCommandArray(commands)) {
                    System.err.println("안전하지 않은 명령어: " + java.util.Arrays.toString(commands));
                    return false;
                }
                
                // 안전하게 실행
                String[] sanitizedCommands;
                try {
                    sanitizedCommands = CommandSecurityValidator.sanitizeCommandArray(commands);
                } catch (SecurityException e) {
                    System.err.println("명령어 정제 실패: " + e.getMessage());
                    return false;
                }
                
                ProcessBuilder pb = new ProcessBuilder(sanitizedCommands);
                Process process = pb.start();
                
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.equals("ProcessId") && line.matches("\\d+")) {
                            process.waitFor();
                            return true;
                        }
                    }
                }
                
                process.waitFor();
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 락 해제
     */
    private static void releaseLock() {
        closeLockFile();
        
        // 락 파일 삭제
        File lockFileLocation = new File(System.getProperty("java.io.tmpdir"), ".softone_auto.lock");
        if (lockFileLocation.exists()) {
            try {
                lockFileLocation.delete();
            } catch (Exception e) {
                // 무시
            }
        }
    }
    
    /**
     * 기존 프로세스 종료 시도
     */
    private static void killExistingProcess() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: 실행 중인 Java 프로세스 찾아서 종료
                String[] commands = {
                    "cmd.exe", "/c", 
                    "wmic process where \"name='java.exe' and commandline like '%softoneAuto%'\" delete"
                };
                
                // 명령어 검증
                if (!CommandSecurityValidator.isValidCommandArray(commands)) {
                    System.err.println("안전하지 않은 명령어: " + java.util.Arrays.toString(commands));
                    return;
                }
                
                // 안전하게 실행
                String[] sanitizedCommands;
                try {
                    sanitizedCommands = CommandSecurityValidator.sanitizeCommandArray(commands);
                } catch (SecurityException e) {
                    System.err.println("명령어 정제 실패: " + e.getMessage());
                    return;
                }
                
                ProcessBuilder pb = new ProcessBuilder(sanitizedCommands);
                Process process = pb.start();
                
                // 출력 스트림 닫기 (사용하지 않지만 리소스 정리)
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    // 읽기 (프로세스 완료 대기)
                    while (reader.readLine() != null) {
                        // 무시
                    }
                }
                
                process.waitFor();
            }
        } catch (Exception e) {
            System.err.println("기존 프로세스 종료 실패: " + e.getMessage());
        }
    }
}


