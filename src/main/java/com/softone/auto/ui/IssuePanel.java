package com.softone.auto.ui;

import com.softone.auto.model.Issue;
import com.softone.auto.service.IssueService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 이슈 관리 패널
 */
public class IssuePanel extends JPanel {
    
    private final IssueService issueService;
    
    private JTable issueTable;
    private DefaultTableModel tableModel;
    
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> severityCombo;
    private JComboBox<String> statusCombo;
    private JTextField reporterField;
    private JTextField assigneeField;
    private JTextArea resolutionArea;
    private JTextArea notesArea;
    
    private Issue selectedIssue;
    private JButton saveUpdateButton;  // 동적으로 변경되는 버튼
    private boolean isNewMode = false;  // 신규 모드 플래그
    
    public IssuePanel() {
        this.issueService = new IssueService();
        try {
            initializeUI();
            loadIssues();
        } catch (Exception e) {
            System.err.println("IssuePanel 초기화 오류: " + e.getMessage());
            e.printStackTrace();
            try {
                initializeUI();
            } catch (Exception uiEx) {
                System.err.println("UI 초기화도 실패: " + uiEx.getMessage());
            }
        }
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernDesign.BG_PRIMARY);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(ModernDesign.BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // 상단 헤더
        mainPanel.add(createHeaderSection(), BorderLayout.NORTH);
        
        // 중앙: 분할 패널
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.0);  // 좌측 목록 크기 고정 (0.0 = 좌측 고정, 1.0 = 우측 고정)
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        
        // 왼쪽: 테이블
        JPanel tablePanel = createTablePanel();
        tablePanel.setPreferredSize(new Dimension(400, 0));
        tablePanel.setMinimumSize(new Dimension(400, 0));
        tablePanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        splitPane.setLeftComponent(tablePanel);
        
        // 오른쪽: 입력 폼
        splitPane.setRightComponent(createFormPanel());
        
        // 초기 divider 위치 설정 (컴포넌트가 표시된 후에 설정)
        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(400);  // 좌측 목록을 400px로 고정
        });
        
        // 창 크기 변경 시 divider 위치 유지
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (splitPane.getDividerLocation() != 400) {
                        splitPane.setDividerLocation(400);
                    }
                });
            }
        });
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    /**
     * 헤더 섹션 생성
     */
    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_PRIMARY);
        
        JLabel titleLabel = ModernDesign.createTitleLabel("🐛 이슈 관리");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton refreshButton = ModernDesign.createSecondaryButton("🔄 새로고침");
        refreshButton.addActionListener(e -> loadIssues());
        buttonPanel.add(refreshButton);
        
        JButton openButton = ModernDesign.createSecondaryButton("📋 미해결만");
        openButton.addActionListener(e -> loadOpenIssues());
        buttonPanel.add(openButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 테이블 패널 생성
     */
    private JPanel createTablePanel() {
        JPanel panel = ModernDesign.createSection("이슈 목록");
        
        // 테이블
        String[] columnNames = {"제목", "카테고리", "심각도", "상태", "보고자", "담당자"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        issueTable = new JTable(tableModel);
        ModernDesign.styleTable(issueTable);
        
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onIssueSelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(issueTable);
        scrollPane.setBorder(null);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 입력 폼 패널 생성 (컴팩트하고 유연한 레이아웃)
     */
    private JPanel createFormPanel() {
        JPanel panel = ModernDesign.createSection("이슈 정보");
        
        // 폼 패널 - 유연한 레이아웃
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);
        
        int row = 0;
        
        // 제목
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; gbc.weighty = 0.0;
        formPanel.add(createCompactLabel("제목 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        titleField = createCompactTextField();
        formPanel.add(titleField, gbc);
        row++;
        
        // 카테고리
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("카테고리"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        categoryCombo = createCompactCombo(new String[]{"기술", "일정", "인력", "기타"});
        formPanel.add(categoryCombo, gbc);
        
        // 심각도
        gbc.gridx = 2; gbc.weightx = 0.5;
        severityCombo = createCompactCombo(new String[]{"높음", "보통", "낮음"});
        formPanel.add(severityCombo, gbc);
        row++;
        
        // 상태
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("상태"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        statusCombo = createCompactCombo(new String[]{"OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"});
        formPanel.add(statusCombo, gbc);
        row++;
        
        // 보고자
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("보고자"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        reporterField = createCompactTextField();
        formPanel.add(reporterField, gbc);
        
        // 담당자
        gbc.gridx = 2; gbc.weightx = 0.5;
        assigneeField = createCompactTextField();
        formPanel.add(assigneeField, gbc);
        row++;
        
        // 상세 내용
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(createCompactLabel("상세 내용"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(descriptionArea, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // 해결 방안
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(createCompactLabel("해결 방안"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.BOTH;
        resolutionArea = new JTextArea(2, 20);
        resolutionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        resolutionArea.setLineWrap(true);
        resolutionArea.setWrapStyleWord(true);
        resolutionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(resolutionArea, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // 비고
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(createCompactLabel("비고"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(2, 20);
        notesArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(notesArea, gbc);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        
        // 버튼 패널
        contentPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 컴팩트 레이블 생성
     */
    private JLabel createCompactLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        label.setForeground(ModernDesign.TEXT_SECONDARY);
        label.setMinimumSize(new Dimension(80, 25));
        label.setPreferredSize(new Dimension(110, 25));
        return label;
    }
    
    /**
     * 컴팩트 텍스트 필드 생성 (유연한 크기)
     */
    private JTextField createCompactTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        field.setMinimumSize(new Dimension(150, 32));
        field.setPreferredSize(new Dimension(250, 32));
        return field;
    }
    
    /**
     * 컴팩트 콤보박스 생성 (유연한 크기)
     */
    private JComboBox<String> createCompactCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        combo.setBackground(ModernDesign.BG_SECONDARY);
        combo.setMinimumSize(new Dimension(150, 32));
        combo.setPreferredSize(new Dimension(250, 32));
        return combo;
    }
    
    /**
     * 버튼 패널 생성 (심플하고 작게)
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JButton newButton = createCompactButton("신규");
        newButton.addActionListener(e -> enterNewMode());
        buttonPanel.add(newButton);
        
        // 동적 버튼 (저장/수정)
        saveUpdateButton = createCompactButton("저장");
        saveUpdateButton.addActionListener(e -> saveOrUpdate());
        buttonPanel.add(saveUpdateButton);
        
        JButton deleteButton = createCompactButton("삭제");
        deleteButton.addActionListener(e -> deleteIssue());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = createCompactButton("초기화");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        return buttonPanel;
    }
    
    /**
     * 컴팩트 버튼 생성 (통일된 스타일)
     */
    private JButton createCompactButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        button.setForeground(ModernDesign.TEXT_LIGHT);
        button.setBackground(ModernDesign.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 모든 버튼 동일한 크기로 고정
        button.setPreferredSize(new Dimension(80, 30));
        button.setMinimumSize(new Dimension(80, 30));
        button.setMaximumSize(new Dimension(80, 30));
        
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
    
    /**
     * 신규 모드 진입
     */
    private void enterNewMode() {
        isNewMode = true;
        clearForm();
        saveUpdateButton.setText("저장");
        issueTable.clearSelection();
        titleField.requestFocus();
    }
    
    /**
     * 수정 모드로 전환
     */
    private void enterEditMode() {
        isNewMode = false;
        saveUpdateButton.setText("수정");
    }
    
    /**
     * 저장 또는 수정 실행
     */
    private void saveOrUpdate() {
        if (isNewMode) {
            addIssue();
        } else {
            updateIssue();
        }
    }
    
    /**
     * 이슈 목록 로드
     */
    private void loadIssues() {
        try {
            tableModel.setRowCount(0);
            for (Issue issue : issueService.getAllIssues()) {
                tableModel.addRow(new Object[]{
                        issue.getTitle(),
                        issue.getCategory(),
                        issue.getSeverity(),
                        issue.getStatus(),
                        issue.getReporter(),
                        issue.getAssignee()
                });
            }
            
            // 첫 번째 행 자동 선택
            if (tableModel.getRowCount() > 0) {
                SwingUtilities.invokeLater(() -> {
                    issueTable.setRowSelectionInterval(0, 0);
                    issueTable.scrollRectToVisible(issueTable.getCellRect(0, 0, true));
                });
            }
        } catch (Exception e) {
            System.err.println("이슈 목록 로드 오류: " + e.getMessage());
            e.printStackTrace();
            // 에러가 발생해도 UI는 표시되도록 함
        }
    }
    
    /**
     * 미해결 이슈만 로드
     */
    private void loadOpenIssues() {
        tableModel.setRowCount(0);
        for (Issue issue : issueService.getOpenIssues()) {
            tableModel.addRow(new Object[]{
                    issue.getTitle(),
                    issue.getCategory(),
                    issue.getSeverity(),
                    issue.getStatus(),
                    issue.getReporter(),
                    issue.getAssignee()
            });
        }
        
        // 첫 번째 행 자동 선택
        if (tableModel.getRowCount() > 0) {
            SwingUtilities.invokeLater(() -> {
                issueTable.setRowSelectionInterval(0, 0);
                issueTable.scrollRectToVisible(issueTable.getCellRect(0, 0, true));
            });
        }
    }
    
    /**
     * 이슈 선택 이벤트
     */
    private void onIssueSelected() {
        int selectedRow = issueTable.getSelectedRow();
        if (selectedRow >= 0) {
            String title = (String) tableModel.getValueAt(selectedRow, 0);
            
            for (Issue issue : issueService.getAllIssues()) {
                if (issue.getTitle().equals(title)) {
                    selectedIssue = issue;
                    
                    // 수정 모드로 전환
                    enterEditMode();
                    
                    titleField.setText(issue.getTitle());
                    descriptionArea.setText(issue.getDescription() != null ? issue.getDescription() : "");
                    categoryCombo.setSelectedItem(issue.getCategory());
                    severityCombo.setSelectedItem(issue.getSeverity());
                    statusCombo.setSelectedItem(issue.getStatus());
                    reporterField.setText(issue.getReporter() != null ? issue.getReporter() : "");
                    assigneeField.setText(issue.getAssignee() != null ? issue.getAssignee() : "");
                    resolutionArea.setText(issue.getResolution() != null ? issue.getResolution() : "");
                    notesArea.setText(issue.getNotes() != null ? issue.getNotes() : "");
                    break;
                }
            }
        }
    }
    
    /**
     * 이슈 추가
     */
    private void addIssue() {
        try {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                titleField.requestFocus();
                return;
            }
            
            // 현재 회사 확인
            com.softone.auto.model.Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
            if (currentCompany == null) {
                JOptionPane.showMessageDialog(this, 
                    "회사가 선택되지 않았습니다.\n\n" +
                    "먼저 파견회사 관리에서 회사를 선택하거나\n" +
                    "새 회사를 등록해주세요.",
                    "회사 미선택", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            issueService.createIssue(
                    titleField.getText().trim(),
                    descriptionArea.getText(),
                    (String) categoryCombo.getSelectedItem(),
                    (String) severityCombo.getSelectedItem(),
                    reporterField.getText().trim(),
                    assigneeField.getText().trim(),
                    notesArea.getText()
            );
            
            loadIssues();
            enterEditMode();  // 저장 후 수정 모드로 전환
            JOptionPane.showMessageDialog(this, "✅ 이슈가 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 이슈 저장 중 오류가 발생했습니다.\n\n" +
                "오류 내용: " + errorMessage + "\n\n" +
                "다음 사항을 확인해주세요:\n" +
                "1. 데이터 폴더에 쓰기 권한이 있는지\n" +
                "2. 디스크 공간이 충분한지\n" +
                "3. 파일이 다른 프로그램에서 사용 중인지", 
                "저장 오류", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ 오류: " + e.getMessage() + "\n\n" +
                "자세한 내용은 콘솔 로그를 확인하세요.", 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * 이슈 수정
     */
    private void updateIssue() {
        if (selectedIssue == null) {
            JOptionPane.showMessageDialog(this, "수정할 이슈를 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                titleField.requestFocus();
                return;
            }
            
            selectedIssue.setTitle(titleField.getText().trim());
            selectedIssue.setDescription(descriptionArea.getText());
            selectedIssue.setCategory((String) categoryCombo.getSelectedItem());
            selectedIssue.setSeverity((String) severityCombo.getSelectedItem());
            selectedIssue.setStatus((String) statusCombo.getSelectedItem());
            selectedIssue.setReporter(reporterField.getText().trim());
            selectedIssue.setAssignee(assigneeField.getText().trim());
            selectedIssue.setResolution(resolutionArea.getText());
            selectedIssue.setNotes(notesArea.getText());
            
            issueService.updateIssue(selectedIssue);
            
            loadIssues();
            clearForm();
            JOptionPane.showMessageDialog(this, "✅ 이슈가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 이슈 수정 중 오류가 발생했습니다.\n\n" +
                "오류 내용: " + errorMessage + "\n\n" +
                "다음 사항을 확인해주세요:\n" +
                "1. 데이터 폴더에 쓰기 권한이 있는지\n" +
                "2. 디스크 공간이 충분한지\n" +
                "3. 파일이 다른 프로그램에서 사용 중인지", 
                "저장 오류", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "❌ 오류: " + e.getMessage() + "\n\n" +
                "자세한 내용은 콘솔 로그를 확인하세요.", 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * 이슈 삭제
     */
    private void deleteIssue() {
        if (selectedIssue == null) {
            JOptionPane.showMessageDialog(this, "삭제할 이슈를 선택하세요.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            issueService.deleteIssue(selectedIssue.getId());
            JOptionPane.showMessageDialog(this, "이슈가 삭제되었습니다.");
            loadIssues();
            clearForm();
        }
    }
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        categoryCombo.setSelectedIndex(0);
        severityCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        reporterField.setText("");
        assigneeField.setText("");
        resolutionArea.setText("");
        notesArea.setText("");
        selectedIssue = null;
        issueTable.clearSelection();
        
        // 초기화 시 수정 모드로 (기본값)
        if (!isNewMode) {
            enterEditMode();
        }
    }
}

