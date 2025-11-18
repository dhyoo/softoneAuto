package com.softone.auto.ui;

import com.softone.auto.model.WeeklyReport;
import com.softone.auto.service.WeeklyReportService;
import com.softone.auto.util.AppContext;
import com.softone.auto.util.ErrorMessageMapper;
import com.softone.auto.util.ExcelReportGenerator;
import com.softone.auto.util.PdfReportGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 주간보고서 작성 패널 (리스트 + 작성 폼)
 */
public class WeeklyReportPanel extends JPanel {
    
    private final WeeklyReportService reportService;
    
    private JTable reportTable;
    private DefaultTableModel tableModel;
    
    private JTextField titleField;
    private JTextField projectNameField;
    private JTextField reporterField;
    private JTextField startDateField;
    private JTextField endDateField;
    
    private JTextField thisWeekRequestField;
    private JTextField thisWeekCompleteField;
    private JTextArea thisWeekTasksArea;
    
    private JTextField nextWeekRequestField;
    private JTextField nextWeekCompleteField;
    private JTextArea nextWeekTasksArea;
    
    private JTextArea issuesArea;
    private JCheckBox[] checkBoxes;
    
    private WeeklyReport currentReport;
    private JButton saveUpdateButton;
    private boolean isNewMode = true;
    private boolean isFormModified = false;  // 폼 수정 여부 체크
    private boolean isProgrammaticUpdate = false;  // 프로그래밍 방식의 업데이트 플래그
    
    // 캐시된 보고서 목록 (스레드 안전 컬렉션 사용)
    private final List<WeeklyReport> cachedReports = new CopyOnWriteArrayList<>();
    
    // 캐시 동기화를 위한 락 객체 (향후 동기화 블록 추가 시 사용 예정)
    @SuppressWarnings("unused")
    private final Object cacheLock = new Object();
    
    public WeeklyReportPanel() {
        this.reportService = new WeeklyReportService();
        
        try {
            isProgrammaticUpdate = true;  // 초기화 시작
        initializeUI();
            isProgrammaticUpdate = false;  // 초기화 완료
            isFormModified = false;  // 명시적으로 false 설정
            
            // UI 초기화 후 데이터 로드
            SwingUtilities.invokeLater(() -> {
                try {
                    loadReports();
                } catch (Exception e) {
                    System.err.println("주간보고서 로드 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("WeeklyReportPanel 초기화 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernDesign.BG_PRIMARY);
        
        // 좌우 분할
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setBorder(null);
        
        // 왼쪽: 보고서 리스트
        splitPane.setLeftComponent(createListPanel());
        
        // 오른쪽: 작성/수정 폼
        splitPane.setRightComponent(createFormPanel());
        
        add(splitPane);
    }
    
    /**
     * 리스트 패널
     */
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ModernDesign.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        
        // 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_PRIMARY);
        
        JLabel titleLabel = ModernDesign.createTitleLabel("보고서 목록");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton newButton = createSmallButton("신규");
        newButton.addActionListener(e -> enterNewMode());
        buttonPanel.add(newButton);
        
        JButton refreshButton = createSmallButton("새로고침");
        refreshButton.addActionListener(e -> loadReports());
        buttonPanel.add(refreshButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        
        // 테이블
        String[] columns = {"보고 기간", "작성자"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        reportTable.setRowHeight(35);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setReorderingAllowed(false);
        
        // ListSelectionListener - 선택 변경 시 (단일 리스너로 통합)
        reportTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SwingUtilities.invokeLater(() -> onReportSelected());
            }
        });
        
        // MouseListener - 마우스 클릭 시 선택만 변경 (ListSelectionListener가 자동 호출)
        reportTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = reportTable.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    reportTable.setRowSelectionInterval(row, row);
                    // ListSelectionListener가 자동으로 호출됨
                }
            }
        });
        
        // KeyListener - 키보드 화살표 키 지원 (선택만 변경)
        reportTable.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP || 
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    // 선택 변경만 수행, ListSelectionListener가 자동 호출됨
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernDesign.BORDER));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 작성/수정 폼 패널 (반응형 레이아웃)
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernDesign.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 15));
        
        // 상단 헤더
        panel.add(createHeaderSection(), BorderLayout.NORTH);
        
        // 중앙 스크롤 패널 (GridBagLayout으로 동적 크기 조정)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(ModernDesign.BG_PRIMARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        
        // 프로젝트 정보 (고정 높이, 작게)
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        contentPanel.add(createBasicInfoSection(), gbc);
        
        // 프로젝트 추진현황 (가장 많은 공간 차지)
        gbc.gridy = 1;
        gbc.weighty = 0.35;
        contentPanel.add(createProgressSection(), gbc);
        
        // 주요 ISSUE 사항 (중간 크기)
        gbc.gridy = 2;
        gbc.weighty = 0.25;
        contentPanel.add(createIssuesSection(), gbc);
        
        // 체크리스트 (작게)
        gbc.gridy = 3;
        gbc.weighty = 0.15;
        contentPanel.add(createChecklistSection(), gbc);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 헤더 섹션
     */
    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_PRIMARY);
        
        JLabel titleLabel = ModernDesign.createTitleLabel("주간보고서 작성");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        saveUpdateButton = UIUtils.createUnifiedButton("저장");
        saveUpdateButton.addActionListener(e -> saveOrUpdateReport());
        buttonPanel.add(saveUpdateButton);
        
        JButton excelButton = UIUtils.createUnifiedButton("Excel");
        excelButton.setPreferredSize(new Dimension(80, 30));
        excelButton.addActionListener(e -> generateExcel());
        buttonPanel.add(excelButton);
        
        JButton pdfButton = UIUtils.createUnifiedButton("PDF");
        pdfButton.setPreferredSize(new Dimension(80, 30));
        pdfButton.addActionListener(e -> generatePdf());
        buttonPanel.add(pdfButton);
        
        JButton deleteButton = UIUtils.createUnifiedButton("삭제");
        deleteButton.addActionListener(e -> deleteReport());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = UIUtils.createUnifiedButton("초기화");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 기본 정보 섹션 (컴팩트하게)
     */
    private JPanel createBasicInfoSection() {
        JPanel section = ModernDesign.createSection("프로젝트 정보");
        section.setPreferredSize(new Dimension(0, 130));  // 날짜 필드가 보이도록 높이 조정
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 8, 2, 8);  // 간격 줄임
        
        int row = 0;
        
        // 첫 번째 행: 제목 | 프로젝트명
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1; gbc.gridwidth = 1;
        formPanel.add(createCompactLabel("제목"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        titleField = createCompactTextField();
        titleField.setText("주간 업무 보고서");
        titleField.getDocument().addDocumentListener(createDocumentListener());
        formPanel.add(titleField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.1;
        formPanel.add(createCompactLabel("프로젝트명"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        projectNameField = createCompactTextField();
        projectNameField.getDocument().addDocumentListener(createDocumentListener());
        var currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany != null) {
            projectNameField.setText(currentCompany.getProjectName());
        }
        formPanel.add(projectNameField, gbc);
        row++;
        
        // 두 번째 행: 작성자 | 보고 기간 (시작~종료)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1;
        formPanel.add(createCompactLabel("작성자"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        reporterField = createCompactTextField();
        reporterField.setText("관리자");
        reporterField.getDocument().addDocumentListener(createDocumentListener());
        formPanel.add(reporterField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.1;
        formPanel.add(createCompactLabel("보고 기간"), gbc);
        
        // 보고 기간 서브 패널 (시작일 ~ 종료일)
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.setBackground(ModernDesign.BG_SECONDARY);
        
        LocalDate monday = getMonday(LocalDate.now());
        startDateField = createCompactTextField();
        startDateField.setText(monday.format(DateTimeFormatter.ISO_LOCAL_DATE));
        startDateField.getDocument().addDocumentListener(createDocumentListener());
        datePanel.add(startDateField);
        
        JLabel tildeLabel = new JLabel("~");
        tildeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        datePanel.add(tildeLabel);
        
        endDateField = createCompactTextField();
        endDateField.setText(monday.plusDays(4).format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.getDocument().addDocumentListener(createDocumentListener());
        datePanel.add(endDateField);
        
        gbc.gridx = 3; gbc.weightx = 0.4;
        formPanel.add(datePanel, gbc);
        
        section.add(formPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    /**
     * 프로젝트 추진현황 섹션 (반응형)
     */
    private JPanel createProgressSection() {
        JPanel section = ModernDesign.createSection("프로젝트 추진현황");
        // 높이 제한 제거 - 동적으로 조정됨
        
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        contentPanel.add(createThisWeekPanel());
        contentPanel.add(createNextWeekPanel());
        
        section.add(contentPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createThisWeekPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ModernDesign.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)  // 패딩 줄임
        ));
        
        JLabel titleLabel = new JLabel("금주 주요 수행 업무");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        titleLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        statsPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        statsPanel.add(createTinyLabel("요청:"));
        thisWeekRequestField = createTinyTextField();
        thisWeekRequestField.setText("0");
        statsPanel.add(thisWeekRequestField);
        
        statsPanel.add(createTinyLabel("완료:"));
        thisWeekCompleteField = createTinyTextField();
        thisWeekCompleteField.setText("0");
        statsPanel.add(thisWeekCompleteField);
        
        thisWeekTasksArea = new JTextArea(6, 30);  // 행 수 줄임
        thisWeekTasksArea.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        thisWeekTasksArea.setLineWrap(true);
        thisWeekTasksArea.setWrapStyleWord(true);
        thisWeekTasksArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)  // 패딩 줄임
        ));
        
        // JSON 파싱 에러 방지를 위한 특수문자 필터 적용
        ((javax.swing.text.AbstractDocument) thisWeekTasksArea.getDocument()).setDocumentFilter(
            new javax.swing.text.DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) 
                        throws javax.swing.text.BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, text, attrs);
                        return;
                    }
                    // JSON에서 문제될 수 있는 문자 제거: 백슬래시, 큰따옴표
                    String filtered = text.replace("\\", "").replace("\"", "");
                    super.replace(fb, offset, length, filtered, attrs);
                }
            });
        
        thisWeekTasksArea.getDocument().addDocumentListener(createDocumentListener());
        
        JPanel contentPanel = new JPanel(new BorderLayout(3, 3));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(thisWeekTasksArea), BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNextWeekPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ModernDesign.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)  // 패딩 줄임
        ));
        
        JLabel titleLabel = new JLabel("차주 주요 수행 계획");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        titleLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        statsPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        statsPanel.add(createTinyLabel("요청:"));
        nextWeekRequestField = createTinyTextField();
        nextWeekRequestField.setText("0");
        statsPanel.add(nextWeekRequestField);
        
        statsPanel.add(createTinyLabel("완료:"));
        nextWeekCompleteField = createTinyTextField();
        nextWeekCompleteField.setText("0");
        statsPanel.add(nextWeekCompleteField);
        
        nextWeekTasksArea = new JTextArea(6, 30);  // 행 수 줄임
        nextWeekTasksArea.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        nextWeekTasksArea.setLineWrap(true);
        nextWeekTasksArea.setWrapStyleWord(true);
        nextWeekTasksArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)  // 패딩 줄임
        ));
        
        // JSON 파싱 에러 방지를 위한 특수문자 필터 적용
        ((javax.swing.text.AbstractDocument) nextWeekTasksArea.getDocument()).setDocumentFilter(
            new javax.swing.text.DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) 
                        throws javax.swing.text.BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, text, attrs);
                        return;
                    }
                    // JSON에서 문제될 수 있는 문자 제거: 백슬래시, 큰따옴표
                    String filtered = text.replace("\\", "").replace("\"", "");
                    super.replace(fb, offset, length, filtered, attrs);
                }
            });
        
        nextWeekTasksArea.getDocument().addDocumentListener(createDocumentListener());
        
        JPanel contentPanel = new JPanel(new BorderLayout(3, 3));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(nextWeekTasksArea), BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 주요 ISSUE 사항 섹션 (반응형)
     */
    private JPanel createIssuesSection() {
        JPanel section = ModernDesign.createSection("주요 ISSUE 사항");
        // 높이 제한 제거 - 동적으로 조정됨
        
        issuesArea = new JTextArea(6, 60);  // 행 수 줄임
        issuesArea.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        issuesArea.setLineWrap(true);
        issuesArea.setWrapStyleWord(true);
        issuesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)  // 패딩 줄임
        ));
        issuesArea.getDocument().addDocumentListener(createDocumentListener());
        
        // 기본 섹션 제목 설정
        issuesArea.setText(
            "■ 전체 프로젝트\n" +
            "  - \n\n" +
            "■ 당사 참여부문\n" +
            "  - \n\n" +
            "■ 고객사 주요 동향\n" +
            "  - \n\n" +
            "■ 경쟁회사 주요 동향\n" +
            "  - \n\n" +
            "■ 기타\n" +
            "  - "
        );
        
        JScrollPane scrollPane = new JScrollPane(issuesArea);
        scrollPane.setBorder(null);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        section.add(contentPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    /**
     * 주요 10가지 Check 사항 섹션 (반응형)
     */
    private JPanel createChecklistSection() {
        JPanel section = ModernDesign.createSection("주요 10가지 Check 사항");
        // 높이 제한 제거 - 동적으로 조정됨
        
        JPanel checkPanel = new JPanel(new GridLayout(5, 2, 12, 3));  // 간격 줄임
        checkPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        String[] checkItems = {
            "고객사 보안 규정 준수",
            "품질(납기, 생산성) 양호",
            "불법 S/W 미사용",
            "사고 대응 체계 숙지",
            "고객사 생산성 향상 노력",
            "고객 중심적 업무 수행",
            "회사 대표 의식",
            "성실한 근태",
            "단정한 복장",
            "자기/회사 발전 노력"
        };
        
        checkBoxes = new JCheckBox[10];
        for (int i = 0; i < checkItems.length; i++) {
            checkBoxes[i] = new JCheckBox(checkItems[i]);
            checkBoxes[i].setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            checkBoxes[i].setBackground(ModernDesign.BG_SECONDARY);
            checkBoxes[i].setSelected(true);
            
            // 체크박스 변경 시 수정 플래그 설정
            checkBoxes[i].addItemListener(e -> {
                if (!isProgrammaticUpdate) {
                    isFormModified = true;
                }
            });
            
            checkPanel.add(checkBoxes[i]);
        }
        
        section.add(checkPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    private JLabel createCompactLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        label.setForeground(ModernDesign.TEXT_SECONDARY);
        return label;
    }
    
    private JLabel createTinyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        label.setForeground(ModernDesign.TEXT_SECONDARY);
        return label;
    }
    
    private JTextField createCompactTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        field.setPreferredSize(new Dimension(150, 26));
        return field;
    }
    
    private JTextField createTinyTextField() {
        JTextField field = new JTextField(4);
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        field.setPreferredSize(new Dimension(50, 24));
        return field;
    }
    
    /**
     * 보고서 목록 로드 (public 메서드로 외부에서 호출 가능)
     */
    public void loadReports() {
        System.out.println("=== loadReports() 시작 ===");
        
        synchronized (cacheLock) {
            // 현재 선택된 보고서 ID 저장 (복원용)
            String currentReportId = (currentReport != null) ? currentReport.getId() : null;
            System.out.println("  현재 선택된 보고서 ID: " + currentReportId);
            
            // 이전 캐시 백업 (예외 발생 시 복원용)
            List<WeeklyReport> backupReports = new ArrayList<>(cachedReports);
            int previousRowCount = tableModel.getRowCount();
            
            try {
                // 현재 회사 확인
                var currentCompany = AppContext.getInstance().getCurrentCompany();
                System.out.println("  현재 회사: " + (currentCompany != null ? currentCompany.getName() : "없음"));
                
                // 보고서 목록 조회
                List<WeeklyReport> newReports = reportService.getAllReports();
                System.out.println("  조회된 보고서: " + newReports.size() + "건");
                
                // 최신순 정렬 (시작일 기준 내림차순)
                newReports.sort((r1, r2) -> r2.getStartDate().compareTo(r1.getStartDate()));
                System.out.println("  정렬 완료");
                
                // 캐시 업데이트 (원자적)
                cachedReports.clear();
                cachedReports.addAll(newReports);
                System.out.println("  캐시 업데이트 완료: " + cachedReports.size() + "건");
                
                // UI 업데이트는 EDT에서 실행
                final List<WeeklyReport> reportsForUI = new ArrayList<>(newReports);
                final String finalCurrentReportId = currentReportId;
                SwingUtilities.invokeLater(() -> {
                    synchronized (cacheLock) {
                        updateTableModel(reportsForUI, finalCurrentReportId);
                    }
                });
                
                System.out.println("=== loadReports() 완료 ===\n");
                
            } catch (Exception e) {
                System.err.println("✗ 주간보고서 로드 실패: " + e.getMessage());
                ErrorMessageMapper.logError("보고서 로드", e);
                
                // 예외 발생 시 백업으로 복원
                System.out.println("  → 백업 데이터로 복원 시도");
                cachedReports.clear();
                cachedReports.addAll(backupReports);
                
                // UI도 복원
                final List<WeeklyReport> finalBackupReports = new ArrayList<>(backupReports);
                final int finalPreviousRowCount = previousRowCount;
                SwingUtilities.invokeLater(() -> {
                    synchronized (cacheLock) {
                        restoreTableModel(finalBackupReports, finalPreviousRowCount);
                    }
                });
                
                String userMessage = ErrorMessageMapper.getUserFriendlyMessage(e);
                JOptionPane.showMessageDialog(this, 
                    "보고서 목록을 불러오는 중 오류가 발생했습니다:\n\n" + userMessage + 
                    "\n\n이전 목록을 유지합니다.", 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 테이블 모델 업데이트 (동기화된 메서드)
     */
    private void updateTableModel(List<WeeklyReport> reports, String currentReportId) {
        // 테이블 초기화
        tableModel.setRowCount(0);
        System.out.println("  테이블 초기화 완료");
        
        // 테이블에 표시
        int restoreIndex = -1;
        for (int i = 0; i < reports.size(); i++) {
            WeeklyReport report = reports.get(i);
            try {
                tableModel.addRow(new Object[]{
                    report.getStartDate().format(DateTimeFormatter.ofPattern("MM/dd")) + 
                    " ~ " + report.getEndDate().format(DateTimeFormatter.ofPattern("MM/dd")),
                    report.getReporter()
                });
                
                // 이전 선택 보고서 인덱스 찾기
                if (currentReportId != null && report.getId().equals(currentReportId)) {
                    restoreIndex = i;
                }
            } catch (Exception rowEx) {
                System.err.println("  ✗ 행 추가 실패 (index " + i + "): " + rowEx.getMessage());
            }
        }
        
        System.out.println("  테이블에 " + tableModel.getRowCount() + "개 행 추가됨");
        
        // 이전 선택 복원 (ID 기준)
        if (restoreIndex >= 0) {
            System.out.println("  이전 선택 복원: 행 " + restoreIndex);
            if (restoreIndex < reportTable.getRowCount()) {
                reportTable.setRowSelectionInterval(restoreIndex, restoreIndex);
            }
        } else if (reports.isEmpty()) {
            System.out.println("  ⚠ 보고서 데이터 없음 - 신규 모드로 전환");
            currentReport = null;
            enterNewMode();
        } else {
            // 복원할 선택이 없지만 데이터는 있음 - 첫 번째 행 선택
            System.out.println("  첫 번째 행 자동 선택");
            if (reportTable.getRowCount() > 0) {
                reportTable.setRowSelectionInterval(0, 0);
                reportTable.scrollRectToVisible(reportTable.getCellRect(0, 0, true));
            }
        }
    }
    
    /**
     * 테이블 모델 복원 (동기화된 메서드)
     */
    private void restoreTableModel(List<WeeklyReport> reports, int previousRowCount) {
        if (previousRowCount > 0 && tableModel.getRowCount() == 0) {
            System.out.println("  → 테이블 복원 시도");
            for (WeeklyReport report : reports) {
                try {
                    tableModel.addRow(new Object[]{
                        report.getStartDate().format(DateTimeFormatter.ofPattern("MM/dd")) + 
                        " ~ " + report.getEndDate().format(DateTimeFormatter.ofPattern("MM/dd")),
                        report.getReporter()
                    });
                } catch (Exception restoreEx) {
                    // 복원 실패는 무시
                }
            }
            System.out.println("  → 테이블 복원 완료: " + tableModel.getRowCount() + "개 행");
        }
    }
    
    /**
     * 보고서 선택 이벤트 (단일 클릭으로 로드)
     */
    private void onReportSelected() {
        int selectedRow = reportTable.getSelectedRow();
        System.out.println("=== onReportSelected() 시작 ===");
        System.out.println("  선택된 행: " + selectedRow);
        
        synchronized (cacheLock) {
            System.out.println("  캐시된 보고서 수: " + cachedReports.size());
            
            if (selectedRow < 0) {
                System.out.println("  → 선택된 행 없음, 종료\n");
                return;
            }
            
            // 캐시된 목록이 비어있으면 다시 로드 (동기화 블록 밖에서 호출)
            if (cachedReports.isEmpty()) {
                System.out.println("  ⚠ 캐시 비어있음, 다시 로드");
            }
            
            // 인덱스 범위 체크
            if (selectedRow >= cachedReports.size()) {
                System.err.println("  ✗ 인덱스 초과: " + selectedRow + " >= " + cachedReports.size());
                System.err.println("  → 목록 다시 로드");
            }
            
            // 동기화 블록 밖에서 loadReports 호출 (데드락 방지)
            if (cachedReports.isEmpty() || selectedRow >= cachedReports.size()) {
                SwingUtilities.invokeLater(() -> loadReports());
                return;
            }
            
            // 작성 중인 내용이 있으면 경고
            if (isFormModified) {
                System.out.println("  → 폼이 수정됨, 경고 표시");
                int result = JOptionPane.showConfirmDialog(this,
                    "작성 중인 내용이 있습니다.\n저장하지 않고 다른 보고서를 불러오시겠습니까?",
                    "경고",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (result != JOptionPane.YES_OPTION) {
                    System.out.println("  → 사용자가 취소함\n");
                    // 이전 선택으로 복원
                    if (currentReport != null) {
                        for (int i = 0; i < cachedReports.size(); i++) {
                            if (cachedReports.get(i).getId().equals(currentReport.getId())) {
                                reportTable.setRowSelectionInterval(i, i);
                                break;
                            }
                        }
                    }
                    return;
                }
                System.out.println("  → 사용자가 확인함, 계속 진행");
            }
            
            try {
                // 캐시된 목록에서 바로 가져오기
                currentReport = cachedReports.get(selectedRow);
                System.out.println("  → 선택된 보고서:");
                System.out.println("     ID: " + currentReport.getId());
                System.out.println("     제목: " + currentReport.getTitle());
                System.out.println("     기간: " + currentReport.getStartDate() + " ~ " + currentReport.getEndDate());
                
                enterEditMode();
                System.out.println("  → 수정 모드 진입");
                
                loadReportToForm(currentReport);
                System.out.println("  → 폼 로드 완료");
                
                isFormModified = false;
                System.out.println("  → isFormModified = false");
                System.out.println("=== onReportSelected() 완료 ===\n");
                
            } catch (Exception e) {
                System.err.println("✗ 보고서 로드 중 오류: " + e.getMessage());
                ErrorMessageMapper.logError("보고서 선택", e);
                
                String userMessage = ErrorMessageMapper.getUserFriendlyMessage(e);
                JOptionPane.showMessageDialog(this, 
                    "보고서를 불러오는 중 오류가 발생했습니다:\n\n" + userMessage, 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    /**
     * 보고서 데이터를 폼에 로드
     */
    private void loadReportToForm(WeeklyReport report) {
        System.out.println("=== 보고서 폼에 로드 ===");
        System.out.println("  제목: " + report.getTitle());
        System.out.println("  기간: " + report.getStartDate() + " ~ " + report.getEndDate());
        System.out.println("  프로젝트: " + report.getProjectName());
        
        // 프로그래밍 방식의 업데이트 시작
        isProgrammaticUpdate = true;
        
        // 기본 정보
        titleField.setText(report.getTitle() != null ? report.getTitle() : "");
        projectNameField.setText(report.getProjectName() != null ? report.getProjectName() : "");
        reporterField.setText(report.getReporter() != null ? report.getReporter() : "");
        startDateField.setText(report.getStartDate() != null ? 
            report.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        endDateField.setText(report.getEndDate() != null ? 
            report.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        
        // 금주 업무 (텍스트 그대로 로드)
        thisWeekTasksArea.setText(report.getThisWeekTasksText() != null ? report.getThisWeekTasksText() : "");
        
        // 금주 요청/완료 건수
        thisWeekRequestField.setText(String.valueOf(report.getThisWeekRequestCount() != null ? report.getThisWeekRequestCount() : 0));
        thisWeekCompleteField.setText(String.valueOf(report.getThisWeekCompleteCount() != null ? report.getThisWeekCompleteCount() : 0));
        
        // 차주 계획 (텍스트 그대로 로드)
        nextWeekTasksArea.setText(report.getNextWeekTasksText() != null ? report.getNextWeekTasksText() : "");
        
        // 차주 요청/완료 건수
        nextWeekRequestField.setText(String.valueOf(report.getNextWeekRequestCount() != null ? report.getNextWeekRequestCount() : 0));
        nextWeekCompleteField.setText(String.valueOf(report.getNextWeekCompleteCount() != null ? report.getNextWeekCompleteCount() : 0));
        
        // 이슈 사항
        String notes = report.getAdditionalNotes() != null ? report.getAdditionalNotes() : "";
        if (notes.isEmpty()) {
            // 기본 템플릿 설정
            notes = "■ 전체 프로젝트\n" +
                    "  - \n\n" +
                    "■ 당사 참여부문\n" +
                    "  - \n\n" +
                    "■ 고객사 주요 동향\n" +
                    "  - \n\n" +
                    "■ 경쟁회사 주요 동향\n" +
                    "  - \n\n" +
                    "■ 기타\n" +
                    "  - ";
        }
        issuesArea.setText(notes);
        
        // 체크리스트 로드
        if (checkBoxes != null) {
            List<Boolean> checkItems = report.getCheckItems();
            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkItems != null && i < checkItems.size()) {
                    checkBoxes[i].setSelected(checkItems.get(i));
                } else {
                    checkBoxes[i].setSelected(false);
                }
            }
        }
        
        // 프로그래밍 방식의 업데이트 종료
        isProgrammaticUpdate = false;
        isFormModified = false;  // 로드는 수정으로 간주하지 않음
        
        System.out.println("  ✓ 폼 로드 완료");
        System.out.println("    - 금주 업무: " + (report.getThisWeekTasksText() != null ? report.getThisWeekTasksText().length() : 0) + "자");
        System.out.println("    - 차주 계획: " + (report.getNextWeekTasksText() != null ? report.getNextWeekTasksText().length() : 0) + "자");
    }
    
    /**
     * 신규 모드 진입
     */
    private void enterNewMode() {
        System.out.println("=== enterNewMode() 시작 ===");
        
        // 작성 중인 내용이 있으면 경고
        if (isFormModified) {
            System.out.println("  → 폼이 수정됨, 경고 표시");
            int result = JOptionPane.showConfirmDialog(this,
                "작성 중인 내용이 있습니다.\n저장하지 않고 새로 작성하시겠습니까?",
                "경고",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result != JOptionPane.YES_OPTION) {
                System.out.println("  → 사용자가 취소함");
                return;
            }
            System.out.println("  → 사용자가 확인함");
        }
        
        isNewMode = true;
        currentReport = null;
        System.out.println("  → isNewMode = true, currentReport = null");
        
        clearForm();
        System.out.println("  → 폼 초기화 완료");
        
        saveUpdateButton.setText("저장");
        reportTable.clearSelection();
        titleField.requestFocus();
        isFormModified = false;
        
        System.out.println("=== enterNewMode() 완료 ===\n");
    }
    
    /**
     * 수정 모드로 전환
     */
    private void enterEditMode() {
        System.out.println("  → enterEditMode(): isNewMode = false, 버튼 = '수정'");
        isNewMode = false;
        saveUpdateButton.setText("수정");
    }
    
    /**
     * 저장 또는 수정
     */
    private void saveOrUpdateReport() {
        try {
            String title = titleField.getText().trim();
            String projectName = projectNameField.getText().trim();
            String reporter = reporterField.getText().trim();
            LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
            LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
            
            if (title.isEmpty() || projectName.isEmpty() || reporter.isEmpty()) {
                JOptionPane.showMessageDialog(this, "필수 항목을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (isNewMode) {
                // 중복 체크
                if (reportService.existsByStartDate(startDate)) {
                    int result = JOptionPane.showConfirmDialog(this, 
                        "해당 기간의 보고서가 이미 존재합니다.\n덮어쓰시겠습니까?", 
                        "중복 확인", 
                        JOptionPane.YES_NO_OPTION);
                    
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                    
                    // 기존 보고서 가져오기
                    currentReport = reportService.getReportByStartDate(startDate);
                    isNewMode = false;
                }
            }
            
            if (isNewMode) {
                currentReport = reportService.createReport(title, startDate, endDate, projectName, reporter);
                System.out.println("  → 신규 보고서 생성: ID=" + currentReport.getId() + ", companyId=" + currentReport.getCompanyId());
            } else {
                // 수정 모드: companyId 확인 및 복원
                String existingCompanyId = currentReport.getCompanyId();
                System.out.println("  → 기존 보고서 수정: ID=" + currentReport.getId() + ", companyId=" + existingCompanyId);
                
                currentReport.setTitle(title);
                currentReport.setProjectName(projectName);
                currentReport.setReporter(reporter);
                currentReport.setStartDate(startDate);
                currentReport.setEndDate(endDate);
                
                // companyId가 null이면 현재 회사로 설정
                if (currentReport.getCompanyId() == null) {
                    var currentCompany = AppContext.getInstance().getCurrentCompany();
                    if (currentCompany != null) {
                        currentReport.setCompanyId(currentCompany.getId());
                        System.out.println("  ⚠ companyId가 null이어서 현재 회사로 설정: " + currentCompany.getId());
                    } else {
                        System.err.println("  ✗ 치명적 오류: companyId와 현재 회사 모두 없음!");
                        throw new IllegalStateException("회사 정보가 없습니다. 회사를 먼저 선택해주세요.");
                    }
                }
                
                System.out.println("  → 수정 후 companyId: " + currentReport.getCompanyId());
            }
            
            // 금주 업무 저장 (텍스트 그대로, 파싱 없음)
            String thisWeekText = thisWeekTasksArea.getText().trim();
            currentReport.setThisWeekTasksText(thisWeekText);
            System.out.println("  금주 업무 텍스트 저장 완료: " + thisWeekText.length() + "자");
            
            // 차주 계획 저장 (텍스트 그대로, 파싱 없음)
            String nextWeekText = nextWeekTasksArea.getText().trim();
            currentReport.setNextWeekTasksText(nextWeekText);
            System.out.println("  차주 계획 텍스트 저장 완료: " + nextWeekText.length() + "자");
            
            // 이슈 사항
            currentReport.setAdditionalNotes(issuesArea.getText());
            
            // 체크리스트 저장
            currentReport.getCheckItems().clear();
            for (JCheckBox checkBox : checkBoxes) {
                currentReport.getCheckItems().add(checkBox.isSelected());
            }
            
            // 요청/완료 건수 저장 (수동 입력 값)
            try {
                currentReport.setThisWeekRequestCount(Integer.parseInt(thisWeekRequestField.getText().trim()));
            } catch (NumberFormatException e) {
                currentReport.setThisWeekRequestCount(0);
            }
            try {
                currentReport.setThisWeekCompleteCount(Integer.parseInt(thisWeekCompleteField.getText().trim()));
            } catch (NumberFormatException e) {
                currentReport.setThisWeekCompleteCount(0);
            }
            try {
                currentReport.setNextWeekRequestCount(Integer.parseInt(nextWeekRequestField.getText().trim()));
            } catch (NumberFormatException e) {
                currentReport.setNextWeekRequestCount(0);
            }
            try {
                currentReport.setNextWeekCompleteCount(Integer.parseInt(nextWeekCompleteField.getText().trim()));
            } catch (NumberFormatException e) {
                currentReport.setNextWeekCompleteCount(0);
            }
            
            System.out.println("=== 보고서 저장 시작 ===");
            System.out.println("  신규 모드: " + isNewMode);
            System.out.println("  보고서 ID: " + currentReport.getId());
            System.out.println("  제목: " + currentReport.getTitle());
            System.out.println("  기간: " + currentReport.getStartDate() + " ~ " + currentReport.getEndDate());
            System.out.println("  금주 업무: " + currentReport.getThisWeekTasksText().length() + "자 (요청: " + currentReport.getThisWeekRequestCount() + ", 완료: " + currentReport.getThisWeekCompleteCount() + ")");
            System.out.println("  차주 계획: " + currentReport.getNextWeekTasksText().length() + "자 (요청: " + currentReport.getNextWeekRequestCount() + ", 완료: " + currentReport.getNextWeekCompleteCount() + ")");
            System.out.println("  체크리스트: " + currentReport.getCheckItems().size() + "개");
            
            // 저장 전에 현재 보고서 백업
            WeeklyReport savedReport = currentReport;
            
            reportService.updateReport(currentReport);
            System.out.println("  → 저장 완료");
            
            isFormModified = false;
            System.out.println("  → isFormModified = false");
            
            // 저장된 보고서 ID 확인
            String savedReportId = savedReport.getId();
            System.out.println("  → 저장된 보고서 ID: " + savedReportId);
            
            // 목록 갱신 (currentReport 유지됨)
            System.out.println("  → 목록 갱신 시작");
            loadReports();
            System.out.println("  → 목록 갱신 완료");
            
            // 수정 모드로 전환 (저장 후에는 항상 수정 모드)
            enterEditMode();
            System.out.println("  → enterEditMode() 호출 완료");
            
            // 성공 메시지는 마지막에 표시
            JOptionPane.showMessageDialog(this, 
                "✅ 보고서가 저장되었습니다.", 
                "완료", 
                JOptionPane.INFORMATION_MESSAGE);
            
            System.out.println("=== 보고서 저장 완료 ===\n");
            
        } catch (Exception e) {
            ErrorMessageMapper.logError("보고서 저장", e);
            String userMessage = ErrorMessageMapper.getUserFriendlyMessage(e);
            JOptionPane.showMessageDialog(this, 
                "보고서 저장 중 오류가 발생했습니다:\n\n" + userMessage, 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Excel 생성
     */
    private void generateExcel() {
        try {
            if (currentReport == null) {
                saveOrUpdateReport();
            }
            if (currentReport != null) {
                String fileName = ExcelReportGenerator.generateWeeklyReport(currentReport);
                if (fileName != null) {
                    JOptionPane.showMessageDialog(this, 
                            "Excel 파일이 생성되었습니다:\n" + new File(fileName).getAbsolutePath(),
                            "완료",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Excel 생성 실패", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            ErrorMessageMapper.logError("Excel 생성", e);
            String userMessage = ErrorMessageMapper.getUserFriendlyMessage(e);
            JOptionPane.showMessageDialog(this, 
                    "Excel 파일 생성 중 오류가 발생했습니다:\n\n" + userMessage,
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * PDF 생성
     */
    private void generatePdf() {
        try {
            if (currentReport == null) {
                saveOrUpdateReport();
            }
            if (currentReport != null) {
                String fileName = PdfReportGenerator.generateWeeklyReport(currentReport);
                if (fileName != null) {
                    JOptionPane.showMessageDialog(this, 
                            "PDF 파일이 생성되었습니다:\n" + new File(fileName).getAbsolutePath(),
                            "완료",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "PDF 생성 실패", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            ErrorMessageMapper.logError("PDF 생성", e);
            String userMessage = ErrorMessageMapper.getUserFriendlyMessage(e);
            JOptionPane.showMessageDialog(this, 
                    "PDF 파일 생성 중 오류가 발생했습니다:\n\n" + userMessage,
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 보고서 삭제
     */
    private void deleteReport() {
        System.out.println("=== deleteReport() 시작 ===");
        
        if (currentReport == null) {
            System.out.println("  ⚠ 선택된 보고서 없음");
            JOptionPane.showMessageDialog(this, "삭제할 보고서를 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        System.out.println("  삭제할 보고서: " + currentReport.getTitle() + " (ID: " + currentReport.getId() + ")");
        
        int result = JOptionPane.showConfirmDialog(this, 
            "정말 삭제하시겠습니까?", 
            "삭제 확인", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            System.out.println("  → 삭제 확인");
            reportService.deleteReport(currentReport.getId());
            System.out.println("  → 삭제 완료");
            
            JOptionPane.showMessageDialog(this, "삭제되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            
            currentReport = null;
            System.out.println("  → currentReport = null");
            
            loadReports();
            enterNewMode();
            System.out.println("=== deleteReport() 완료 ===\n");
        } else {
            System.out.println("  → 삭제 취소");
        }
    }
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        isProgrammaticUpdate = true;  // 프로그래밍 방식의 업데이트 시작
        
        titleField.setText("주간 업무 보고서");
        projectNameField.setText("");
        var currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany != null) {
            projectNameField.setText(currentCompany.getProjectName());
        }
        reporterField.setText("관리자");
        LocalDate monday = getMonday(LocalDate.now());
        startDateField.setText(monday.format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setText(monday.plusDays(4).format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        thisWeekRequestField.setText("0");
        thisWeekCompleteField.setText("0");
        thisWeekTasksArea.setText("");
        
        nextWeekRequestField.setText("0");
        nextWeekCompleteField.setText("0");
        nextWeekTasksArea.setText("");
        
        // 기본 섹션 제목으로 초기화
        issuesArea.setText(
            "■ 전체 프로젝트\n" +
            "  - \n\n" +
            "■ 당사 참여부문\n" +
            "  - \n\n" +
            "■ 고객사 주요 동향\n" +
            "  - \n\n" +
            "■ 경쟁회사 주요 동향\n" +
            "  - \n\n" +
            "■ 기타\n" +
            "  - "
        );
        
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setSelected(true);
        }
        
        isProgrammaticUpdate = false;  // 프로그래밍 방식의 업데이트 종료
        isFormModified = false;  // 초기화 시 수정 플래그 초기화
    }
    
    /**
     * DocumentListener 생성 (필드 변경 감지)
     */
    private javax.swing.event.DocumentListener createDocumentListener() {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!isProgrammaticUpdate) {
                    isFormModified = true;
                }
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (!isProgrammaticUpdate) {
                    isFormModified = true;
                }
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                if (!isProgrammaticUpdate) {
                    isFormModified = true;
                }
            }
        };
    }
    
    private LocalDate getMonday(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }
    
    /**
     * 작은 버튼 생성 (목록용)
     */
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        button.setForeground(ModernDesign.TEXT_LIGHT);
        button.setBackground(ModernDesign.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(65, 26));
        button.setMinimumSize(new Dimension(65, 26));
        button.setMaximumSize(new Dimension(65, 26));
        
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
