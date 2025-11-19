package com.softone.auto.ui;

import com.softone.auto.model.Company;
import com.softone.auto.service.CompanyService;
import com.softone.auto.util.InputValidator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 파견회사 관리 패널
 */
public class CompanyPanel extends JPanel {
    
    private final CompanyService companyService;
    private JTable companyTable;
    private DefaultTableModel tableModel;
    
    private JTextField nameField;
    private JTextField projectNameField;
    private JComboBox<String> contractTypeCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> statusCombo;
    private JTextArea notesArea;
    
    private JLabel startDateErrorLabel;
    private JLabel endDateErrorLabel;
    
    private Company selectedCompany;
    private JButton saveUpdateButton;  // 동적으로 변경되는 버튼
    private boolean isNewMode = false;  // 신규 모드 플래그
    
    public CompanyPanel() {
        this.companyService = new CompanyService();
        try {
            initializeUI();
            loadCompanies();
        } catch (Exception e) {
            System.err.println("CompanyPanel 초기화 오류: " + e.getMessage());
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
        splitPane.setContinuousLayout(true); // 실시간 리사이징
        splitPane.setOneTouchExpandable(true); // 원터치 확장 버튼
        
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
        
        JLabel titleLabel = ModernDesign.createTitleLabel("🏢 파견회사 관리");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton refreshButton = ModernDesign.createSecondaryButton("🔄 새로고침");
        refreshButton.addActionListener(e -> loadCompanies());
        buttonPanel.add(refreshButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 회사 목록 변경 알림 (MainFrame에 전달)
     */
    private void notifyCompanyListChanged() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof MainFrame)) {
            parent = parent.getParent();
        }
        if (parent instanceof MainFrame) {
            ((MainFrame) parent).refreshCompanyList();
        }
    }
    
    /**
     * 테이블 패널 생성
     */
    private JPanel createTablePanel() {
        JPanel panel = ModernDesign.createSection("파견회사 목록");
        
        // 테이블
        String[] columnNames = {"회사명", "프로젝트명", "계약형태", "시작일", "종료일", "상태"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        companyTable = new JTable(tableModel);
        ModernDesign.styleTable(companyTable);
        
        // 멀티 선택 모드로 변경
        companyTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        companyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onCompanySelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(companyTable);
        scrollPane.setBorder(null);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 입력 폼 패널 생성 (한눈에 보이게 컴팩트하게)
     */
    private JPanel createFormPanel() {
        JPanel panel = ModernDesign.createSection("회사 정보");
        
        // 폼 패널 - 유연한 레이아웃
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);
        
        int row = 0;
        
        // 회사명
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; gbc.weighty = 0.0;
        formPanel.add(createCompactLabel("회사명 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = createCompactTextField();
        formPanel.add(nameField, gbc);
        row++;
        
        // 프로젝트명
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("프로젝트명 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        projectNameField = createCompactTextField();
        formPanel.add(projectNameField, gbc);
        row++;
        
        // 계약 형태
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("계약 형태"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        contractTypeCombo = createCompactCombo(new String[]{"파견", "용역", "SI", "SM"});
        formPanel.add(contractTypeCombo, gbc);
        row++;
        
        // 시작일
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("시작일 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        startDateField = createCompactTextField();
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        startDateField.setToolTipText("형식: 2025-01-15");
        formPanel.add(startDateField, gbc);
        row++;
        gbc.gridx = 1; gbc.weightx = 1.0;
        startDateErrorLabel = new JLabel(" ");
        startDateErrorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        startDateErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(startDateErrorLabel, gbc);
        row++;
        
        // 종료일
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("종료일 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        endDateField = createCompactTextField();
        endDateField.setText(LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setToolTipText("형식: 2026-01-15");
        formPanel.add(endDateField, gbc);
        row++;
        gbc.gridx = 1; gbc.weightx = 1.0;
        endDateErrorLabel = new JLabel(" ");
        endDateErrorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        endDateErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(endDateErrorLabel, gbc);
        row++;
        
        // 날짜 검증
        DateValidator.addDateValidation(startDateField, startDateErrorLabel, "시작일");
        DateValidator.addDateValidation(endDateField, endDateErrorLabel, "종료일");
        
        // 상태
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("상태"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        statusCombo = createCompactCombo(new String[]{"ACTIVE", "INACTIVE", "COMPLETED"});
        formPanel.add(statusCombo, gbc);
        row++;
        
        // 비고
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE; // anchor 사용 시 fill은 NONE
        formPanel.add(createCompactLabel("비고"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(3, 20);
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
        // 최대 크기는 제한하지 않아 창 크기에 따라 확장됨
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
        // 최대 크기는 제한하지 않아 창 크기에 따라 확장됨
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
        
        // 삭제 버튼 (왼쪽에 배치)
        JButton deleteButton = createCompactButton("삭제");
        deleteButton.setBackground(new Color(220, 53, 69)); // 빨간색
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                deleteButton.setBackground(new Color(200, 35, 51));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                deleteButton.setBackground(new Color(220, 53, 69));
            }
        });
        deleteButton.addActionListener(e -> deleteCompanies());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = createCompactButton("초기화");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        return buttonPanel;
    }
    
    /**
     * 신규 모드 진입
     */
    private void enterNewMode() {
        isNewMode = true;
        clearForm();
        saveUpdateButton.setText("저장");
        companyTable.clearSelection();
        nameField.requestFocus();
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
            addCompany();
        } else {
            updateCompany();
        }
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
     * 레이블 생성
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ModernDesign.FONT_BODY);
        label.setForeground(ModernDesign.TEXT_SECONDARY);
        return label;
    }
    
    /**
     * 회사 목록 로드
     */
    private void loadCompanies() {
        try {
            tableModel.setRowCount(0);
            for (Company company : companyService.getAllCompanies()) {
                Object[] row = new Object[]{
                    company.getName(),
                    company.getProjectName(),
                    company.getContractType(),
                    company.getStartDate() != null ? company.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                    company.getEndDate() != null ? company.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                    getStatusBadge(company.getStatus())
                };
                tableModel.addRow(row);
            }
            
            // 첫 번째 행 자동 선택
            if (tableModel.getRowCount() > 0) {
                SwingUtilities.invokeLater(() -> {
                    companyTable.setRowSelectionInterval(0, 0);
                    companyTable.scrollRectToVisible(companyTable.getCellRect(0, 0, true));
                });
            }
        } catch (Exception e) {
            System.err.println("회사 목록 로드 오류: " + e.getMessage());
            e.printStackTrace();
            // 에러가 발생해도 UI는 표시되도록 함
        }
    }
    
    /**
     * 상태 배지 생성
     */
    private String getStatusBadge(String status) {
        switch (status) {
            case "ACTIVE": return "✅ 활성";
            case "INACTIVE": return "⏸️ 비활성";
            case "COMPLETED": return "✔️ 완료";
            default: return status;
        }
    }
    
    /**
     * 회사 선택 이벤트
     */
    private void onCompanySelected() {
        int selectedRow = companyTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            selectedCompany = companyService.getCompanyByName(name);
            
            if (selectedCompany != null) {
                // 수정 모드로 전환
                enterEditMode();
                
                nameField.setText(selectedCompany.getName());
                projectNameField.setText(selectedCompany.getProjectName());
                contractTypeCombo.setSelectedItem(selectedCompany.getContractType());
                startDateField.setText(selectedCompany.getStartDate() != null ? 
                    selectedCompany.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
                endDateField.setText(selectedCompany.getEndDate() != null ? 
                    selectedCompany.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
                statusCombo.setSelectedItem(selectedCompany.getStatus());
                notesArea.setText(selectedCompany.getNotes());
            }
        }
    }
    
    /**
     * 회사 추가
     */
    private void addCompany() {
        try {
            // 유효성 검사
            String companyName = nameField.getText().trim();
            
            if (companyName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "회사명을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            
            // 중복 회사명 체크 (ID 기반)
            Company existingCompany = companyService.getCompanyByName(companyName);
            if (existingCompany != null) {
                JOptionPane.showMessageDialog(this, 
                    "이미 등록된 회사입니다.\n\n" +
                    "회사명: " + companyName + "\n" +
                    "프로젝트: " + existingCompany.getProjectName() + "\n\n" +
                    "다른 회사명을 입력하거나 기존 회사를 수정해주세요.",
                    "중복 등록 오류", 
                    JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                nameField.selectAll();
                return;
            }
            
            if (projectNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "프로젝트명을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                projectNameField.requestFocus();
                return;
            }
            
            // 날짜 검증
            if (!DateValidator.validateDateBeforeSave(startDateField.getText().trim(), "계약 시작일", this)) {
                startDateField.requestFocus();
                return;
            }
            
            if (!DateValidator.validateDateBeforeSave(endDateField.getText().trim(), "계약 종료일", this)) {
                endDateField.requestFocus();
                return;
            }
            
            LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
            LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
            
            // 날짜 논리 검증
            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this, "종료일은 시작일보다 이후여야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                endDateField.requestFocus();
                return;
            }
            
            companyService.createCompany(
                companyName,
                projectNameField.getText().trim(),
                (String) contractTypeCombo.getSelectedItem(),
                startDate,
                endDate,
                notesArea.getText()
            );
            
            loadCompanies();
            notifyCompanyListChanged();
            enterEditMode();  // 저장 후 수정 모드로 전환
            JOptionPane.showMessageDialog(this, "✅ 파견회사가 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 회사 수정
     */
    private void updateCompany() {
        if (selectedCompany == null) {
            JOptionPane.showMessageDialog(this, "수정할 회사를 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // 유효성 검사
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "회사명을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            
            if (projectNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "프로젝트명을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                projectNameField.requestFocus();
                return;
            }
            
            // 날짜 검증
            if (!DateValidator.validateDateBeforeSave(startDateField.getText().trim(), "계약 시작일", this)) {
                startDateField.requestFocus();
                return;
            }
            
            if (!DateValidator.validateDateBeforeSave(endDateField.getText().trim(), "계약 종료일", this)) {
                endDateField.requestFocus();
                return;
            }
            
            LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
            LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
            
            // 날짜 논리 검증
            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this, "종료일은 시작일보다 이후여야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                endDateField.requestFocus();
                return;
            }
            
            // 기존 ID 보존 (중요: companyId가 변경되면 Foreign Key 제약조건으로 관련 데이터가 삭제될 수 있음)
            String originalId = selectedCompany.getId();
            if (originalId == null || originalId.isEmpty()) {
                throw new IllegalStateException("회사 ID가 없습니다. 수정할 수 없습니다.");
            }
            
            System.out.println("  [CompanyPanel.updateCompany] 회사 수정 시작: " + selectedCompany.getName() + " (ID: " + originalId + ")");
            
            // ID를 별도 변수에 저장하여 절대 변경되지 않도록 보장
            String preservedId = originalId;
            
            selectedCompany.setName(nameField.getText().trim());
            selectedCompany.setProjectName(projectNameField.getText().trim());
            selectedCompany.setContractType((String) contractTypeCombo.getSelectedItem());
            selectedCompany.setStartDate(startDate);
            selectedCompany.setEndDate(endDate);
            selectedCompany.setStatus((String) statusCombo.getSelectedItem());
            selectedCompany.setNotes(notesArea.getText());
            
            // ID가 변경되지 않았는지 확인 및 강제 복원
            if (!preservedId.equals(selectedCompany.getId())) {
                System.err.println("  ⚠️ 경고: 회사 ID가 변경되었습니다! 원래 ID로 복원합니다.");
                System.err.println("    기존 ID: " + preservedId);
                System.err.println("    변경된 ID: " + selectedCompany.getId());
                selectedCompany.setId(preservedId);
            }
            
            // 추가 검증: ID가 여전히 올바른지 확인
            if (!preservedId.equals(selectedCompany.getId())) {
                throw new IllegalStateException("회사 ID는 변경할 수 없습니다. 원래 ID: " + preservedId);
            }
            
            Company updatedCompany = companyService.updateCompany(selectedCompany);
            System.out.println("  [CompanyPanel.updateCompany] 회사 수정 완료: " + updatedCompany.getName() + " (ID: " + updatedCompany.getId() + ")");
            
            // 최종 검증: 저장된 회사의 ID가 원래 ID와 일치하는지 확인
            if (!preservedId.equals(updatedCompany.getId())) {
                System.err.println("  ✗ 치명적 오류: 저장된 회사의 ID가 원래 ID와 다릅니다!");
                System.err.println("    원래 ID: " + preservedId);
                System.err.println("    저장된 ID: " + updatedCompany.getId());
                throw new IllegalStateException("회사 ID가 변경되었습니다. 데이터 무결성을 위해 수정을 취소합니다.");
            }
            
            // 현재 선택된 회사가 수정된 회사인 경우 AppContext도 업데이트 (ID는 동일)
            com.softone.auto.model.Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
            if (currentCompany != null && currentCompany.getId().equals(preservedId)) {
                System.out.println("  → 현재 선택된 회사가 수정되었으므로 AppContext 업데이트 (ID 유지: " + preservedId + ")");
                com.softone.auto.util.AppContext.getInstance().setCurrentCompany(updatedCompany);
            }
            
            // 현재 회사가 수정된 회사인지 확인
            boolean isCurrentCompany = currentCompany != null && currentCompany.getId().equals(preservedId);
            
            // 목록 새로고침 (수정된 회사 정보 반영)
            loadCompanies();
            
            // 수정된 회사를 다시 선택 (테이블의 첫 번째 컬럼은 회사명)
            boolean found = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String companyName = (String) tableModel.getValueAt(i, 0);
                if (companyName != null && companyName.equals(updatedCompany.getName())) {
                    companyTable.setRowSelectionInterval(i, i);
                    selectedCompany = updatedCompany;
                    found = true;
                    System.out.println("  → 수정된 회사를 테이블에서 찾아 선택: " + updatedCompany.getName());
                    break;
                }
            }
            
            if (!found) {
                System.err.println("  ⚠️ 수정된 회사를 테이블에서 찾을 수 없음: " + updatedCompany.getName());
            }
            
            // 현재 회사가 수정된 회사인 경우, AppContext를 업데이트하고 notifyCompanyListChanged 호출
            // 이렇게 하면 MainFrame.refreshCompanyList()에서 현재 회사를 올바르게 유지할 수 있음
            if (isCurrentCompany) {
                System.out.println("  → 현재 회사가 수정되었으므로 AppContext를 먼저 업데이트");
                com.softone.auto.util.AppContext.getInstance().setCurrentCompany(updatedCompany);
            }
            
            // 회사 목록 변경 알림 (현재 회사는 이미 AppContext에 설정되어 있음)
            notifyCompanyListChanged();
            
            JOptionPane.showMessageDialog(this, "✅ 회사 정보가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        nameField.setText("");
        projectNameField.setText("");
        contractTypeCombo.setSelectedIndex(0);
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setText(LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        statusCombo.setSelectedIndex(0);
        notesArea.setText("");
        selectedCompany = null;
        companyTable.clearSelection();
        
        // 초기화 시 수정 모드로 (기본값)
        if (!isNewMode) {
            enterEditMode();
        }
    }
    
    /**
     * 회사 삭제 (단일 또는 멀티)
     */
    private void deleteCompanies() {
        int[] selectedRows = companyTable.getSelectedRows();
        
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "삭제할 회사를 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 선택 건수만 표시 (회사 이름은 노출하지 않음)
        String message = selectedRows.length == 1 
            ? "정말 삭제하시겠습니까?\n\n선택된 회사: 1개\n\n이 회사와 관련된 모든 데이터가 삭제됩니다!"
            : "정말 삭제하시겠습니까?\n\n선택된 회사: " + selectedRows.length + "개\n\n이 회사들과 관련된 모든 데이터가 삭제됩니다!";
        
        int result = JOptionPane.showConfirmDialog(this, 
                message, 
                "확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            int successCount = 0;
            int failCount = 0;
            
            for (int row : selectedRows) {
                try {
                    String companyName = (String) tableModel.getValueAt(row, 0);
                    Company company = companyService.getCompanyByName(companyName);
                    if (company != null) {
                        companyService.deleteCompany(company.getId());
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    System.err.println("회사 삭제 오류: " + e.getMessage());
                    failCount++;
                }
            }
            
            notifyCompanyListChanged();
            
            if (failCount == 0) {
                JOptionPane.showMessageDialog(this, 
                    "✅ " + successCount + "개의 회사가 삭제되었습니다.", 
                    "완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "⚠️ " + successCount + "개 삭제 완료, " + failCount + "개 삭제 실패", 
                    "알림", JOptionPane.WARNING_MESSAGE);
            }
            
            loadCompanies();
            selectedCompany = null;
            companyTable.clearSelection();
        }
    }
}
