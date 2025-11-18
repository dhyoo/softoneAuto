package com.softone.auto.ui;

import com.softone.auto.model.Attendance;
import com.softone.auto.model.Developer;
import com.softone.auto.service.AttendanceService;
import com.softone.auto.service.DeveloperService;
import com.softone.auto.util.ErrorMessageMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 근태 관리 패널
 */
public class AttendancePanel extends JPanel {
    
    private final AttendanceService attendanceService;
    private final DeveloperService developerService;
    
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JComboBox<String> typeFilterCombo;
    
    private JComboBox<String> developerCombo;
    private JTextField dateField;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JComboBox<String> typeCombo;
    private JTextArea notesArea;
    
    private Attendance selectedAttendance;
    private JButton saveUpdateButton;  // 동적으로 변경되는 버튼
    private boolean isNewMode = false;  // 신규 모드 플래그
    
    public AttendancePanel() {
        this.attendanceService = new AttendanceService();
        this.developerService = new DeveloperService();
        
        try {
            initializeUI();
            // 데이터 로드는 별도로 처리하여 예외가 발생해도 UI는 표시되도록
            SwingUtilities.invokeLater(() -> {
                try {
                    loadAttendances();
                } catch (Exception e) {
                    System.err.println("근태 데이터 로드 오류: " + e.getMessage());
                    e.printStackTrace();
                    // 오류 발생해도 UI는 표시되도록 빈 상태로 유지
                }
            });
        } catch (Exception e) {
            System.err.println("AttendancePanel 초기화 오류: " + e.getMessage());
            e.printStackTrace();
            // 최소한의 UI라도 표시되도록 시도
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
        
        // 패널이 표시될 때마다 개발자 목록 새로고침
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDevelopers();
            }
        });
    }
    
    /**
     * 헤더 섹션 생성
     */
    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_PRIMARY);
        
        JLabel titleLabel = ModernDesign.createTitleLabel("⏰ 근태 관리");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton refreshButton = ModernDesign.createSecondaryButton("🔄 새로고침");
        refreshButton.addActionListener(e -> loadAttendances());
        buttonPanel.add(refreshButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 테이블 패널 생성
     */
    private JPanel createTablePanel() {
        JPanel panel = ModernDesign.createSection("근태 목록");
        
        // 필터 패널
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JLabel filterLabel = new JLabel("유형 필터:");
        filterLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        filterLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        filterPanel.add(filterLabel);
        
        String[] typeOptions = {"전체", "NORMAL", "LATE", "EARLY_LEAVE", "ABSENT", "VACATION", "SICK_LEAVE"};
        typeFilterCombo = new JComboBox<>(typeOptions);
        typeFilterCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        typeFilterCombo.setBackground(ModernDesign.BG_SECONDARY);
        typeFilterCombo.setPreferredSize(new Dimension(150, 30));
        typeFilterCombo.addActionListener(e -> applyTypeFilter());
        filterPanel.add(typeFilterCombo);
        
        // 테이블
        String[] columnNames = {"날짜", "개발자", "출근", "퇴근", "근무시간(분)", "유형", "비고"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        attendanceTable = new JTable(tableModel);
        ModernDesign.styleTable(attendanceTable);
        
        // 테이블 자동 리사이즈 모드 설정 (수평 스크롤 활성화)
        attendanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // TableRowSorter 설정
        tableSorter = new TableRowSorter<>(tableModel);
        attendanceTable.setRowSorter(tableSorter);
        
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAttendanceSelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 유형 필터 적용
     */
    private void applyTypeFilter() {
        String selectedType = (String) typeFilterCombo.getSelectedItem();
        
        if (selectedType == null || "전체".equals(selectedType)) {
            // 전체 표시
            tableSorter.setRowFilter(null);
        } else {
            // 선택한 유형만 표시 (유형 컬럼은 인덱스 5)
            tableSorter.setRowFilter(RowFilter.regexFilter("^" + selectedType + "$", 5));
        }
    }
    
    /**
     * 입력 폼 패널 생성 (컴팩트하고 유연한 레이아웃)
     */
    private JPanel createFormPanel() {
        JPanel panel = ModernDesign.createSection("근태 정보");
        
        // 폼 패널 - 유연한 레이아웃
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ModernDesign.BG_SECONDARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 10, 6, 10);
        
        int row = 0;
        
        // 개발자 선택
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; gbc.weighty = 0.0;
        formPanel.add(createCompactLabel("개발자 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        developerCombo = createCompactCombo(new String[]{});
        loadDevelopers();
        formPanel.add(developerCombo, gbc);
        row++;
        
        // 날짜
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("날짜 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dateField = createCompactTextField();
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        dateField.setToolTipText("형식: 2025-01-15");
        formPanel.add(dateField, gbc);
        row++;
        gbc.gridx = 1; gbc.weightx = 1.0;
        JLabel dateErrorLabel = new JLabel(" ");
        dateErrorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        dateErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(dateErrorLabel, gbc);
        DateValidator.addDateValidation(dateField, dateErrorLabel, "날짜");
        row++;
        
        // 출근 시간
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("출근"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        checkInField = createCompactTextField();
        checkInField.setText("09:00");
        checkInField.setToolTipText("형식: 09:00");
        formPanel.add(checkInField, gbc);
        row++;
        gbc.gridx = 1; gbc.weightx = 1.0;
        JLabel checkInErrorLabel = new JLabel(" ");
        checkInErrorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        checkInErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(checkInErrorLabel, gbc);
        DateValidator.addTimeValidation(checkInField, checkInErrorLabel, "출근시간");
        row++;
        
        // 퇴근 시간
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("퇴근"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        checkOutField = createCompactTextField();
        checkOutField.setText("18:00");
        checkOutField.setToolTipText("형식: 18:00");
        formPanel.add(checkOutField, gbc);
        row++;
        gbc.gridx = 1; gbc.weightx = 1.0;
        JLabel checkOutErrorLabel = new JLabel(" ");
        checkOutErrorLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        checkOutErrorLabel.setForeground(ModernDesign.ERROR);
        formPanel.add(checkOutErrorLabel, gbc);
        DateValidator.addTimeValidation(checkOutField, checkOutErrorLabel, "퇴근시간");
        row++;
        
        // 유형
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        formPanel.add(createCompactLabel("유형"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        typeCombo = createCompactCombo(new String[]{"NORMAL", "LATE", "EARLY_LEAVE", "ABSENT", "VACATION", "SICK_LEAVE"});
        formPanel.add(typeCombo, gbc);
        row++;
        
        // 비고
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH; gbc.weightx = 0.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
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
        deleteButton.addActionListener(e -> deleteAttendance());
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
        attendanceTable.clearSelection();
        developerCombo.requestFocus();
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
            addAttendance();
        } else {
            updateAttendance();
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
     * 개발자 목록 로드
     */
    private void loadDevelopers() {
        developerCombo.removeAllItems();
        for (Developer dev : developerService.getAllDevelopers()) {
            developerCombo.addItem(dev.getName());
        }
    }
    
    /**
     * 근태 목록 로드
     */
    private void loadAttendances() {
        // EDT에서 실행되도록 보장
        if (SwingUtilities.isEventDispatchThread()) {
            loadAttendancesInternal();
        } else {
            SwingUtilities.invokeLater(() -> loadAttendancesInternal());
        }
    }
    
    /**
     * 근태 목록 로드 (내부 메서드 - EDT에서 실행)
     */
    private void loadAttendancesInternal() {
        try {
            System.out.println("=== 근태 목록 로드 시작 ===");
            
            // 개발자 목록도 함께 새로고침
            loadDevelopers();
            
            // 테이블 모델 초기화
            tableModel.setRowCount(0);
            
            // 데이터 조회
            List<Attendance> attendances = attendanceService.getAllAttendance();
            System.out.println("  조회된 근태 데이터: " + attendances.size() + "건");
            
            // 현재 회사 정보 출력
            com.softone.auto.model.Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
            System.out.println("  현재 회사: " + (currentCompany != null ? currentCompany.getName() + " (ID: " + currentCompany.getId() + ")" : "없음"));
            
            if (attendances.isEmpty()) {
                System.out.println("  ⚠️ 근태 데이터가 없습니다.");
            } else {
                int rowCount = 0;
                for (Attendance att : attendances) {
                    try {
                        // 회사 ID 확인
                        System.out.println("    - 근태: 날짜=" + att.getDate() + ", 개발자=" + att.getDeveloperName() + ", 회사ID=" + att.getCompanyId());
                        
                        tableModel.addRow(new Object[]{
                                att.getDate() != null ? att.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                                att.getDeveloperName() != null ? att.getDeveloperName() : "",
                                att.getCheckIn() != null ? att.getCheckIn().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                                att.getCheckOut() != null ? att.getCheckOut().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                                att.getWorkMinutes() != null ? att.getWorkMinutes() : 0,
                                att.getType() != null ? att.getType() : "",
                                att.getNotes() != null ? att.getNotes() : ""
                        });
                        rowCount++;
                    } catch (Exception rowEx) {
                        System.err.println("  ✗ 근태 데이터 행 추가 실패: " + rowEx.getMessage());
                        System.err.println("    - 날짜: " + att.getDate());
                        System.err.println("    - 개발자: " + att.getDeveloperName());
                        rowEx.printStackTrace();
                    }
                }
                System.out.println("  ✓ 테이블에 추가된 행: " + rowCount + "건");
                
                // 테이블 모델 갱신
                tableModel.fireTableDataChanged();
                
                // 첫 번째 행 자동 선택
                if (tableModel.getRowCount() > 0) {
                    try {
                        attendanceTable.setRowSelectionInterval(0, 0);
                        attendanceTable.scrollRectToVisible(attendanceTable.getCellRect(0, 0, true));
                        System.out.println("  ✓ 첫 번째 행 자동 선택 완료");
                    } catch (Exception selectEx) {
                        System.err.println("  ✗ 행 선택 실패: " + selectEx.getMessage());
                    }
                }
            }
            System.out.println("=== 근태 목록 로드 완료 ===\n");
        } catch (Exception e) {
            System.err.println("✗ 근태 목록 로드 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 사용자에게 오류 알림
            JOptionPane.showMessageDialog(this,
                "근태 목록을 불러오는 중 오류가 발생했습니다:\n\n" +
                ErrorMessageMapper.getUserFriendlyMessage(e),
                "오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 근태 선택 이벤트
     */
    private void onAttendanceSelected() {
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        try {
            // 필터링된 테이블의 경우 뷰 인덱스를 모델 인덱스로 변환
            int modelRow = attendanceTable.convertRowIndexToModel(selectedRow);
            
            if (modelRow < 0 || modelRow >= tableModel.getRowCount()) {
                System.err.println("  ✗ 잘못된 행 인덱스: " + modelRow);
                return;
            }
            
            String dateStr = (String) tableModel.getValueAt(modelRow, 0);
            String devName = (String) tableModel.getValueAt(modelRow, 1);
            
            if (dateStr == null || dateStr.isEmpty() || devName == null || devName.isEmpty()) {
                System.err.println("  ✗ 날짜 또는 개발자 이름이 비어있음");
                return;
            }
            
            System.out.println("=== 근태 선택: " + dateStr + ", " + devName + " ===");
            
            List<Attendance> attendances = attendanceService.getAllAttendance();
            System.out.println("  전체 근태 데이터: " + attendances.size() + "건");
            
            boolean found = false;
            for (Attendance att : attendances) {
                if (att.getDate() != null && att.getDeveloperName() != null) {
                    String attDateStr = att.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    if (attDateStr.equals(dateStr) && att.getDeveloperName().equals(devName)) {
                        selectedAttendance = att;
                        found = true;
                        
                        // 수정 모드로 전환
                        enterEditMode();
                        
                        // 폼 필드 채우기
                        if (att.getDeveloperName() != null) {
                            developerCombo.setSelectedItem(att.getDeveloperName());
                        }
                        if (att.getDate() != null) {
                            dateField.setText(att.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                        }
                        checkInField.setText(att.getCheckIn() != null ? 
                                att.getCheckIn().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                        checkOutField.setText(att.getCheckOut() != null ? 
                                att.getCheckOut().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
                        if (att.getType() != null) {
                            typeCombo.setSelectedItem(att.getType());
                        }
                        notesArea.setText(att.getNotes() != null ? att.getNotes() : "");
                        
                        System.out.println("  ✓ 근태 정보 로드 완료");
                        break;
                    }
                }
            }
            
            if (!found) {
                System.err.println("  ✗ 일치하는 근태 데이터를 찾을 수 없음");
                System.err.println("    - 검색 조건: 날짜=" + dateStr + ", 개발자=" + devName);
            }
        } catch (Exception e) {
            System.err.println("✗ 근태 선택 처리 오류: " + e.getMessage());
            e.printStackTrace();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "선택한 근태 정보를 불러오는 중 오류가 발생했습니다:\n\n" +
                    ErrorMessageMapper.getUserFriendlyMessage(e),
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * 근태 추가
     */
    private void addAttendance() {
        try {
            String devName = (String) developerCombo.getSelectedItem();
            if (devName == null) {
                JOptionPane.showMessageDialog(this, "개발자를 선택하세요.");
                return;
            }
            
            Developer dev = developerService.getDeveloperByName(devName);
            if (dev == null) {
                JOptionPane.showMessageDialog(this, "개발자를 찾을 수 없습니다.");
                return;
            }
            
            // 날짜 검증
            if (!DateValidator.validateDateBeforeSave(dateField.getText().trim(), "날짜", this)) {
                dateField.requestFocus();
                return;
            }
            
            LocalDate date = LocalDate.parse(dateField.getText());
            
            // 출근 시간 검증 (선택적)
            String checkInText = checkInField.getText().trim();
            LocalTime checkIn = null;
            if (!checkInText.isEmpty()) {
                if (!DateValidator.isValidTime(checkInText)) {
                    JOptionPane.showMessageDialog(this, "출근 시간 형식이 올바르지 않습니다.\n형식: HH:mm (예: 09:00)", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    checkInField.requestFocus();
                    return;
                }
                checkIn = LocalTime.parse(checkInText, DateTimeFormatter.ofPattern("HH:mm"));
            }
            
            // 퇴근 시간 검증 (선택적)
            String checkOutText = checkOutField.getText().trim();
            LocalTime checkOut = null;
            if (!checkOutText.isEmpty()) {
                if (!DateValidator.isValidTime(checkOutText)) {
                    JOptionPane.showMessageDialog(this, "퇴근 시간 형식이 올바르지 않습니다.\n형식: HH:mm (예: 18:00)", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    checkOutField.requestFocus();
                    return;
                }
                checkOut = LocalTime.parse(checkOutText, DateTimeFormatter.ofPattern("HH:mm"));
            }
            
            // 현재 회사 확인 (선택적 - 회사가 없어도 저장 가능하지만 경고 표시)
            com.softone.auto.model.Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
            if (currentCompany == null) {
                int result = JOptionPane.showConfirmDialog(this, 
                    "회사가 선택되지 않았습니다.\n\n" +
                    "근태 정보는 회사와 연결되지 않습니다.\n" +
                    "계속하시겠습니까?",
                    "회사 미선택", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            String type = (String) typeCombo.getSelectedItem();
            
            attendanceService.createAttendance(dev.getId(), dev.getName(), date, 
                    checkIn, checkOut, type, notesArea.getText());
            
            loadAttendances();
            enterEditMode();  // 저장 후 수정 모드로 전환
            JOptionPane.showMessageDialog(this, "✅ 근태가 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                "날짜 형식이 올바르지 않습니다.\n형식: yyyy-MM-dd (예: 2025-01-15)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            dateField.requestFocus();
        } catch (IllegalStateException e) {
            // 중복 체크 등 비즈니스 로직 오류
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "중복 등록 오류", 
                JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 근태 저장 중 오류가 발생했습니다.\n\n" +
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
     * 근태 수정
     */
    private void updateAttendance() {
        if (selectedAttendance == null) {
            JOptionPane.showMessageDialog(this, "수정할 근태를 선택하세요.");
            return;
        }
        
        try {
            // 날짜 검증
            if (!DateValidator.validateDateBeforeSave(dateField.getText().trim(), "날짜", this)) {
                dateField.requestFocus();
                return;
            }
            
            LocalDate date = LocalDate.parse(dateField.getText());
            
            // 개발자 확인
            String devName = (String) developerCombo.getSelectedItem();
            if (devName == null) {
                JOptionPane.showMessageDialog(this, "개발자를 선택하세요.");
                return;
            }
            
            Developer dev = developerService.getDeveloperByName(devName);
            if (dev == null) {
                JOptionPane.showMessageDialog(this, "개발자를 찾을 수 없습니다.");
                return;
            }
            
            LocalTime checkIn = !checkInField.getText().isEmpty() ? 
                    LocalTime.parse(checkInField.getText(), DateTimeFormatter.ofPattern("HH:mm")) : null;
            LocalTime checkOut = !checkOutField.getText().isEmpty() ? 
                    LocalTime.parse(checkOutField.getText(), DateTimeFormatter.ofPattern("HH:mm")) : null;
            
            // 날짜나 개발자가 변경된 경우 업데이트
            selectedAttendance.setDate(date);
            selectedAttendance.setDeveloperId(dev.getId());
            selectedAttendance.setDeveloperName(dev.getName());
            selectedAttendance.setCheckIn(checkIn);
            selectedAttendance.setCheckOut(checkOut);
            selectedAttendance.setType((String) typeCombo.getSelectedItem());
            selectedAttendance.setNotes(notesArea.getText());
            
            attendanceService.updateAttendance(selectedAttendance);
            
            loadAttendances();
            clearForm();
            JOptionPane.showMessageDialog(this, "✅ 근태가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                "시간 형식이 올바르지 않습니다.\n형식: HH:mm (예: 09:00)", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            if (checkInField.getText().isEmpty()) {
                checkOutField.requestFocus();
            } else {
                checkInField.requestFocus();
            }
        } catch (RuntimeException e) {
            // 저장 오류의 원인 메시지 추출
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            JOptionPane.showMessageDialog(this, 
                "❌ 근태 수정 중 오류가 발생했습니다.\n\n" +
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
     * 근태 삭제
     */
    private void deleteAttendance() {
        if (selectedAttendance == null) {
            JOptionPane.showMessageDialog(this, "삭제할 근태를 선택하세요.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            attendanceService.deleteAttendance(selectedAttendance.getId());
            JOptionPane.showMessageDialog(this, "근태가 삭제되었습니다.");
            loadAttendances();
            clearForm();
        }
    }
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        checkInField.setText("09:00");
        checkOutField.setText("18:00");
        typeCombo.setSelectedIndex(0);
        notesArea.setText("");
        selectedAttendance = null;
        attendanceTable.clearSelection();
        
        // 초기화 시 수정 모드로 (기본값)
        if (!isNewMode) {
            enterEditMode();
        }
    }
}

