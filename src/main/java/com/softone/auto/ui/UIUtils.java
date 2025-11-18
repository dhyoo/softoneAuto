package com.softone.auto.ui;

import javax.swing.*;
import java.awt.*;

/**
 * UI 공통 유틸리티
 */
public class UIUtils {
    
    /**
     * 통일된 버튼 생성 (모든 패널에서 사용)
     */
    public static JButton createUnifiedButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        button.setForeground(ModernDesign.TEXT_LIGHT);
        button.setBackground(ModernDesign.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 30));
        button.setMinimumSize(new Dimension(80, 30));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ModernDesign.PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ModernDesign.PRIMARY);
            }
        });
        
        return button;
    }
}

