package com.softone.auto.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 애플리케이션 설정 관리
 */
public class AppSettings {
    
    private static final String SETTINGS_FILE = "settings.json";
    private static AppSettings instance;
    private Settings settings;
    private final Gson gson;
    
    private AppSettings() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadSettings();
    }
    
    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }
    
    /**
     * 설정 로드
     */
    private void loadSettings() {
        try {
            File file = new File(SETTINGS_FILE);
            if (file.exists()) {
                Reader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8);
                settings = gson.fromJson(reader, Settings.class);
                reader.close();
            } else {
                settings = new Settings();
            }
        } catch (IOException e) {
            e.printStackTrace();
            settings = new Settings();
        }
    }
    
    /**
     * 설정 저장
     */
    public void saveSettings() {
        try {
            Writer writer = new OutputStreamWriter(
                new FileOutputStream(SETTINGS_FILE), StandardCharsets.UTF_8);
            gson.toJson(settings, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 데이터 디렉토리 경로 가져오기
     */
    public String getDataDirectory() {
        return settings.dataDirectory;
    }
    
    /**
     * 데이터 디렉토리 경로 설정
     */
    public void setDataDirectory(String path) {
        settings.dataDirectory = path;
        saveSettings();
        
        // 디렉토리 생성
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 최초 실행 여부
     */
    public boolean isFirstRun() {
        return settings.dataDirectory == null || settings.dataDirectory.isEmpty();
    }
    
    /**
     * 설정 클래스
     */
    private static class Settings {
        private String dataDirectory = "";
        private String lastCompanyId = "";
        
        public Settings() {
        }
    }
    
    /**
     * 마지막 선택 회사 ID
     */
    public String getLastCompanyId() {
        return settings.lastCompanyId;
    }
    
    public void setLastCompanyId(String companyId) {
        settings.lastCompanyId = companyId;
        saveSettings();
    }
}

