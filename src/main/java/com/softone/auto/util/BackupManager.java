package com.softone.auto.util;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.*;

/**
 * 백업 및 복원 관리자
 */
public class BackupManager {
    
    /**
     * 전체 데이터 백업
     */
    public static boolean backupAllData() {
        try {
            String dataPath = DataPathManager.getDataPath();
            String backupPath = DataPathManager.getBackupPath();
            
            // 백업 파일명: backup_YYYYMMDD_HHmmss.zip
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_" + timestamp + ".zip";
            String backupFilePath = backupPath + File.separator + backupFileName;
            
            // ZIP 파일 생성
            try (FileOutputStream fos = new FileOutputStream(backupFilePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                
                Path sourcePath = Paths.get(dataPath);
                Files.walk(sourcePath)
                    .filter(path -> !path.toString().contains("backups")) // 백업 폴더 제외
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String zipEntryName = sourcePath.relativize(path).toString();
                            zos.putNextEntry(new ZipEntry(zipEntryName));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println("파일 백업 실패: " + path + " - " + e.getMessage());
                        }
                    });
            }
            
            JOptionPane.showMessageDialog(
                null,
                "백업이 완료되었습니다!\n\n파일: " + backupFileName,
                "백업 완료",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "백업 실패: " + e.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
    
    /**
     * 백업 파일에서 데이터 복원
     */
    public static boolean restoreFromBackup() {
        try {
            // 백업 파일 선택
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(DataPathManager.getBackupPath()));
            fileChooser.setDialogTitle("복원할 백업 파일 선택");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip");
                }
                public String getDescription() {
                    return "백업 파일 (*.zip)";
                }
            });
            
            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            
            File backupFile = fileChooser.getSelectedFile();
            
            // 확인 대화상자
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "현재 데이터가 모두 삭제되고 백업 데이터로 교체됩니다.\n" +
                "정말 복원하시겠습니까?\n\n백업 파일: " + backupFile.getName(),
                "데이터 복원 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }
            
            String dataPath = DataPathManager.getDataPath();
            
            // 기존 데이터 삭제 (backups 폴더 제외)
            Files.walk(Paths.get(dataPath))
                .filter(path -> !path.toString().contains("backups"))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("파일 삭제 실패: " + path);
                    }
                });
            
            // ZIP 파일 압축 해제
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = Paths.get(dataPath, entry.getName());
                    
                    // 디렉토리 생성
                    Files.createDirectories(filePath.getParent());
                    
                    // 파일 복사
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    zis.closeEntry();
                }
            }
            
            JOptionPane.showMessageDialog(
                null,
                "데이터 복원이 완료되었습니다!\n애플리케이션을 재시작해주세요.",
                "복원 완료",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "복원 실패: " + e.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
    
    /**
     * 특정 회사 데이터만 백업
     */
    public static boolean backupCompanyData(String companyId, String companyName) {
        try {
            String companyPath = DataPathManager.getCompanyDataPath(companyId);
            String backupPath = DataPathManager.getBackupPath();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_" + companyName + "_" + timestamp + ".zip";
            String backupFilePath = backupPath + File.separator + backupFileName;
            
            try (FileOutputStream fos = new FileOutputStream(backupFilePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                
                Path sourcePath = Paths.get(companyPath);
                if (Files.exists(sourcePath)) {
                    Files.walk(sourcePath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String zipEntryName = sourcePath.relativize(path).toString();
                                zos.putNextEntry(new ZipEntry(zipEntryName));
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                System.err.println("파일 백업 실패: " + path);
                            }
                        });
                }
            }
            
            JOptionPane.showMessageDialog(
                null,
                companyName + " 백업이 완료되었습니다!\n\n파일: " + backupFileName,
                "백업 완료",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null,
                "백업 실패: " + e.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
}

