package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

/**
 * 민감한 설정 정보를 안전하게 암호화하여 저장/로드하는 관리자
 * 
 * 암호화 방식:
 * - 알고리즘: AES-256-GCM (인증된 암호화)
 * - 키 파생: PBKDF2WithHmacSHA256 (10,000회 반복)
 * - IV: 랜덤 생성 (각 암호화마다 고유)
 * 
 * 보안 특징:
 * - GCM 모드로 무결성 검증 포함
 * - 마스터 키는 환경 변수나 시스템 속성에서 파생
 * - 키는 메모리에만 보관, 평문으로 저장하지 않음
 */
@Slf4j
public class SecureConfigManager {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256; // AES-256
    private static final int IV_LENGTH = 12; // GCM 권장 IV 길이
    private static final int TAG_LENGTH = 128; // GCM 인증 태그 길이 (비트)
    private static final int ITERATION_COUNT = 10000; // PBKDF2 반복 횟수
    private static final int SALT_LENGTH = 16; // 솔트 길이 (바이트)
    
    private static final String MASTER_KEY_ENV_VAR = "SOFTONE_MASTER_KEY";
    private static final String MASTER_KEY_PROPERTY = "softone.master.key";
    private static final String DEFAULT_MASTER_KEY = "SoftOneAuto2025!DefaultKey"; // 기본값 (프로덕션에서는 제거 필요)
    
    private static final String CONFIG_FILE = "config.encrypted";
    private static final String SALT_FILE = "config.salt";
    
    /**
     * 마스터 키 가져오기
     * 우선순위: 환경 변수 > 시스템 속성 > 기본값
     */
    private static String getMasterKey() {
        // 1. 환경 변수에서 확인
        String masterKey = System.getenv(MASTER_KEY_ENV_VAR);
        if (masterKey != null && !masterKey.isEmpty()) {
            log.debug("마스터 키를 환경 변수에서 로드");
            return masterKey;
        }
        
        // 2. 시스템 속성에서 확인
        masterKey = System.getProperty(MASTER_KEY_PROPERTY);
        if (masterKey != null && !masterKey.isEmpty()) {
            log.debug("마스터 키를 시스템 속성에서 로드");
            return masterKey;
        }
        
        // 3. 기본값 사용 (프로덕션에서는 제거 권장)
        log.warn("마스터 키가 설정되지 않아 기본값을 사용합니다. 프로덕션 환경에서는 환경 변수나 시스템 속성으로 설정하세요.");
        return DEFAULT_MASTER_KEY;
    }
    
    /**
     * 솔트 생성 또는 로드
     */
    private static byte[] getOrCreateSalt() throws IOException {
        Path saltPath = Paths.get(SALT_FILE);
        
        if (Files.exists(saltPath)) {
            // 기존 솔트 로드
            return Files.readAllBytes(saltPath);
        } else {
            // 새 솔트 생성 및 저장
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            Files.write(saltPath, salt);
            log.info("새 솔트 생성 및 저장: {}", SALT_FILE);
            return salt;
        }
    }
    
    /**
     * 마스터 키에서 암호화 키 파생 (PBKDF2)
     */
    private static SecretKey deriveKey(String masterKey, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
            masterKey.toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    /**
     * 데이터 암호화
     */
    public static String encrypt(String plaintext) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }
        
        // 마스터 키 및 솔트 가져오기
        String masterKey = getMasterKey();
        byte[] salt = getOrCreateSalt();
        
        // 키 파생
        SecretKey key = deriveKey(masterKey, salt);
        
        // IV 생성
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        
        // 암호화
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // IV + 암호화된 데이터를 Base64로 인코딩
        byte[] ivAndEncrypted = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, ivAndEncrypted, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, ivAndEncrypted, IV_LENGTH, encrypted.length);
        
        return Base64.getEncoder().encodeToString(ivAndEncrypted);
    }
    
    /**
     * 데이터 복호화
     */
    public static String decrypt(String ciphertext) throws Exception {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return "";
        }
        
        // 마스터 키 및 솔트 가져오기
        String masterKey = getMasterKey();
        byte[] salt = getOrCreateSalt();
        
        // 키 파생
        SecretKey key = deriveKey(masterKey, salt);
        
        // Base64 디코딩
        byte[] ivAndEncrypted = Base64.getDecoder().decode(ciphertext);
        
        // IV와 암호화된 데이터 분리
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[ivAndEncrypted.length - IV_LENGTH];
        System.arraycopy(ivAndEncrypted, 0, iv, 0, IV_LENGTH);
        System.arraycopy(ivAndEncrypted, IV_LENGTH, encrypted, 0, encrypted.length);
        
        // 복호화
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    /**
     * 암호화된 설정 파일에 저장
     */
    public static void saveEncryptedConfig(Properties config) throws Exception {
        // Properties를 문자열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        config.store(baos, "Encrypted Configuration");
        String plaintext = baos.toString(StandardCharsets.UTF_8);
        
        // 암호화
        String encrypted = encrypt(plaintext);
        
        // 파일에 저장
        try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
            writer.write(encrypted);
        }
        
        log.info("암호화된 설정 파일 저장 완료: {}", CONFIG_FILE);
    }
    
    /**
     * 암호화된 설정 파일에서 로드
     */
    public static Properties loadEncryptedConfig() throws Exception {
        File configFile = new File(CONFIG_FILE);
        
        if (!configFile.exists()) {
            log.info("암호화된 설정 파일이 없습니다. 새로 생성합니다.");
            return new Properties();
        }
        
        // 암호화된 데이터 읽기
        String encrypted;
        try (BufferedReader reader = new BufferedReader(
                new FileReader(configFile, StandardCharsets.UTF_8))) {
            encrypted = reader.readLine();
        }
        
        if (encrypted == null || encrypted.isEmpty()) {
            return new Properties();
        }
        
        // 복호화
        String plaintext = decrypt(encrypted);
        
        // Properties로 변환
        Properties config = new Properties();
        try (StringReader reader = new StringReader(plaintext)) {
            config.load(reader);
        }
        
        log.info("암호화된 설정 파일 로드 완료: {}", CONFIG_FILE);
        return config;
    }
    
    /**
     * 특정 키의 값을 암호화하여 저장
     */
    public static void saveEncryptedValue(String key, String value) throws Exception {
        Properties config = loadEncryptedConfig();
        if (value != null && !value.isEmpty()) {
            String encrypted = encrypt(value);
            config.setProperty(key, encrypted);
        } else {
            config.remove(key);
        }
        saveEncryptedConfig(config);
    }
    
    /**
     * 특정 키의 값을 복호화하여 가져오기
     */
    public static String getEncryptedValue(String key) throws Exception {
        Properties config = loadEncryptedConfig();
        String encrypted = config.getProperty(key);
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }
        return decrypt(encrypted);
    }
    
    /**
     * 기존 평문 설정 파일을 암호화된 형식으로 마이그레이션
     */
    public static void migrateFromPlainText(String plainTextConfigFile) throws Exception {
        File plainFile = new File(plainTextConfigFile);
        if (!plainFile.exists()) {
            log.warn("마이그레이션할 평문 설정 파일이 없습니다: {}", plainTextConfigFile);
            return;
        }
        
        // 평문 파일 읽기
        Properties plainConfig = new Properties();
        try (FileReader reader = new FileReader(plainFile, StandardCharsets.UTF_8)) {
            plainConfig.load(reader);
        }
        
        // 암호화하여 저장
        saveEncryptedConfig(plainConfig);
        
        // 원본 파일 백업 (선택사항)
        File backupFile = new File(plainTextConfigFile + ".backup");
        Files.copy(plainFile.toPath(), backupFile.toPath());
        
        log.info("평문 설정 파일을 암호화된 형식으로 마이그레이션 완료");
        log.info("원본 파일 백업: {}", backupFile.getAbsolutePath());
    }
}

