package com.softone.auto.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 모던 디자인 시스템
 * 프로페셔널하고 현대적인 UI를 위한 스타일 가이드
 */
public class ModernDesign {
    
    // ============ 색상 팔레트 ============
    // Primary Colors
    public static final Color PRIMARY = new Color(41, 128, 185);           // 파란색
    public static final Color PRIMARY_DARK = new Color(33, 97, 140);       // 진한 파란색
    public static final Color PRIMARY_LIGHT = new Color(93, 173, 226);     // 밝은 파란색
    
    // Accent Colors
    public static final Color ACCENT = new Color(39, 174, 96);             // 초록색
    public static final Color ACCENT_DARK = new Color(34, 153, 84);        // 진한 초록색
    public static final Color WARNING = new Color(243, 156, 18);           // 주황색
    public static final Color DANGER = new Color(231, 76, 60);             // 빨강색
    
    // Neutral Colors
    public static final Color BG_PRIMARY = new Color(245, 247, 250);       // 메인 배경
    public static final Color BG_SECONDARY = new Color(255, 255, 255);     // 카드 배경
    public static final Color BG_DARK = new Color(44, 62, 80);             // 다크 배경
    
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);        // 주요 텍스트
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141);   // 보조 텍스트
    public static final Color TEXT_LIGHT = new Color(255, 255, 255);       // 밝은 텍스트
    
    public static final Color BORDER = new Color(220, 227, 232);           // 테두리
    public static final Color SHADOW = new Color(0, 0, 0, 20);             // 그림자
    
    // Status Colors
    public static final Color SUCCESS = new Color(46, 204, 113);           // 성공
    public static final Color INFO = new Color(52, 152, 219);              // 정보
    public static final Color ERROR = new Color(231, 76, 60);              // 에러
    
    // ============ 폰트 ============
    public static final Font FONT_TITLE = new Font("맑은 고딕", Font.BOLD, 28);
    public static final Font FONT_HEADING = new Font("맑은 고딕", Font.BOLD, 20);
    public static final Font FONT_SUBHEADING = new Font("맑은 고딕", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("맑은 고딕", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("맑은 고딕", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("맑은 고딕", Font.BOLD, 14);
    
    // ============ 스페이싱 ============
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE = 20;
    public static final int PADDING_XL = 30;
    
    public static final int RADIUS_SMALL = 4;
    public static final int RADIUS_MEDIUM = 8;
    public static final int RADIUS_LARGE = 12;
    
    // ============ UI 컴포넌트 생성 메소드 ============
    
    /**
     * 모던 스타일 버튼 생성 (통일된 디자인)
     */
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_LIGHT);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 20, 11, 20));
        
        // 호버 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY);
            }
        });
        
        return button;
    }
    
    /**
     * Primary 버튼 (통일된 스타일)
     */
    public static JButton createPrimaryButton(String text) {
        return createButton(text);
    }
    
    /**
     * Success 버튼 (통일된 스타일)
     */
    public static JButton createSuccessButton(String text) {
        return createButton(text);
    }
    
    /**
     * Danger 버튼 (통일된 스타일)
     */
    public static JButton createDangerButton(String text) {
        return createButton(text);
    }
    
    /**
     * 세컨더리 버튼 (아웃라인 스타일)
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(PRIMARY);
        button.setBackground(BG_SECONDARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 2),
            new EmptyBorder(9, 18, 9, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 호버 효과
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(241, 245, 249));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BG_SECONDARY);
            }
        });
        
        return button;
    }
    
    /**
     * 모던 스타일 카드 패널
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE)
        ));
        return card;
    }
    
    /**
     * 통계 카드 생성
     */
    public static JPanel createStatsCard(String title, String value, Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // 아이콘 & 제목 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(BG_SECONDARY);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(accentColor);
        topPanel.add(iconLabel);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_BODY);
        titleLabel.setForeground(TEXT_SECONDARY);
        topPanel.add(titleLabel);
        
        card.add(topPanel, BorderLayout.NORTH);
        
        // 값 레이블
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 48));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * 제목 레이블 생성
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    /**
     * 헤딩 레이블 생성
     */
    public static JLabel createHeadingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    /**
     * 서브헤딩 레이블 생성
     */
    public static JLabel createSubheadingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBHEADING);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }
    
    /**
     * 모던 스타일 텍스트 필드
     */
    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    /**
     * 모던 스타일 텍스트 에리어
     */
    public static JTextArea createTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setFont(FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 12, 8, 12));
        return area;
    }
    
    /**
     * 모던 스타일 콤보박스
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(BG_SECONDARY);
        return combo;
    }
    
    /**
     * 모던 스타일 셀렉트박스 (헤더용 - 다크 배경에 적합)
     */
    public static <T> JComboBox<T> createModernSelectBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setPreferredSize(new Dimension(220, 38));
        combo.setMinimumSize(new Dimension(220, 38));
        combo.setMaximumSize(new Dimension(220, 38));
        
        // 밝은 배경색 (다크 헤더에서 잘 보이도록)
        combo.setBackground(new Color(255, 255, 255));
        combo.setForeground(TEXT_PRIMARY);
        
        // 모던한 테두리
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // 커스텀 렌더러 (드롭다운 항목 스타일)
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(TEXT_LIGHT);
                } else {
                    c.setBackground(BG_SECONDARY);
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        });
        
        // 호버 효과
        combo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!combo.isPopupVisible()) {
                    combo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2),
                        BorderFactory.createEmptyBorder(7, 11, 7, 11)
                    ));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!combo.isPopupVisible()) {
                    combo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                    ));
                }
            }
        });
        
        return combo;
    }
    
    /**
     * 모던 스타일 테이블 설정
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(40);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        // 헤더 스타일링
        table.getTableHeader().setFont(FONT_SUBHEADING);
        table.getTableHeader().setBackground(BG_PRIMARY);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));
    }
    
    /**
     * 섹션 패널 생성 (제목 있는 카드)
     */
    public static JPanel createSection(String title) {
        JPanel section = new JPanel(new BorderLayout(10, 10));
        section.setBackground(BG_SECONDARY);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE)
        ));
        
        JLabel titleLabel = createSubheadingLabel(title);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        section.add(titleLabel, BorderLayout.NORTH);
        
        return section;
    }
    
    /**
     * 구분선 생성
     */
    public static JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);
        return separator;
    }
    
    /**
     * 배지 레이블 생성 (상태 표시용)
     */
    public static JLabel createBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text);
        badge.setFont(FONT_SMALL);
        badge.setForeground(TEXT_LIGHT);
        badge.setBackground(bgColor);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }
    
    /**
     * 툴팁 설정
     */
    public static void setupTooltips() {
        UIManager.put("ToolTip.background", BG_DARK);
        UIManager.put("ToolTip.foreground", TEXT_LIGHT);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(5, 10, 5, 10));
        UIManager.put("ToolTip.font", FONT_SMALL);
    }
}

