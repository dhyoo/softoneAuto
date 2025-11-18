package com.softone.auto.ui;

import com.softone.auto.model.CustomerCommunication;
import com.softone.auto.service.CustomerCommunicationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 고객 소통 관리 패널
 */
public class CustomerCommunicationPanel extends JPanel {
    
    private final CustomerCommunicationService communicationService;
    
    private JTable communicationTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> typeCombo;
    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField customerNameField;
    private JTextField ourRepField;
    private JTextField commDateField;
    private JComboBox<String> statusCombo;
    private JComboBox<String> priorityCombo;
    private JTextField dueDateField;
    private JTextArea responseArea;
    private JTextArea notesArea;
    
    private CustomerCommunication selectedCommunication;
    private JButton saveUpdateButton;  // 동적으로 변경되는 버튼
    private boolean isNewMode = false;  // 신규 모드 플래그
    
    public CustomerCommunicationPanel() {
        this.communicationService = new CustomerCommunicationService();
        try {
            initializeUI();
            loadCommunications();
        } catch (Exception e) {
            System.err.println("CustomerCommunicationPanel 초기화 오류: " + e.getMessage());
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
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        
        // 왼쪽: 테이블
        splitPane.setLeftComponent(createTablePanel());
        
        // 오른쪽: 입력 폼
        splitPane.setRightComponent(createFormPanel());
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    /**
     * 헤더 섹션 생성
     */
    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_PRIMARY);
        
        JLabel titleLabel = ModernDesign.createTitleLabel("💬 고객 소통 관리");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton refreshButton = ModernDesign.createSecondaryButton("🔄 새로고침");
        refreshButton.addActionListener(e -> loadCommunications());
        buttonPanel.add(refreshButton);
        
        JButton pendingButton = ModernDesign.createSecondaryButton("⏳ 대기중만");
        pendingButton.addActionListener(e -> loadPendingCommunications());
        buttonPanel.add(pendingButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 테이블 패널 생성
     */
    private JPanel createTablePanel() {
        JPanel panel = ModernDesign.createSection("소통 목록");
        
        // 테이블
        String[] columnNames = {"유형", "제목", "고객", "우리측", "일시", "상태", "우선순위"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        communicationTable = new JTable(tableModel);
        ModernDesign.styleTable(communicationTable);
        
        // 테이블 자동 리사이즈 모드 설정 (수평 스크롤 활성화)
        communicationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        communicationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        communicationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onCommunicationSelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(communicationTable);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
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
        JPanel panel = ModernDesign.createSection("소통 정보");
        
        // 폼 패널 - 유연한 레이아웃
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);
        
        int row = 0;
        
        // 유형
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; gbc.weighty = 0.0;
        formPanel.add(createCompactLabel("유형"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        typeCombo = createCompactCombo(new String[]{"MEETING", "REQUEST", "QA", "EMAIL", "PHONE"});
        formPanel.add(typeCombo, gbc);
        row++;
        
        // 제목
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("제목 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        titleField = createCompactTextField();
        formPanel.add(titleField, gbc);
        row++;
        
        // 고객 담당자
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("고객 담당자"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        customerNameField = createCompactTextField();
        formPanel.add(customerNameField, gbc);
        
        // 우리측 담당자
        gbc.gridx = 2; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("우리측 담당자"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        ourRepField = createCompactTextField();
        formPanel.add(ourRepField, gbc);
        row++;
        
        // 소통 일시
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("소통 일시"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        commDateField = createCompactTextField();
        commDateField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        commDateField.setToolTipText("형식: 2025-01-15 14:30");
        formPanel.add(commDateField, gbc);
        
        // 처리 기한
        gbc.gridx = 2; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("처리 기한"), gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        dueDateField = createCompactTextField();
        dueDateField.setToolTipText("형식: 2025-01-20 18:00");
        gbc.gridx = 1; gbc.weightx = 0.5;
        formPanel.add(dueDateField, gbc);
        row++;
        
        // 상태
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("상태"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        statusCombo = createCompactCombo(new String[]{"PENDING", "IN_PROGRESS", "COMPLETED"});
        formPanel.add(statusCombo, gbc);
        
        // 우선순위
        gbc.gridx = 2; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("우선순위"), gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        priorityCombo = createCompactCombo(new String[]{"HIGH", "MEDIUM", "LOW"});
        gbc.gridx = 1; gbc.weightx = 0.5;
        formPanel.add(priorityCombo, gbc);
        row++;
        
        // 내용
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(createCompactLabel("내용"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.BOTH;
        contentArea = new JTextArea(3, 20);
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(contentArea, gbc);
        row++;
        gbc.gridwidth = 1;
        
        // 답변/조치
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(createCompactLabel("답변/조치"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 1.0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.BOTH;
        responseArea = new JTextArea(2, 20);
        responseArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(responseArea, gbc);
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
        
        // 폼 패널을 스크롤 패널로 감싸기 (버튼은 제외)
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(formScrollPane, BorderLayout.CENTER);
        
        // 버튼 패널 (스크롤 밖에 고정)
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
        deleteButton.addActionListener(e -> deleteCommunication());
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
        communicationTable.clearSelection();
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
            addCommunication();
        } else {
            updateCommunication();
        }
    }
    
    /**
     * 소통 목록 로드
     */
    private void loadCommunications() {
        try {
            tableModel.setRowCount(0);
            for (CustomerCommunication comm : communicationService.getAllCommunications()) {
                tableModel.addRow(new Object[]{
                        comm.getType(),
                        comm.getTitle(),
                        comm.getCustomerName(),
                        comm.getOurRepresentative(),
                        comm.getCommunicationDate() != null ? comm.getCommunicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                        comm.getStatus(),
                        comm.getPriority()
                });
            }
            
            // 첫 번째 행 자동 선택
            if (tableModel.getRowCount() > 0) {
                SwingUtilities.invokeLater(() -> {
                    communicationTable.setRowSelectionInterval(0, 0);
                    communicationTable.scrollRectToVisible(communicationTable.getCellRect(0, 0, true));
                });
            }
        } catch (Exception e) {
            System.err.println("고객 소통 목록 로드 오류: " + e.getMessage());
            e.printStackTrace();
            // 에러가 발생해도 UI는 표시되도록 함
        }
    }
    
    /**
     * 대기중인 소통만 로드
     */
    private void loadPendingCommunications() {
        tableModel.setRowCount(0);
        for (CustomerCommunication comm : communicationService.getPendingCommunications()) {
            tableModel.addRow(new Object[]{
                    comm.getType(),
                    comm.getTitle(),
                    comm.getCustomerName(),
                    comm.getOurRepresentative(),
                    comm.getCommunicationDate() != null ? comm.getCommunicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    comm.getStatus(),
                    comm.getPriority()
            });
        }
        
        // 첫 번째 행 자동 선택
        if (tableModel.getRowCount() > 0) {
            SwingUtilities.invokeLater(() -> {
                communicationTable.setRowSelectionInterval(0, 0);
                communicationTable.scrollRectToVisible(communicationTable.getCellRect(0, 0, true));
            });
        }
    }
    
    /**
     * 소통 선택 이벤트
     */
    private void onCommunicationSelected() {
        int selectedRow = communicationTable.getSelectedRow();
        if (selectedRow >= 0) {
            String title = (String) tableModel.getValueAt(selectedRow, 1);
            
            for (CustomerCommunication comm : communicationService.getAllCommunications()) {
                if (comm.getTitle().equals(title)) {
                    selectedCommunication = comm;
                    
                    // 수정 모드로 전환
                    enterEditMode();
                    
                    typeCombo.setSelectedItem(comm.getType());
                    titleField.setText(comm.getTitle() != null ? comm.getTitle() : "");
                    contentArea.setText(comm.getContent() != null ? comm.getContent() : "");
                    customerNameField.setText(comm.getCustomerName() != null ? comm.getCustomerName() : "");
                    ourRepField.setText(comm.getOurRepresentative() != null ? comm.getOurRepresentative() : "");
                    if (comm.getCommunicationDate() != null) {
                        commDateField.setText(comm.getCommunicationDate().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    }
                    statusCombo.setSelectedItem(comm.getStatus());
                    priorityCombo.setSelectedItem(comm.getPriority());
                    dueDateField.setText(comm.getDueDate() != null ? 
                            comm.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
                    responseArea.setText(comm.getResponse() != null ? comm.getResponse() : "");
                    notesArea.setText(comm.getNotes() != null ? comm.getNotes() : "");
                    break;
                }
            }
        }
    }
    
    /**
     * 소통 추가
     */
    private void addCommunication() {
        try {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                titleField.requestFocus();
                return;
            }
            
            LocalDateTime commDate;
            try {
                commDate = LocalDateTime.parse(commDateField.getText().trim(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "소통 일시 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd HH:mm (예: 2025-01-15 14:30)", 
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
                commDateField.requestFocus();
                return;
            }
            
            LocalDateTime dueDate = null;
            if (!dueDateField.getText().trim().isEmpty()) {
                try {
                    dueDate = LocalDateTime.parse(dueDateField.getText().trim(), 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "처리 기한 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd HH:mm (예: 2025-01-20 18:00)", 
                            "입력 오류", JOptionPane.WARNING_MESSAGE);
                    dueDateField.requestFocus();
                    return;
                }
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
            
            communicationService.createCommunication(
                    (String) typeCombo.getSelectedItem(),
                    titleField.getText().trim(),
                    contentArea.getText(),
                    customerNameField.getText().trim(),
                    ourRepField.getText().trim(),
                    commDate,
                    (String) priorityCombo.getSelectedItem(),
                    dueDate,
                    notesArea.getText()
            );
            
            loadCommunications();
            enterEditMode();  // 저장 후 수정 모드로 전환
            JOptionPane.showMessageDialog(this, "✅ 소통 기록이 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (java.time.format.DateTimeParseException e) {
            // 이미 위에서 처리되지만 혹시 모를 경우를 대비
            JOptionPane.showMessageDialog(this, 
                "날짜/시간 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd HH:mm (예: 2025-01-15 14:30)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 소통 기록 저장 중 오류가 발생했습니다.\n\n" +
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
     * 소통 수정
     */
    private void updateCommunication() {
        if (selectedCommunication == null) {
            JOptionPane.showMessageDialog(this, "수정할 소통 기록을 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                titleField.requestFocus();
                return;
            }
            
            LocalDateTime commDate;
            try {
                commDate = LocalDateTime.parse(commDateField.getText().trim(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "소통 일시 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd HH:mm (예: 2025-01-15 14:30)", 
                        "입력 오류", JOptionPane.WARNING_MESSAGE);
                commDateField.requestFocus();
                return;
            }
            
            LocalDateTime dueDate = null;
            if (!dueDateField.getText().trim().isEmpty()) {
                try {
                    dueDate = LocalDateTime.parse(dueDateField.getText().trim(), 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "처리 기한 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd HH:mm (예: 2025-01-20 18:00)", 
                            "입력 오류", JOptionPane.WARNING_MESSAGE);
                    dueDateField.requestFocus();
                    return;
                }
            }
            
            selectedCommunication.setType((String) typeCombo.getSelectedItem());
            selectedCommunication.setTitle(titleField.getText().trim());
            selectedCommunication.setContent(contentArea.getText());
            selectedCommunication.setCustomerName(customerNameField.getText().trim());
            selectedCommunication.setOurRepresentative(ourRepField.getText().trim());
            selectedCommunication.setCommunicationDate(commDate);
            selectedCommunication.setDueDate(dueDate);
            selectedCommunication.setStatus((String) statusCombo.getSelectedItem());
            selectedCommunication.setPriority((String) priorityCombo.getSelectedItem());
            selectedCommunication.setResponse(responseArea.getText());
            selectedCommunication.setNotes(notesArea.getText());
            
            communicationService.updateCommunication(selectedCommunication);
            
            loadCommunications();
            clearForm();
            JOptionPane.showMessageDialog(this, "✅ 소통 기록이 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            // 이미 위에서 처리되지만 혹시 모를 경우를 대비
            JOptionPane.showMessageDialog(this, 
                "날짜/시간 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd HH:mm (예: 2025-01-15 14:30)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 소통 기록 수정 중 오류가 발생했습니다.\n\n" +
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
     * 소통 삭제
     */
    private void deleteCommunication() {
        if (selectedCommunication == null) {
            JOptionPane.showMessageDialog(this, "삭제할 소통 기록을 선택하세요.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            communicationService.deleteCommunication(selectedCommunication.getId());
            JOptionPane.showMessageDialog(this, "소통 기록이 삭제되었습니다.");
            loadCommunications();
            clearForm();
        }
    }
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        typeCombo.setSelectedIndex(0);
        titleField.setText("");
        contentArea.setText("");
        customerNameField.setText("");
        ourRepField.setText("");
        commDateField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        statusCombo.setSelectedIndex(0);
        priorityCombo.setSelectedIndex(0);
        dueDateField.setText("");
        responseArea.setText("");
        notesArea.setText("");
        selectedCommunication = null;
        communicationTable.clearSelection();
        
        // 초기화 시 수정 모드로 (기본값)
        if (!isNewMode) {
            enterEditMode();
        }
    }
}

