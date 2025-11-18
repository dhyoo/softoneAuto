package com.softone.auto.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 날짜 검증 유틸리티
 */
public class DateValidator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * 날짜 필드에 실시간 검증 추가 (yyyy-MM-dd)
     */
    public static void addDateValidation(JTextField dateField, JLabel errorLabel, String fieldName) {
        dateField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                validateDate();
            }
            public void removeUpdate(DocumentEvent e) {
                validateDate();
            }
            public void insertUpdate(DocumentEvent e) {
                validateDate();
            }
            
            private void validateDate() {
                SwingUtilities.invokeLater(() -> {
                    String text = dateField.getText().trim();
                    
                    if (text.isEmpty()) {
                        setError(errorLabel, "⚠ " + fieldName + "을(를) 입력하세요", dateField);
                    } else if (!isValidDate(text)) {
                        setError(errorLabel, "⚠ 형식: yyyy-MM-dd (예: 2025-01-15)", dateField);
                    } else {
                        setSuccess(errorLabel, dateField);
                    }
                });
            }
        });
    }
    
    /**
     * 날짜시간 필드에 실시간 검증 추가 (yyyy-MM-dd HH:mm)
     */
    public static void addDateTimeValidation(JTextField dateTimeField, JLabel errorLabel, String fieldName) {
        dateTimeField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                validateDateTime();
            }
            public void removeUpdate(DocumentEvent e) {
                validateDateTime();
            }
            public void insertUpdate(DocumentEvent e) {
                validateDateTime();
            }
            
            private void validateDateTime() {
                SwingUtilities.invokeLater(() -> {
                    String text = dateTimeField.getText().trim();
                    
                    if (text.isEmpty()) {
                        setError(errorLabel, "⚠ " + fieldName + "을(를) 입력하세요", dateTimeField);
                    } else if (!isValidDateTime(text)) {
                        setError(errorLabel, "⚠ 형식: yyyy-MM-dd HH:mm (예: 2025-01-15 14:30)", dateTimeField);
                    } else {
                        setSuccess(errorLabel, dateTimeField);
                    }
                });
            }
        });
    }
    
    /**
     * 시간 필드 검증 (HH:mm)
     */
    public static void addTimeValidation(JTextField timeField, JLabel errorLabel, String fieldName) {
        timeField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                validateTime();
            }
            public void removeUpdate(DocumentEvent e) {
                validateTime();
            }
            public void insertUpdate(DocumentEvent e) {
                validateTime();
            }
            
            private void validateTime() {
                SwingUtilities.invokeLater(() -> {
                    String text = timeField.getText().trim();
                    
                    if (text.isEmpty()) {
                        clearValidation(errorLabel, timeField);
                    } else if (!isValidTime(text)) {
                        setError(errorLabel, "⚠ 형식: HH:mm (예: 09:00)", timeField);
                    } else {
                        setSuccess(errorLabel, timeField);
                    }
                });
            }
        });
    }
    
    /**
     * 날짜 형식 검증 (yyyy-MM-dd)
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        
        // 기본 패턴 체크: yyyy-MM-dd (10자리)
        if (dateStr.length() != 10) {
            return false;
        }
        
        // 하이픈 위치 체크
        if (dateStr.charAt(4) != '-' || dateStr.charAt(7) != '-') {
            return false;
        }
        
        // 실제 날짜 파싱 시도
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * 날짜시간 형식 검증 (yyyy-MM-dd HH:mm)
     */
    public static boolean isValidDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return false;
        }
        
        // 기본 패턴 체크: yyyy-MM-dd HH:mm (16자리)
        if (dateTimeStr.length() != 16) {
            return false;
        }
        
        try {
            LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * 시간 형식 검증 (HH:mm)
     */
    public static boolean isValidTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        
        if (timeStr.length() != 5 || timeStr.charAt(2) != ':') {
            return false;
        }
        
        try {
            java.time.LocalTime.parse(timeStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * 에러 상태 설정
     */
    private static void setError(JLabel errorLabel, String message, JTextField field) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.ERROR, 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * 성공 상태 설정
     */
    private static void setSuccess(JLabel errorLabel, JTextField field) {
        if (errorLabel != null) {
            errorLabel.setText(" ");
        }
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.SUCCESS, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * 검증 상태 클리어
     */
    private static void clearValidation(JLabel errorLabel, JTextField field) {
        if (errorLabel != null) {
            errorLabel.setText(" ");
        }
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    /**
     * 저장 전 날짜 검증 (필수)
     */
    public static boolean validateDateBeforeSave(String dateStr, String fieldName, Component parent) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                fieldName + "을(를) 입력하세요.",
                "입력 오류",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!isValidDate(dateStr.trim())) {
            JOptionPane.showMessageDialog(parent,
                fieldName + " 형식이 올바르지 않습니다.\n\n" +
                "올바른 형식: yyyy-MM-dd\n" +
                "예시: 2025-01-15",
                "날짜 형식 오류",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * 날짜시간 검증 (필수)
     */
    public static boolean validateDateTimeBeforeSave(String dateTimeStr, String fieldName, Component parent) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            // 선택적 필드인 경우 true 반환
            return true;
        }
        
        if (!isValidDateTime(dateTimeStr.trim())) {
            JOptionPane.showMessageDialog(parent,
                fieldName + " 형식이 올바르지 않습니다.\n\n" +
                "올바른 형식: yyyy-MM-dd HH:mm\n" +
                "예시: 2025-01-15 14:30",
                "날짜시간 형식 오류",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
}

