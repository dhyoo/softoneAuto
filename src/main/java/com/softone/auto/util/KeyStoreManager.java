package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import javax.crypto.KeyGenerator;
import java.util.Base64;

/**
 * Java KeyStore를 사용한 암호화 키 관리
 * 
 * 보안 특징:
 * - 키는 KeyStore에 안전하게 저장
 * - KeyStore 자체는 비밀번호로 보호
 * - Windows Credential Manager와 유사한 기능 제공
 */
@Slf4j
public class KeyStoreManager {
    
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_FILE = "softone.keystore";
    private static final String KEYSTORE_ALIAS = "softone-master-key";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256; // AES-256
    
    // KeyStore 비밀번호는 환경 변수나 시스템 속성에서 가져옴
    private static final String KEYSTORE_PASSWORD_ENV_VAR = "SOFTONE_KEYSTORE_PASSWORD";
    private static final String KEYSTORE_PASSWORD_PROPERTY = "softone.keystore.password";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "SoftOneAuto2025!KeyStore"; // 기본값 (프로덕션에서는 제거 필요)
    
    /**
     * KeyStore 비밀번호 가져오기
     */
    private static char[] getKeyStorePassword() {
        // 1. 환경 변수에서 확인
        String password = System.getenv(KEYSTORE_PASSWORD_ENV_VAR);
        if (password != null && !password.isEmpty()) {
            log.debug("KeyStore 비밀번호를 환경 변수에서 로드");
            return password.toCharArray();
        }
        
        // 2. 시스템 속성에서 확인
        password = System.getProperty(KEYSTORE_PASSWORD_PROPERTY);
        if (password != null && !password.isEmpty()) {
            log.debug("KeyStore 비밀번호를 시스템 속성에서 로드");
            return password.toCharArray();
        }
        
        // 3. 기본값 사용 (프로덕션에서는 제거 권장)
        log.warn("KeyStore 비밀번호가 설정되지 않아 기본값을 사용합니다. 프로덕션 환경에서는 환경 변수나 시스템 속성으로 설정하세요.");
        return DEFAULT_KEYSTORE_PASSWORD.toCharArray();
    }
    
    /**
     * KeyStore 파일 경로 가져오기
     */
    private static Path getKeyStorePath() {
        String dataPath = AppConfig.getInstance().getOrSelectDataPath();
        return Paths.get(dataPath, KEYSTORE_FILE);
    }
    
    /**
     * KeyStore 로드 또는 생성
     */
    private static KeyStore loadOrCreateKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        Path keyStorePath = getKeyStorePath();
        char[] password = getKeyStorePassword();
        
        if (Files.exists(keyStorePath)) {
            // 기존 KeyStore 로드
            try (FileInputStream fis = new FileInputStream(keyStorePath.toFile())) {
                keyStore.load(fis, password);
                log.info("기존 KeyStore 로드 완료: {}", keyStorePath);
            }
        } else {
            // 새 KeyStore 생성
            keyStore.load(null, password);
            
            // 디렉토리 생성
            Files.createDirectories(keyStorePath.getParent());
            
            // KeyStore 저장
            try (FileOutputStream fos = new FileOutputStream(keyStorePath.toFile())) {
                keyStore.store(fos, password);
            }
            log.info("새 KeyStore 생성 완료: {}", keyStorePath);
        }
        
        return keyStore;
    }
    
    /**
     * KeyStore 저장
     */
    private static void saveKeyStore(KeyStore keyStore) throws Exception {
        Path keyStorePath = getKeyStorePath();
        char[] password = getKeyStorePassword();
        
        try (FileOutputStream fos = new FileOutputStream(keyStorePath.toFile())) {
            keyStore.store(fos, password);
        }
        log.debug("KeyStore 저장 완료: {}", keyStorePath);
    }
    
    /**
     * 마스터 키 생성 또는 가져오기
     */
    public static SecretKey getOrCreateMasterKey() throws Exception {
        KeyStore keyStore = loadOrCreateKeyStore();
        
        // 기존 키 확인
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                KEYSTORE_ALIAS, 
                new KeyStore.PasswordProtection(getKeyStorePassword())
            );
            SecretKey key = entry.getSecretKey();
            log.debug("기존 마스터 키 로드 완료");
            return key;
        }
        
        // 새 키 생성
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey newKey = keyGenerator.generateKey();
        
        // KeyStore에 저장
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(newKey);
        keyStore.setEntry(
            KEYSTORE_ALIAS,
            secretKeyEntry,
            new KeyStore.PasswordProtection(getKeyStorePassword())
        );
        saveKeyStore(keyStore);
        
        log.info("새 마스터 키 생성 및 저장 완료");
        return newKey;
    }
    
    /**
     * 마스터 키를 Base64 문자열로 내보내기 (백업용)
     * 주의: 이 메서드는 백업 목적으로만 사용하고, 생성된 문자열은 안전하게 보관해야 함
     */
    public static String exportMasterKeyAsBase64() throws Exception {
        SecretKey key = getOrCreateMasterKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * Base64 문자열에서 마스터 키 가져오기 (복원용)
     */
    public static void importMasterKeyFromBase64(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        
        KeyStore keyStore = loadOrCreateKeyStore();
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(key);
        keyStore.setEntry(
            KEYSTORE_ALIAS,
            secretKeyEntry,
            new KeyStore.PasswordProtection(getKeyStorePassword())
        );
        saveKeyStore(keyStore);
        
        log.info("마스터 키 복원 완료");
    }
    
    /**
     * KeyStore 삭제 (주의: 모든 키가 삭제됨)
     */
    public static void deleteKeyStore() throws IOException {
        Path keyStorePath = getKeyStorePath();
        if (Files.exists(keyStorePath)) {
            Files.delete(keyStorePath);
            log.warn("KeyStore 삭제 완료: {}", keyStorePath);
        }
    }
    
    /**
     * KeyStore 존재 여부 확인
     */
    public static boolean keyStoreExists() {
        return Files.exists(getKeyStorePath());
    }
}

