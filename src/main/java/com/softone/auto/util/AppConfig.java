package com.softone.auto.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 애플리케이션 설정 관리
 * 
 * <p><b>현재 구현:</b> JSON 파일 기반 설정 저장</p>
 * <p><b>향후 계획:</b> SQLite 기반 설정 테이블로 전환 예정</p>
 * <p>
 *   전환 시 장점:
 *   - 설정 값 암호화 지원 용이
 *   - 트랜잭션 기반 원자적 업데이트
 *   - 설정 변경 이력 추적 가능
 * </p>
 * 
 * @see com.softone.auto.util.SecureConfigManager (암호화된 설정 관리)
 */
@Data
public class AppConfig {
    
    private static final String CONFIG_FILE = "config.json";
    private static AppConfig instance;
    
    private String dataPath;
    private String lastCompanyId;
    private boolean darkMode = false;
    private String language = "ko";
    private String applicationMode = "DEVELOPMENT"; // DEVELOPMENT, PRODUCTION, DEMO
    private boolean enableSampleData = true; // 샘플 데이터 활성화 여부
    private WindowSettings windowSettings = new WindowSettings();
    
    @Data
    public static class WindowSettings {
        private int width = 1400;
        private int height = 900;
        private boolean maximized = false;
    }
    
    /**
     * 싱글톤 인스턴스 가져오기
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    /**
     * 설정 파일 로드
     */
    private static AppConfig load() {
        File configFile = new File(CONFIG_FILE);
        
        if (!configFile.exists()) {
            System.out.println("설정 파일 없음. 새로 생성합니다.");
            AppConfig config = new AppConfig();
            // 환경 변수나 시스템 속성에서 모드 확인
            config.detectApplicationMode();
            return config;
        }
        
        try (Reader reader = new InputStreamReader(
                new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            AppConfig config = gson.fromJson(reader, AppConfig.class);
            
            if (config != null) {
                System.out.println("설정 파일 로드 완료: " + CONFIG_FILE);
                System.out.println("  - 데이터 경로: " + config.dataPath);
                // 환경 변수나 시스템 속성이 우선순위가 높으면 덮어쓰기
                config.detectApplicationMode();
                System.out.println("  - 애플리케이션 모드: " + config.applicationMode);
                System.out.println("  - 샘플 데이터 활성화: " + config.enableSampleData);
                return config;
            }
        } catch (Exception e) {
            System.err.println("설정 파일 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        AppConfig config = new AppConfig();
        config.detectApplicationMode();
        return config;
    }
    
    /**
     * 애플리케이션 모드 자동 감지
     * 우선순위: 환경 변수 > 시스템 속성 > 설정 파일
     */
    private void detectApplicationMode() {
        // 1. 환경 변수 확인
        String envMode = System.getenv("SOFTONE_APP_MODE");
        if (envMode != null && !envMode.isEmpty()) {
            this.applicationMode = envMode.toUpperCase();
            updateSampleDataFlag();
            System.out.println("환경 변수에서 애플리케이션 모드 감지: " + this.applicationMode);
            return;
        }
        
        // 2. 시스템 속성 확인
        String propMode = System.getProperty("softone.app.mode");
        if (propMode != null && !propMode.isEmpty()) {
            this.applicationMode = propMode.toUpperCase();
            updateSampleDataFlag();
            System.out.println("시스템 속성에서 애플리케이션 모드 감지: " + this.applicationMode);
            return;
        }
        
        // 3. 설정 파일 값 사용 (이미 로드됨)
        updateSampleDataFlag();
    }
    
    /**
     * 애플리케이션 모드에 따라 샘플 데이터 플래그 업데이트
     */
    private void updateSampleDataFlag() {
        if ("PRODUCTION".equalsIgnoreCase(applicationMode)) {
            this.enableSampleData = false;
        } else {
            // DEVELOPMENT 또는 DEMO 모드에서는 기본값 유지
            // 설정 파일에 명시적으로 false로 되어있으면 그대로 유지
        }
    }
    
    /**
     * 애플리케이션 모드 가져오기
     */
    public ApplicationMode getApplicationMode() {
        try {
            return ApplicationMode.valueOf(applicationMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApplicationMode.DEVELOPMENT; // 기본값
        }
    }
    
    /**
     * 샘플 데이터 활성화 여부
     */
    public boolean isSampleDataEnabled() {
        return enableSampleData && getApplicationMode() != ApplicationMode.PRODUCTION;
    }
    
    /**
     * 개발 모드 여부
     */
    public boolean isDevelopmentMode() {
        return getApplicationMode() == ApplicationMode.DEVELOPMENT;
    }
    
    /**
     * 프로덕션 모드 여부
     */
    public boolean isProductionMode() {
        return getApplicationMode() == ApplicationMode.PRODUCTION;
    }
    
    /**
     * 설정 저장
     */
    public void save() {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
            System.out.println("설정 파일 저장 완료: " + CONFIG_FILE);
        } catch (Exception e) {
            System.err.println("설정 파일 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 데이터 경로 가져오기 (없으면 선택 대화상자 표시)
     */
    public String getOrSelectDataPath() {
        if (dataPath == null || dataPath.isEmpty()) {
            dataPath = selectDataPath();
            save();
        }
        
        // 경로 존재 여부 확인
        if (dataPath != null && !Files.exists(Paths.get(dataPath))) {
            try {
                Files.createDirectories(Paths.get(dataPath));
            } catch (IOException e) {
                System.err.println("데이터 경로 생성 실패: " + e.getMessage());
            }
        }
        
        return dataPath;
    }
    
    /**
     * 데이터 저장 경로 선택
     */
    private String selectDataPath() {
        String defaultPath = System.getProperty("user.home") + File.separator + "SoftOneAutoData";
        
        int choice = JOptionPane.showOptionDialog(
            null,
            "데이터를 저장할 위치를 선택해주세요.\n\n" +
            "권장: " + defaultPath + "\n\n" +
            "• 기본 위치 사용: 위 경로에 자동 저장\n" +
            "• 직접 선택: 원하는 폴더를 선택",
            "데이터 저장 위치 설정",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{"기본 위치 사용", "직접 선택", "취소"},
            "기본 위치 사용"
        );
        
        String selectedPath = null;
        
        if (choice == 0) {
            selectedPath = defaultPath;
        } else if (choice == 1) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("데이터 저장 폴더 선택");
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            
            int result = fileChooser.showDialog(null, "선택");
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
            }
        }
        
        if (selectedPath != null) {
            try {
                // JFileChooser를 통해 선택된 경로는 이미 안전하므로,
                // 최소한의 검증만 수행 (Path Traversal 패턴만 확인)
                Path path = Paths.get(selectedPath).normalize().toAbsolutePath();
                String normalizedPath = path.toString();
                
                // Path Traversal 패턴만 확인 (사용자가 직접 입력한 경우 대비)
                if (normalizedPath.contains("..")) {
                    JOptionPane.showMessageDialog(
                        null,
                        "안전하지 않은 경로입니다.\n다른 경로를 선택해주세요.",
                        "경로 오류",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return selectDataPath(); // 재선택 요청
                }
                
                // 경로가 존재하지 않으면 생성
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }
                
                // 경로 접근 가능 여부 확인
                if (!Files.isWritable(path)) {
                    JOptionPane.showMessageDialog(
                        null,
                        "선택한 폴더에 쓰기 권한이 없습니다.\n다른 폴더를 선택해주세요.",
                        "권한 오류",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return selectDataPath(); // 재선택 요청
                }
                
                JOptionPane.showMessageDialog(
                    null,
                    "데이터 저장 위치가 설정되었습니다:\n" + normalizedPath,
                    "설정 완료",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                return normalizedPath;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    null,
                    "경로 설정 실패: " + e.getMessage() + "\n\n다른 경로를 선택해주세요.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
                );
                // 오류 발생 시 재선택 요청
                return selectDataPath();
            }
        }
        
        // 취소하거나 실패한 경우 기본 경로 사용
        return "data";
    }
    
    /**
     * 데이터 경로 재설정
     */
    public void resetDataPath() {
        dataPath = null;
        dataPath = selectDataPath();
        save();
    }
}

