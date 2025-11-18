package com.softone.auto.ui;

import com.softone.auto.model.Company;
import com.softone.auto.service.CompanyService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 파견회사 추가/수정 다이얼로그
 */
public class CompanyFormDialog extends JDialog {
    
    private final CompanyService companyService;
    private Company company;
    private boolean saved = false;
    
    private JTextField nameField;
    private JTextField projectNameField;
    private JComboBox<String> contractTypeCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> statusCombo;
    private JTextArea notesArea;
    
    private JLabel startDateErrorLabel;
    private JLabel endDateErrorLabel;
    
    /**
     * 새 회사 추가용 생성자
     */
    public CompanyFormDialog(Frame owner) {
        this(owner, null);
    }
    
    /**
     * 회사 수정용 생성자
     */
    public CompanyFormDialog(Frame owner, Company company) {
        super(owner, company == null ? "파견회사 추가" : "파견회사 수정", true);
        this.companyService = new CompanyService();
        this.company = company;
        
        initializeUI();
        
        if (company != null) {
            loadCompanyData();
        }
        
        setSize(500, 600);
        setLocationRelativeTo(owner);
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(ModernDesign.BG_PRIMARY);
        
        // 메인 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ModernDesign.BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 폼 패널
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ModernDesign.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // 회사명
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("회사명 *"), gbc);
        gbc.gridy = row + 1;
        nameField = ModernDesign.createTextField();
        formPanel.add(nameField, gbc);
        row += 2;
        
        // 프로젝트명
        gbc.gridy = row;
        formPanel.add(createLabel("프로젝트명 *"), gbc);
        gbc.gridy = row + 1;
        projectNameField = ModernDesign.createTextField();
        formPanel.add(projectNameField, gbc);
        row += 2;
        
        // 계약 형태
        gbc.gridy = row;
        formPanel.add(createLabel("계약 형태"), gbc);
        gbc.gridy = row + 1;
        contractTypeCombo = ModernDesign.createComboBox(new String[]{"파견", "용역", "SI", "SM"});
        formPanel.add(contractTypeCombo, gbc);
        row += 2;
        
        // 시작일
        gbc.gridy = row;
        formPanel.add(createLabel("계약 시작일 (yyyy-MM-dd) *"), gbc);
        gbc.gridy = row + 1;
        startDateField = ModernDesign.createTextField();
        startDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        startDateField.setToolTipText("형식: 2025-01-15 (년-월-일)");
        formPanel.add(startDateField, gbc);
        gbc.gridy = row + 2;
        startDateErrorLabel = new JLabel(" ");
        startDateErrorLabel.setFont(ModernDesign.FONT_SMALL);
        startDateErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(startDateErrorLabel, gbc);
        row += 3;
        
        // 종료일
        gbc.gridy = row;
        formPanel.add(createLabel("계약 종료일 (yyyy-MM-dd) *"), gbc);
        gbc.gridy = row + 1;
        endDateField = ModernDesign.createTextField();
        endDateField.setText(LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setToolTipText("형식: 2026-01-15 (년-월-일)");
        formPanel.add(endDateField, gbc);
        gbc.gridy = row + 2;
        endDateErrorLabel = new JLabel(" ");
        endDateErrorLabel.setFont(ModernDesign.FONT_SMALL);
        endDateErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(endDateErrorLabel, gbc);
        row += 3;
        
        // 날짜 필드 실시간 검증 추가
        DateValidator.addDateValidation(startDateField, startDateErrorLabel, "시작일");
        DateValidator.addDateValidation(endDateField, endDateErrorLabel, "종료일");
        
        // 상태
        gbc.gridy = row;
        formPanel.add(createLabel("상태"), gbc);
        gbc.gridy = row + 1;
        statusCombo = ModernDesign.createComboBox(new String[]{"ACTIVE", "INACTIVE", "COMPLETED"});
        formPanel.add(statusCombo, gbc);
        row += 2;
        
        // 비고
        gbc.gridy = row;
        formPanel.add(createLabel("비고"), gbc);
        gbc.gridy = row + 1;
        notesArea = ModernDesign.createTextArea(4, 20);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(ModernDesign.BORDER));
        formPanel.add(notesScroll, gbc);
        
        mainPanel.add(formPanel);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton saveButton = ModernDesign.createPrimaryButton("저장");
        saveButton.addActionListener(e -> saveCompany());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = ModernDesign.createSecondaryButton("취소");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
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
     * 회사 데이터 로드 (수정 모드)
     */
    private void loadCompanyData() {
        nameField.setText(company.getName());
        projectNameField.setText(company.getProjectName());
        contractTypeCombo.setSelectedItem(company.getContractType());
        startDateField.setText(company.getStartDate() != null ? 
            company.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        endDateField.setText(company.getEndDate() != null ? 
            company.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        statusCombo.setSelectedItem(company.getStatus());
        notesArea.setText(company.getNotes());
    }
    
    /**
     * 회사 저장
     */
    private void saveCompany() {
        try {
            // 1. 필수 필드 검사
            if (nameField.getText().trim().isEmpty()) {
                showError("회사명을 입력하세요.", nameField);
                return;
            }
            
            if (projectNameField.getText().trim().isEmpty()) {
                showError("프로젝트명을 입력하세요.", projectNameField);
                return;
            }
            
            // 2. 시작일 검증
            String startDateText = startDateField.getText().trim();
            if (!DateValidator.validateDateBeforeSave(startDateText, "계약 시작일", this)) {
                startDateField.requestFocus();
                return;
            }
            
            // 3. 종료일 검증
            String endDateText = endDateField.getText().trim();
            if (!DateValidator.validateDateBeforeSave(endDateText, "계약 종료일", this)) {
                endDateField.requestFocus();
                return;
            }
            
            // 4. 날짜 파싱
            LocalDate startDate = LocalDate.parse(startDateText, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(endDateText, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // 5. 날짜 논리 검증
            if (endDate.isBefore(startDate)) {
                showError("종료일은 시작일보다 이후여야 합니다.", endDateField);
                return;
            }
            
            if (startDate.isAfter(LocalDate.now().plusYears(10))) {
                showError("시작일이 너무 미래입니다. 날짜를 확인하세요.", startDateField);
                return;
            }
            
            // 6. 데이터 저장
            if (company == null) {
                // 새 회사 추가
                companyService.createCompany(
                    nameField.getText().trim(),
                    projectNameField.getText().trim(),
                    (String) contractTypeCombo.getSelectedItem(),
                    startDate,
                    endDate,
                    notesArea.getText()
                );
            } else {
                // 기존 회사 수정
                company.setName(nameField.getText().trim());
                company.setProjectName(projectNameField.getText().trim());
                company.setContractType((String) contractTypeCombo.getSelectedItem());
                company.setStartDate(startDate);
                company.setEndDate(endDate);
                company.setStatus((String) statusCombo.getSelectedItem());
                company.setNotes(notesArea.getText());
                
                companyService.updateCompany(company);
            }
            
            saved = true;
            dispose();
            
        } catch (DateTimeParseException e) {
            showError("날짜 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd (예: 2025-01-15)\n\n상세: " + e.getMessage(), null);
        } catch (Exception e) {
            showError("저장 실패: " + e.getMessage(), null);
        }
    }
    
    /**
     * 에러 메시지 표시
     */
    private void showError(String message, JTextField focusField) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "입력 오류", 
            JOptionPane.WARNING_MESSAGE);
        
        if (focusField != null) {
            focusField.requestFocus();
            focusField.selectAll();
        }
    }
    
    /**
     * 저장 여부 확인
     */
    public boolean isSaved() {
        return saved;
    }
}

