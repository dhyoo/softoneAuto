package com.softone.auto.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 최적화된 테이블 셀 렌더러
 * 대용량 데이터 렌더링 성능 향상을 위한 최적화
 */
public class OptimizedTableRenderer extends DefaultTableCellRenderer {
    
    private static final OptimizedTableRenderer INSTANCE = new OptimizedTableRenderer();
    
    private OptimizedTableRenderer() {
        // 기본 설정
        setOpaque(true);
    }
    
    public static OptimizedTableRenderer getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, 
            boolean hasFocus, int row, int column) {
        
        // 부모 클래스의 기본 렌더링 수행
        Component component = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        
        // 선택 상태에 따른 배경색 설정 (최적화)
        if (isSelected) {
            component.setBackground(table.getSelectionBackground());
            component.setForeground(table.getSelectionForeground());
        } else {
            // 짝수/홀수 행 구분 (가독성 향상)
            if (row % 2 == 0) {
                component.setBackground(table.getBackground());
            } else {
                Color altColor = new Color(
                    Math.max(0, table.getBackground().getRed() - 5),
                    Math.max(0, table.getBackground().getGreen() - 5),
                    Math.max(0, table.getBackground().getBlue() - 5)
                );
                component.setBackground(altColor);
            }
            component.setForeground(table.getForeground());
        }
        
        // 포커스 표시 최적화 (렌더링 성능 향상)
        if (hasFocus && isSelected) {
            // 포커스가 있을 때만 테두리 표시
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(table.getSelectionBackground().darker(), 1),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ));
        } else {
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        
        return component;
    }
}

