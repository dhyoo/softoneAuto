package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 데이터 무결성 검증을 위한 유틸리티
 * SHA-256 해시를 사용하여 데이터 변조를 감지
 */
@Slf4j
public class DataIntegrityManager {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * 문자열 데이터의 해시 계산
     */
    public static String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("해시 알고리즘을 사용할 수 없습니다: {}", HASH_ALGORITHM, e);
            throw new RuntimeException("해시 알고리즘을 사용할 수 없습니다", e);
        }
    }
    
    /**
     * 파일의 해시 계산
     */
    public static String calculateFileHash(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("해시 알고리즘을 사용할 수 없습니다: {}", HASH_ALGORITHM, e);
            throw new RuntimeException("해시 알고리즘을 사용할 수 없습니다", e);
        }
    }
    
    /**
     * 체크섬 파일에 해시 저장
     */
    public static void saveChecksum(File dataFile, String hash) throws IOException {
        File checksumFile = new File(dataFile.getParent(), dataFile.getName() + ".checksum");
        try (FileWriter writer = new FileWriter(checksumFile, StandardCharsets.UTF_8)) {
            writer.write(hash);
        }
        log.debug("체크섬 파일 저장: {}", checksumFile.getAbsolutePath());
    }
    
    /**
     * 체크섬 파일에서 해시 읽기
     */
    public static String readChecksum(File dataFile) throws IOException {
        File checksumFile = new File(dataFile.getParent(), dataFile.getName() + ".checksum");
        if (!checksumFile.exists()) {
            return null;
        }
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(checksumFile, StandardCharsets.UTF_8))) {
            return reader.readLine().trim();
        }
    }
    
    /**
     * 파일 무결성 검증
     * @return 검증 성공 여부
     */
    public static boolean verifyFileIntegrity(File dataFile) {
        try {
            String expectedHash = readChecksum(dataFile);
            if (expectedHash == null) {
                log.warn("체크섬 파일이 없습니다: {}", dataFile.getAbsolutePath());
                return false; // 체크섬 파일이 없으면 검증 불가
            }
            
            String actualHash = calculateFileHash(dataFile);
            boolean isValid = expectedHash.equals(actualHash);
            
            if (!isValid) {
                log.error("파일 무결성 검증 실패: {}", dataFile.getAbsolutePath());
                log.error("예상 해시: {}", expectedHash);
                log.error("실제 해시: {}", actualHash);
            } else {
                log.debug("파일 무결성 검증 성공: {}", dataFile.getName());
            }
            
            return isValid;
        } catch (IOException e) {
            log.error("파일 무결성 검증 중 오류: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 데이터 저장 시 해시 생성 및 저장
     */
    public static void saveWithIntegrityCheck(File dataFile, String data) throws IOException {
        // 1. 데이터 해시 계산
        String hash = calculateHash(data);
        
        // 2. 데이터 파일 저장
        try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
            writer.write(data);
        }
        
        // 3. 체크섬 파일 저장
        saveChecksum(dataFile, hash);
        
        log.debug("데이터 및 체크섬 저장 완료: {}", dataFile.getName());
    }
    
    /**
     * 데이터 로드 시 무결성 검증
     * @return 검증된 데이터, 검증 실패 시 null
     */
    public static String loadWithIntegrityCheck(File dataFile) throws IOException {
        if (!dataFile.exists()) {
            return null;
        }
        
        // 무결성 검증
        if (!verifyFileIntegrity(dataFile)) {
            log.warn("파일 무결성 검증 실패, 데이터 로드 중단: {}", dataFile.getAbsolutePath());
            throw new IOException("파일 무결성 검증 실패: 데이터가 변조되었을 수 있습니다");
        }
        
        // 검증 성공 시 데이터 읽기
        try (BufferedReader reader = new BufferedReader(
                new FileReader(dataFile, StandardCharsets.UTF_8))) {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line).append("\n");
            }
            return data.toString().trim();
        }
    }
}

