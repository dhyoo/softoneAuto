package com.softone.auto.ui;

import com.softone.auto.model.Developer;
import com.softone.auto.service.DeveloperService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 개발자 관리 패널 - 모던 디자인
 */
public class DeveloperPanel extends JPanel {
    
    private final DeveloperService developerService;
    private JTable developerTable;
    private DefaultTableModel tableModel;
    
    private JTextField nameField;
    private JTextField positionField;
    private JTextField roleField;
    private JTextField teamField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField emergencyPhoneField;
    private JTextField joinDateField;
    private JComboBox<String> statusCombo;
    private JTextArea notesArea;
    
    private Developer selectedDeveloper;
    private JButton saveUpdateButton;  // 동적 버튼
    private boolean isNewMode = false;
    
    public DeveloperPanel() {
        this.developerService = new DeveloperService();
        try {
            initializeUI();
            loadDevelopers();
        } catch (Exception e) {
            System.err.println("DeveloperPanel 초기화 오류: " + e.getMessage());
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
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        
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
        
        JLabel titleLabel = ModernDesign.createTitleLabel("👥 개발자 관리");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton refreshButton = createUnifiedButton("새로고침");
        refreshButton.addActionListener(e -> loadDevelopers());
        buttonPanel.add(refreshButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 테이블 패널 생성
     */
    private JPanel createTablePanel() {
        JPanel panel = ModernDesign.createSection("개발자 목록");
        
        // 테이블
        String[] columnNames = {"이름", "직급", "역할", "팀", "상태", "투입일"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        developerTable = new JTable(tableModel);
        ModernDesign.styleTable(developerTable);
        
        // 테이블 자동 리사이즈 모드 설정 (수평 스크롤 활성화)
        developerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        developerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        developerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onDeveloperSelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(developerTable);
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
     * 입력 폼 패널 생성
     */
    private JPanel createFormPanel() {
        JPanel panel = ModernDesign.createSection("개발자 정보");
        
        // 폼 패널 - 컴팩트하게
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        int row = 0;
        
        // 이름
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("이름 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        nameField = createCompactTextField();
        formPanel.add(nameField, gbc);
        row++;
        
        // 직급
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("직급"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        positionField = createCompactTextField();
        formPanel.add(positionField, gbc);
        row++;
        
        // 역할
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("역할"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        roleField = createCompactTextField();
        formPanel.add(roleField, gbc);
        row++;
        
        // 팀
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("팀"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        teamField = createCompactTextField();
        formPanel.add(teamField, gbc);
        row++;
        
        // 이메일
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("이메일"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        emailField = createCompactTextField();
        formPanel.add(emailField, gbc);
        row++;
        
        // 연락처
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("연락처"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        phoneField = createCompactTextField();
        formPanel.add(phoneField, gbc);
        row++;
        
        // 비상연락처
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("비상연락처"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        emergencyPhoneField = createCompactTextField();
        formPanel.add(emergencyPhoneField, gbc);
        row++;
        
        // 투입일
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("투입일 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        joinDateField = createCompactTextField();
        joinDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        joinDateField.setToolTipText("형식: 2025-01-15");
        formPanel.add(joinDateField, gbc);
        row++;
        gbc.gridx = 1;
        JLabel joinDateErrorLabel = new JLabel(" ");
        joinDateErrorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        joinDateErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(joinDateErrorLabel, gbc);
        row++;
        
        // 날짜 검증
        DateValidator.addDateValidation(joinDateField, joinDateErrorLabel, "투입일");
        
        // 상태
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("상태"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        statusCombo = createCompactCombo(new String[]{"ACTIVE", "INACTIVE", "VACATION"});
        formPanel.add(statusCombo, gbc);
        row++;
        
        // 비고
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.3;
        formPanel.add(createCompactLabel("비고"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0.7;
        notesArea = new JTextArea(2, 20);
        notesArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
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
        label.setPreferredSize(new Dimension(110, 25));
        return label;
    }
    
    /**
     * 컴팩트 텍스트 필드 생성
     */
    private JTextField createCompactTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        field.setPreferredSize(new Dimension(250, 32));
        return field;
    }
    
    /**
     * 컴팩트 콤보박스 생성
     */
    private JComboBox<String> createCompactCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        combo.setBackground(ModernDesign.BG_SECONDARY);
        combo.setPreferredSize(new Dimension(250, 32));
        return combo;
    }
    
    /**
     * 버튼 패널 생성 (파견회사와 동일)
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JButton newButton = createUnifiedButton("신규");
        newButton.addActionListener(e -> enterNewMode());
        buttonPanel.add(newButton);
        
        saveUpdateButton = createUnifiedButton("저장");
        saveUpdateButton.addActionListener(e -> saveOrUpdate());
        buttonPanel.add(saveUpdateButton);
        
        JButton deleteButton = createUnifiedButton("삭제");
        deleteButton.addActionListener(e -> deleteDeveloper());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = createUnifiedButton("초기화");
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
        developerTable.clearSelection();
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
            addDeveloper();
        } else {
            updateDeveloper();
        }
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
     * 개발자 목록 로드
     */
    private void loadDevelopers() {
        try {
            tableModel.setRowCount(0);
            for (Developer dev : developerService.getAllDevelopers()) {
                Object[] row = new Object[]{
                    dev.getName(),
                    dev.getPosition(),
                    dev.getRole(),
                    dev.getTeam(),
                    getStatusBadge(dev.getStatus()),
                    dev.getJoinDate() != null ? dev.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : ""
                };
                tableModel.addRow(row);
            }
            
            // 첫 번째 행 자동 선택
            if (tableModel.getRowCount() > 0) {
                SwingUtilities.invokeLater(() -> {
                    developerTable.setRowSelectionInterval(0, 0);
                    developerTable.scrollRectToVisible(developerTable.getCellRect(0, 0, true));
                });
            }
        } catch (Exception e) {
            System.err.println("개발자 목록 로드 오류: " + e.getMessage());
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
            case "VACATION": return "🌴 휴가";
            default: return status;
        }
    }
    
    /**
     * 개발자 선택 이벤트
     */
    private void onDeveloperSelected() {
        int selectedRow = developerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            selectedDeveloper = developerService.getDeveloperByName(name);
            
            if (selectedDeveloper != null) {
                // 수정 모드로 전환
                enterEditMode();
                
                nameField.setText(selectedDeveloper.getName());
                positionField.setText(selectedDeveloper.getPosition());
                roleField.setText(selectedDeveloper.getRole());
                teamField.setText(selectedDeveloper.getTeam());
                emailField.setText(selectedDeveloper.getEmail());
                phoneField.setText(selectedDeveloper.getPhone());
                emergencyPhoneField.setText(selectedDeveloper.getEmergencyPhone() != null ? selectedDeveloper.getEmergencyPhone() : "");
                joinDateField.setText(selectedDeveloper.getJoinDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                statusCombo.setSelectedItem(selectedDeveloper.getStatus());
                notesArea.setText(selectedDeveloper.getNotes());
            }
        }
    }
    
    /**
     * 개발자 추가
     */
    private void addDeveloper() {
        try {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            
            // 투입일 검증
            if (!DateValidator.validateDateBeforeSave(joinDateField.getText().trim(), "투입일", this)) {
                joinDateField.requestFocus();
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
            
            // 현재 회사 내에서 중복 이름 체크
            String developerName = nameField.getText().trim();
            Developer existingDeveloper = developerService.getDeveloperByName(developerName);
            if (existingDeveloper != null) {
                JOptionPane.showMessageDialog(this, 
                    "이미 등록된 개발자입니다.\n\n" +
                    "이름: " + developerName + "\n" +
                    "다른 이름을 입력하거나 기존 개발자를 수정해주세요.",
                    "중복 등록 오류", 
                    JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                nameField.selectAll();
                return;
            }
            
            developerService.createDeveloper(
                    nameField.getText().trim(),
                    positionField.getText().trim(),
                    roleField.getText().trim(),
                    teamField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    emergencyPhoneField.getText().trim(),
                    LocalDate.parse(joinDateField.getText().trim()),
                    notesArea.getText()
            );
            
            loadDevelopers();
            JOptionPane.showMessageDialog(this, "✅ 개발자가 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            // 신규 모드 유지하면서 폼 초기화
            clearForm();
            enterNewMode();  // 저장 후에도 신규 모드 유지
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                "날짜 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd (예: 2025-01-15)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            joinDateField.requestFocus();
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 개발자 저장 중 오류가 발생했습니다.\n\n" +
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
     * 개발자 수정
     */
    private void updateDeveloper() {
        if (selectedDeveloper == null) {
            JOptionPane.showMessageDialog(this, "수정할 개발자를 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름을 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            
            // 투입일 검증
            if (!DateValidator.validateDateBeforeSave(joinDateField.getText().trim(), "투입일", this)) {
                joinDateField.requestFocus();
                return;
            }
            
            selectedDeveloper.setName(nameField.getText().trim());
            selectedDeveloper.setPosition(positionField.getText().trim());
            selectedDeveloper.setRole(roleField.getText().trim());
            selectedDeveloper.setTeam(teamField.getText().trim());
            selectedDeveloper.setEmail(emailField.getText().trim());
            selectedDeveloper.setPhone(phoneField.getText().trim());
            selectedDeveloper.setEmergencyPhone(emergencyPhoneField.getText().trim());
            selectedDeveloper.setJoinDate(LocalDate.parse(joinDateField.getText().trim()));
            selectedDeveloper.setStatus((String) statusCombo.getSelectedItem());
            selectedDeveloper.setNotes(notesArea.getText());
            
            developerService.updateDeveloper(selectedDeveloper);
            
            loadDevelopers();
            clearForm();
            JOptionPane.showMessageDialog(this, "✅ 개발자 정보가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                "날짜 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd (예: 2025-01-15)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            joinDateField.requestFocus();
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 개발자 수정 중 오류가 발생했습니다.\n\n" +
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
     * 개발자 삭제
     */
    private void deleteDeveloper() {
        if (selectedDeveloper == null) {
            JOptionPane.showMessageDialog(this, "삭제할 개발자를 선택하세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "정말 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.", 
                "확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            developerService.deleteDeveloper(selectedDeveloper.getId());
            JOptionPane.showMessageDialog(this, "✅ 개발자가 삭제되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            loadDevelopers();
            clearForm();
        }
    }
    
    /**
     * 통일된 버튼 생성
     */
    private JButton createUnifiedButton(String text) {
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
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        nameField.setText("");
        positionField.setText("");
        roleField.setText("");
        teamField.setText("");
        emailField.setText("");
        phoneField.setText("");
        emergencyPhoneField.setText("");
        joinDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        statusCombo.setSelectedIndex(0);
        notesArea.setText("");
        selectedDeveloper = null;
        developerTable.clearSelection();
    }
}
