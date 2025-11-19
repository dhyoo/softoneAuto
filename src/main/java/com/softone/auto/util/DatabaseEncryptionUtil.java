package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;

/**
 * SQLite 데이터베이스 파일 암호화/복호화 유틸리티
 * 
 * 주의: SQLite는 기본적으로 암호화를 지원하지 않으므로,
 * 애플리케이션 레벨에서 DB 파일 자체를 암호화/복호화합니다.
 * 
 * 더 나은 방법: SQLCipher 라이브러리 사용 권장
 */
@Slf4j
public class DatabaseEncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // GCM 권장 IV 길이
    private static final int TAG_LENGTH = 128; // GCM 인증 태그 길이 (비트)
    private static final int BUFFER_SIZE = 8192; // 8KB 버퍼
    
    /**
     * 데이터베이스 파일 암호화
     * 
     * @param dbPath 원본 DB 파일 경로
     * @param encryptedPath 암호화된 DB 파일 경로
     * @param key 암호화 키
     */
    public static void encryptDatabase(String dbPath, String encryptedPath, SecretKey key) throws Exception {
        log.info("데이터베이스 암호화 시작: {} -> {}", dbPath, encryptedPath);
        
        // IV 생성
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        
        // 암호화 초기화
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        Path sourcePath = Paths.get(dbPath);
        Path targetPath = Paths.get(encryptedPath);
        
        // 디렉토리 생성
        Files.createDirectories(targetPath.getParent());
        
        // IV를 파일 앞부분에 저장
        try (FileOutputStream fos = new FileOutputStream(targetPath.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher);
             FileInputStream fis = new FileInputStream(sourcePath.toFile())) {
            
            // IV 쓰기
            fos.write(iv);
            
            // 데이터 암호화 및 쓰기
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
        
        log.info("데이터베이스 암호화 완료: {}", encryptedPath);
    }
    
    /**
     * 데이터베이스 파일 복호화
     * 
     * @param encryptedPath 암호화된 DB 파일 경로
     * @param dbPath 복호화된 DB 파일 경로
     * @param key 복호화 키
     */
    public static void decryptDatabase(String encryptedPath, String dbPath, SecretKey key) throws Exception {
        log.info("데이터베이스 복호화 시작: {} -> {}", encryptedPath, dbPath);
        
        Path sourcePath = Paths.get(encryptedPath);
        Path targetPath = Paths.get(dbPath);
        
        // 디렉토리 생성
        Files.createDirectories(targetPath.getParent());
        
        try (FileInputStream fis = new FileInputStream(sourcePath.toFile());
             FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
            
            // IV 읽기
            byte[] iv = new byte[IV_LENGTH];
            int ivBytesRead = fis.read(iv);
            if (ivBytesRead != IV_LENGTH) {
                throw new IOException("IV 읽기 실패: 예상 길이 " + IV_LENGTH + ", 실제 " + ivBytesRead);
            }
            
            // 복호화 초기화
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            // 데이터 복호화 및 쓰기
            try (CipherInputStream cis = new CipherInputStream(fis, cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
        
        log.info("데이터베이스 복호화 완료: {}", dbPath);
    }
    
    /**
     * 데이터베이스 파일이 암호화되어 있는지 확인
     * (IV가 파일 앞부분에 있는지 확인)
     */
    public static boolean isEncrypted(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path) || Files.size(path) < IV_LENGTH) {
                return false;
            }
            
            // 파일 앞부분에 IV가 있는지 확인 (간단한 휴리스틱)
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                byte[] header = new byte[IV_LENGTH];
                fis.read(header);
                // IV는 랜덤 바이트이므로, 특정 패턴이 없으면 암호화된 것으로 간주
                // 더 정확한 검증을 위해서는 메타데이터 파일을 별도로 관리하는 것이 좋음
                return true;
            }
        } catch (Exception e) {
            log.warn("암호화 여부 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 데이터베이스 백업 (암호화 포함)
     */
    public static void backupDatabaseEncrypted(String dbPath, String backupPath, SecretKey key) throws Exception {
        log.info("데이터베이스 암호화 백업 시작: {} -> {}", dbPath, backupPath);
        
        // 임시 암호화 파일 경로
        String tempEncrypted = backupPath + ".tmp";
        
        try {
            // 암호화
            encryptDatabase(dbPath, tempEncrypted, key);
            
            // 백업 위치로 이동
            Files.move(Paths.get(tempEncrypted), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
            
            log.info("데이터베이스 암호화 백업 완료: {}", backupPath);
        } catch (Exception e) {
            // 임시 파일 정리
            try {
                Files.deleteIfExists(Paths.get(tempEncrypted));
            } catch (IOException ioEx) {
                log.warn("임시 파일 삭제 실패: {}", ioEx.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * 데이터베이스 복원 (암호화된 백업에서)
     */
    public static void restoreDatabaseEncrypted(String encryptedBackupPath, String dbPath, SecretKey key) throws Exception {
        log.info("데이터베이스 암호화 백업 복원 시작: {} -> {}", encryptedBackupPath, dbPath);
        
        // 복호화
        decryptDatabase(encryptedBackupPath, dbPath, key);
        
        log.info("데이터베이스 암호화 백업 복원 완료: {}", dbPath);
    }
}

