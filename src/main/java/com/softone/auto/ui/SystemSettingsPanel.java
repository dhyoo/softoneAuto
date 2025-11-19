package com.softone.auto.ui;

import com.softone.auto.model.CommonCode;
import com.softone.auto.model.Company;
import com.softone.auto.service.CommonCodeService;
import com.softone.auto.service.CompanyService;
import com.softone.auto.util.AppConfig;
import com.softone.auto.util.AppContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 시스템 관리 패널
 */
public class SystemSettingsPanel extends JPanel {
    
    private final CommonCodeService commonCodeService;
    private final CompanyService companyService;
    
    private JComboBox<String> companyComboBox;
    private boolean isInitializing = false;  // 초기 로드 중인지 여부
    private JTextField dataPathField;
    private JTable categoryTable;          // 카테고리(공통코드) 테이블
    private DefaultTableModel categoryTableModel;
    private JTable detailCodeTable;        // 상세코드 테이블
    private DefaultTableModel detailCodeTableModel;
    
    private JTextField categoryField;
    private JTextField codeField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField sortOrderField;
    private JCheckBox activeCheckBox;
    
    private String selectedCategory;       // 선택된 카테고리
    private CommonCode selectedCode;
    private JButton saveUpdateButton;
    private boolean isNewMode = false;
    
    public SystemSettingsPanel() {
        this.commonCodeService = new CommonCodeService();
        this.companyService = new CompanyService();
        initializeUI();
        loadCategories();
        loadCompanies();
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernDesign.BG_PRIMARY);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(ModernDesign.BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // 1. 데이터 경로 설정 (상단) - 조금 더 높게
        JPanel topSection = createDataPathSection();
        topSection.setPreferredSize(new Dimension(0, 140));
        mainPanel.add(topSection, BorderLayout.NORTH);
        
        // 2. 공통코드 관리 (중앙 - 나머지 공간 모두 사용)
        JPanel centerSection = createCommonCodeSection();
        mainPanel.add(centerSection, BorderLayout.CENTER);
        
        // 3. 기타 환경 설정 (하단) - 충분한 높이 확보 (다크모드까지 모두 표시)
        JPanel bottomSection = createEnvironmentSection();
        bottomSection.setPreferredSize(new Dimension(0, 220));
        mainPanel.add(bottomSection, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 데이터 경로 설정 섹션 (모던 디자인)
     */
    private JPanel createDataPathSection() {
        JPanel section = ModernDesign.createSection("📁 데이터 저장 위치 설정");
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 경로 표시 영역 (가로로 배치)
        JPanel pathRowPanel = new JPanel(new BorderLayout(15, 0));
        pathRowPanel.setBackground(ModernDesign.BG_SECONDARY);
        pathRowPanel.setPreferredSize(new Dimension(0, 55));
        
        // 라벨
        JLabel pathLabel = new JLabel("저장 경로");
        pathLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        pathLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        pathLabel.setPreferredSize(new Dimension(90, 45));
        pathRowPanel.add(pathLabel, BorderLayout.WEST);
        
        // 경로 필드 (확장 가능하게)
        dataPathField = new JTextField();
        dataPathField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        dataPathField.setEditable(false);
        dataPathField.setBackground(new Color(248, 249, 250));
        String currentPath = AppConfig.getInstance().getOrSelectDataPath();
        dataPathField.setText(currentPath);
        dataPathField.setToolTipText(currentPath);
        dataPathField.setCaretPosition(0);
        dataPathField.setPreferredSize(new Dimension(0, 45));
        dataPathField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        pathRowPanel.add(dataPathField, BorderLayout.CENTER);
        
        // 버튼 패널 (오른쪽에 배치)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(ModernDesign.BG_SECONDARY);
        buttonPanel.setPreferredSize(new Dimension(240, 45));
        
        JButton changePathButton = UIUtils.createUnifiedButton("경로 변경");
        changePathButton.setPreferredSize(new Dimension(110, 38));
        changePathButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        changePathButton.addActionListener(e -> changeDataPath());
        buttonPanel.add(changePathButton);
        
        JButton openFolderButton = UIUtils.createUnifiedButton("폴더 열기");
        openFolderButton.setPreferredSize(new Dimension(110, 38));
        openFolderButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        openFolderButton.addActionListener(e -> openDataFolder());
        buttonPanel.add(openFolderButton);
        
        pathRowPanel.add(buttonPanel, BorderLayout.EAST);
        
        contentPanel.add(pathRowPanel);
        section.add(contentPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    /**
     * 공통코드 관리 섹션 (3단 구조: 공통코드 - 상세코드 - 상세화면)
     */
    private JPanel createCommonCodeSection() {
        JPanel section = ModernDesign.createSection("⚙️ 공통코드 관리");
        
        // 3단 구조 생성
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setBorder(null);
        mainSplitPane.setResizeWeight(0.25); // 좌측 25%
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setOneTouchExpandable(true);
        
        // 좌측: 카테고리(공통코드) 목록
        mainSplitPane.setLeftComponent(createCategoryListPanel());
        
        // 중앙+우측 패널
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplitPane.setBorder(null);
        rightSplitPane.setResizeWeight(0.5); // 중앙 50%, 우측 50%
        rightSplitPane.setDividerSize(5);
        rightSplitPane.setContinuousLayout(true);
        rightSplitPane.setOneTouchExpandable(true);
        
        // 중앙: 상세코드 목록
        rightSplitPane.setLeftComponent(createDetailCodeListPanel());
        
        // 우측: 상세 정보 폼
        rightSplitPane.setRightComponent(createCodeFormPanel());
        
        mainSplitPane.setRightComponent(rightSplitPane);
        
        // 초기 divider 위치 설정 (컴포넌트가 표시된 후에 설정)
        SwingUtilities.invokeLater(() -> {
            mainSplitPane.setDividerLocation(0.25);  // 좌측 25% 지점
            rightSplitPane.setDividerLocation(0.5);   // 중앙 50% 지점
        });
        
        JPanel contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contentPanel.add(mainSplitPane, BorderLayout.CENTER);
        
        section.add(contentPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    /**
     * 카테고리(공통코드) 리스트 패널
     */
    private JPanel createCategoryListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ModernDesign.BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_SECONDARY);
        header.setPreferredSize(new Dimension(0, 35));
        
        JLabel titleLabel = new JLabel("공통코드 (카테고리)");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        titleLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);
        
        JButton refreshButton = UIUtils.createUnifiedButton("새로고침");
        refreshButton.setPreferredSize(new Dimension(85, 28));
        refreshButton.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        refreshButton.addActionListener(e -> loadCategories());
        header.add(refreshButton, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        
        // 테이블
        String[] columns = {"카테고리"};
        categoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        categoryTable.setRowHeight(30);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onCategorySelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernDesign.BORDER));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 상세코드 리스트 패널
     */
    private JPanel createDetailCodeListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ModernDesign.BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_SECONDARY);
        header.setPreferredSize(new Dimension(0, 35));
        
        JLabel titleLabel = new JLabel("상세코드");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        titleLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.CENTER);
        
        panel.add(header, BorderLayout.NORTH);
        
        // 테이블
        String[] columns = {"코드명", "코드", "순서"};
        detailCodeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        detailCodeTable = new JTable(detailCodeTableModel);
        detailCodeTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        detailCodeTable.setRowHeight(30);
        detailCodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        detailCodeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onDetailCodeSelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(detailCodeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ModernDesign.BORDER));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 코드 입력 폼 패널 (한눈에 보이는 컴팩트 디자인)
     */
    private JPanel createCodeFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ModernDesign.BG_SECONDARY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 폼 영역 (스크롤 없이 한 화면에 표시)
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBackground(ModernDesign.BG_SECONDARY);
        
        // 제목
        JLabel titleLabel = new JLabel("📝 상세 정보");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        titleLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContent.add(titleLabel);
        formContent.add(Box.createVerticalStrut(8));
        
        // 카테고리
        formContent.add(createCompactFormField("카테고리 *", categoryField = createModernTextField(), 
            "예: POSITION, STATUS, SEVERITY"));
        formContent.add(Box.createVerticalStrut(6));
        
        // 코드
        formContent.add(createCompactFormField("코드 *", codeField = createModernTextField(),
            "예: SENIOR, ACTIVE, HIGH"));
        formContent.add(Box.createVerticalStrut(6));
        
        // 코드명
        formContent.add(createCompactFormField("코드명 *", nameField = createModernTextField(),
            "예: 선임, 활성, 높음"));
        formContent.add(Box.createVerticalStrut(6));
        
        // 정렬 순서
        sortOrderField = createModernTextField();
        sortOrderField.setText("0");
        formContent.add(createCompactFormField("정렬 순서", sortOrderField, "숫자로 입력"));
        formContent.add(Box.createVerticalStrut(6));
        
        // 사용 여부
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkPanel.setBackground(ModernDesign.BG_SECONDARY);
        checkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        
        JLabel checkLabel = new JLabel("사용 여부");
        checkLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        checkLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        
        activeCheckBox = new JCheckBox("사용");
        activeCheckBox.setSelected(true);
        activeCheckBox.setBackground(ModernDesign.BG_SECONDARY);
        activeCheckBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        activeCheckBox.setFocusPainted(false);
        
        checkPanel.add(checkLabel);
        checkPanel.add(Box.createHorizontalStrut(10));
        checkPanel.add(activeCheckBox);
        formContent.add(checkPanel);
        formContent.add(Box.createVerticalStrut(6));
        
        // 설명
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
        descPanel.setBackground(ModernDesign.BG_SECONDARY);
        descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("설명");
        descLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        descLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descPanel.add(descLabel);
        descPanel.add(Box.createVerticalStrut(4));
        
        descriptionArea = new JTextArea(2, 20);
        descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(new Color(248, 249, 250));
        
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(0, 55));
        descScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        descScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        descScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        descPanel.add(descScrollPane);
        
        formContent.add(descPanel);
        
        // 폼을 스크롤 패널로 감싸서 창이 작아질 때 스크롤 가능하도록
        JScrollPane formScrollPane = new JScrollPane(formContent);
        formScrollPane.setBorder(null);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(formScrollPane, BorderLayout.CENTER);
        
        // 버튼
        panel.add(createCodeButtonPanel(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 컴팩트한 폼 필드 생성 (한눈에 보이도록)
     */
    private JPanel createCompactFormField(String labelText, JTextField field, String tooltip) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setBackground(ModernDesign.BG_SECONDARY);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        label.setForeground(ModernDesign.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(4));
        
        field.setToolTipText(tooltip);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(0, 30));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        fieldPanel.add(field);
        
        return fieldPanel;
    }
    
    /**
     * 모던 스타일 텍스트 필드 생성 (컴팩트 버전)
     */
    private JTextField createModernTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        field.setBackground(new Color(248, 249, 250));
        field.setPreferredSize(new Dimension(0, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }
    
    /**
     * 코드 버튼 패널 (컴팩트 스타일)
     */
    private JPanel createCodeButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(ModernDesign.BG_SECONDARY);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)),
            BorderFactory.createEmptyBorder(3, 0, 3, 0)
        ));
        buttonPanel.setPreferredSize(new Dimension(0, 40));
        
        JButton newButton = UIUtils.createUnifiedButton("➕ 신규");
        newButton.setPreferredSize(new Dimension(85, 32));
        newButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        newButton.addActionListener(e -> enterNewMode());
        buttonPanel.add(newButton);
        
        saveUpdateButton = UIUtils.createUnifiedButton("💾 저장");
        saveUpdateButton.setPreferredSize(new Dimension(85, 32));
        saveUpdateButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        saveUpdateButton.addActionListener(e -> saveOrUpdateCode());
        buttonPanel.add(saveUpdateButton);
        
        JButton deleteButton = UIUtils.createUnifiedButton("🗑️ 삭제");
        deleteButton.setPreferredSize(new Dimension(85, 32));
        deleteButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        deleteButton.addActionListener(e -> deleteCode());
        buttonPanel.add(deleteButton);
        
        JButton clearButton = UIUtils.createUnifiedButton("🔄 초기화");
        clearButton.setPreferredSize(new Dimension(85, 32));
        clearButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        return buttonPanel;
    }
    
    /**
     * 기타 환경 설정 섹션 (모던 디자인)
     */
    private JPanel createEnvironmentSection() {
        JPanel section = ModernDesign.createSection("🔧 기타 환경 설정");
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 18, 20));  // 상단 패딩 최소화
        
        // 파견회사 선택 (가장 위에 배치)
        JPanel companyPanel = new JPanel(new BorderLayout(15, 0));
        companyPanel.setBackground(ModernDesign.BG_SECONDARY);
        companyPanel.setPreferredSize(new Dimension(0, 50));
        companyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        companyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel companyLabel = new JLabel("파견회사");
        companyLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        companyLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        companyLabel.setPreferredSize(new Dimension(120, 50));
        companyLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        companyComboBox = new JComboBox<>();
        companyComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        companyComboBox.setPreferredSize(new Dimension(250, 38));
        companyComboBox.setMaximumSize(new Dimension(250, 38));
        companyComboBox.addActionListener(e -> onCompanyChanged());
        
        companyPanel.add(companyLabel, BorderLayout.WEST);
        companyPanel.add(companyComboBox, BorderLayout.CENTER);
        contentPanel.add(Box.createVerticalStrut(2));  // 최소 간격
        contentPanel.add(companyPanel);
        contentPanel.add(Box.createVerticalStrut(12));
        
        // 언어 설정
        JPanel langPanel = new JPanel(new BorderLayout(15, 0));
        langPanel.setBackground(ModernDesign.BG_SECONDARY);
        langPanel.setPreferredSize(new Dimension(0, 45));
        langPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        langPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel langLabel = new JLabel("언어");
        langLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        langLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        langLabel.setPreferredSize(new Dimension(120, 45));
        
        JComboBox<String> langCombo = new JComboBox<>(new String[]{"한국어", "English"});
        langCombo.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        langCombo.setPreferredSize(new Dimension(200, 38));
        
        langPanel.add(langLabel, BorderLayout.WEST);
        langPanel.add(langCombo, BorderLayout.CENTER);
        contentPanel.add(langPanel);
        contentPanel.add(Box.createVerticalStrut(12));
        
        // 다크 모드
        JPanel darkModePanel = new JPanel(new BorderLayout(15, 0));
        darkModePanel.setBackground(ModernDesign.BG_SECONDARY);
        darkModePanel.setPreferredSize(new Dimension(0, 45));
        darkModePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        darkModePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel darkLabel = new JLabel("다크 모드");
        darkLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        darkLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        darkLabel.setPreferredSize(new Dimension(120, 45));
        
        JCheckBox darkModeCheck = new JCheckBox("사용 (향후 지원 예정)");
        darkModeCheck.setEnabled(false);
        darkModeCheck.setBackground(ModernDesign.BG_SECONDARY);
        darkModeCheck.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        darkModeCheck.setFocusPainted(false);
        
        darkModePanel.add(darkLabel, BorderLayout.WEST);
        darkModePanel.add(darkModeCheck, BorderLayout.CENTER);
        contentPanel.add(darkModePanel);
        
        section.add(contentPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    /**
     * 데이터 경로 변경
     */
    private void changeDataPath() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "데이터 경로를 변경하면 애플리케이션이 재시작됩니다.\n계속하시겠습니까?",
            "경로 변경 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            AppConfig config = AppConfig.getInstance();
            config.resetDataPath();
            dataPathField.setText(config.getDataPath());
            
            JOptionPane.showMessageDialog(this,
                "데이터 경로가 변경되었습니다.\n애플리케이션을 재시작해주세요.",
                "변경 완료",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * 데이터 폴더 열기
     */
    private void openDataFolder() {
        try {
            String path = AppConfig.getInstance().getOrSelectDataPath();
            Desktop.getDesktop().open(new File(path));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "폴더를 열 수 없습니다: " + e.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 회사 목록 로드
     */
    private void loadCompanies() {
        System.out.println("=== loadCompanies() 호출 ===");
        companyComboBox.removeAllItems();
        try {
            // 초기 로드 중 플래그 설정 (이벤트 발생 방지)
            isInitializing = true;
            System.out.println("  → isInitializing = true 설정");
            
            List<Company> companies = companyService.getActiveCompanies();
            System.out.println("  → 조회된 회사 수: " + companies.size());
            for (Company company : companies) {
                companyComboBox.addItem(company.getName());
            }
            
            // 현재 선택된 회사 설정
            Company currentCompany = AppContext.getInstance().getCurrentCompany();
            System.out.println("  → 현재 회사: " + (currentCompany != null ? currentCompany.getName() : "null"));
            if (currentCompany != null) {
                // isInitializing이 true이므로 setSelectedIndex 시 이벤트가 발생하지 않음
                for (int i = 0; i < companyComboBox.getItemCount(); i++) {
                    if (currentCompany.getName().equals(companyComboBox.getItemAt(i))) {
                        companyComboBox.setSelectedIndex(i);
                        System.out.println("  → 초기 로드: 현재 회사로 설정 - " + currentCompany.getName() + " (인덱스: " + i + ")");
                        break;
                    }
                }
            } else {
                System.out.println("  → 초기 로드: 현재 회사가 null이므로 첫 번째 회사 선택");
                if (companyComboBox.getItemCount() > 0) {
                    companyComboBox.setSelectedIndex(0);
                    System.out.println("  → 첫 번째 회사 선택: " + companyComboBox.getItemAt(0));
                }
            }
        } catch (Exception e) {
            System.err.println("회사 목록 로드 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 초기 로드 완료
            isInitializing = false;
            System.out.println("  → isInitializing = false 설정 (초기 로드 완료)");
            System.out.println("=== loadCompanies() 완료 ===\n");
        }
    }
    
    /**
     * 회사 변경 이벤트
     */
    private void onCompanyChanged() {
        System.out.println("=== onCompanyChanged() 호출 ===");
        System.out.println("  isInitializing: " + isInitializing);
        System.out.println("  이벤트 소스: " + (companyComboBox.getSelectedItem() != null ? companyComboBox.getSelectedItem() : "null"));
        
        // 초기 로드 중이면 이벤트 무시
        if (isInitializing) {
            System.out.println("  → 초기 로드 중이므로 이벤트 무시");
            return;
        }
        
        String selectedName = (String) companyComboBox.getSelectedItem();
        System.out.println("  선택된 회사명: " + selectedName);
        
        if (selectedName == null) {
            System.out.println("  → 선택된 회사명이 null이므로 종료");
            return;
        }
        
        // 현재 선택된 회사 확인
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        String currentCompanyName = currentCompany != null ? currentCompany.getName() : "null";
        System.out.println("  현재 회사: " + currentCompanyName);
        System.out.println("  선택된 회사와 현재 회사 비교: '" + selectedName + "' vs '" + currentCompanyName + "'");
        System.out.println("  같음 여부: " + (currentCompany != null && currentCompany.getName().equals(selectedName)));
        
        if (currentCompany != null && currentCompany.getName().equals(selectedName)) {
            // 같은 회사면 변경하지 않음
            System.out.println("  → 같은 회사이므로 변경하지 않음");
            return;
        }
        
        // 확인 다이얼로그 표시
        System.out.println("  → 확인 다이얼로그 표시");
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "파견회사를 '" + selectedName + "'로 변경하시겠습니까?\n\n" +
            "변경 시 모든 탭의 데이터가 새로고침됩니다.",
            "파견회사 변경 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        System.out.println("  확인 결과: " + (confirm == JOptionPane.YES_OPTION ? "YES" : "NO"));
        
        if (confirm != JOptionPane.YES_OPTION) {
            // 취소 시 이전 선택으로 복원
            System.out.println("  → 사용자가 취소함. 이전 선택으로 복원");
            if (currentCompany != null) {
                // isInitializing을 true로 설정하여 setSelectedIndex 시 이벤트 발생 방지
                isInitializing = true;
                try {
                    for (int i = 0; i < companyComboBox.getItemCount(); i++) {
                        if (currentCompany.getName().equals(companyComboBox.getItemAt(i))) {
                            companyComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } finally {
                    isInitializing = false;
                }
            }
            return;
        }
        
        try {
            System.out.println("  → 회사 변경 진행: " + selectedName);
            Company company = companyService.getCompanyByName(selectedName);
            if (company != null) {
                System.out.println("  → 회사 찾음: " + company.getName() + " (ID: " + company.getId() + ")");
                // MainFrame에 회사 변경 알림
                Container parent = getParent();
                while (parent != null && !(parent instanceof MainFrame)) {
                    parent = parent.getParent();
                }
                if (parent instanceof MainFrame) {
                    System.out.println("  → MainFrame.changeCompany() 호출");
                    ((MainFrame) parent).changeCompany(company);
                } else {
                    System.err.println("  ✗ MainFrame을 찾을 수 없음");
                }
            } else {
                System.err.println("  ✗ 회사를 찾을 수 없음: " + selectedName);
            }
        } catch (Exception e) {
            System.err.println("회사 변경 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 오류 발생 시 이전 선택으로 복원
            if (currentCompany != null) {
                for (int i = 0; i < companyComboBox.getItemCount(); i++) {
                    if (currentCompany.getName().equals(companyComboBox.getItemAt(i))) {
                        companyComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            JOptionPane.showMessageDialog(
                this,
                "파견회사 변경 중 오류가 발생했습니다:\n\n" + e.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 카테고리 목록 로드
     */
    private void loadCategories() {
        categoryTableModel.setRowCount(0);
        List<CommonCode> allCodes = commonCodeService.getAllCodes();
        
        // 중복 제거하여 카테고리만 추출
        allCodes.stream()
            .map(CommonCode::getCategory)
            .distinct()
            .sorted()
            .forEach(category -> categoryTableModel.addRow(new Object[]{category}));
    }
    
    /**
     * 카테고리 선택 이벤트
     */
    private void onCategorySelected() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedCategory = (String) categoryTableModel.getValueAt(selectedRow, 0);
            loadDetailCodes(selectedCategory);
            clearForm();
        }
    }
    
    /**
     * 선택된 카테고리의 상세코드 로드
     */
    private void loadDetailCodes(String category) {
        detailCodeTableModel.setRowCount(0);
        List<CommonCode> codes = commonCodeService.getAllCodes();
        
        codes.stream()
            .filter(code -> code.getCategory().equals(category))
            .sorted((c1, c2) -> {
                Integer order1 = c1.getSortOrder() != null ? c1.getSortOrder() : 0;
                Integer order2 = c2.getSortOrder() != null ? c2.getSortOrder() : 0;
                return order1.compareTo(order2);
            })
            .forEach(code -> detailCodeTableModel.addRow(new Object[]{
                code.getName(),  // 코드명(한글)을 첫 번째 컬럼에 표시
                code.getCode(),  // 영문 코드를 두 번째 컬럼에 표시
                code.getSortOrder()
            }));
    }
    
    /**
     * 상세코드 선택 이벤트
     */
    private void onDetailCodeSelected() {
        int selectedRow = detailCodeTable.getSelectedRow();
        if (selectedRow >= 0 && selectedCategory != null) {
            // 코드명(한글)이 첫 번째 컬럼, 코드(영문)가 두 번째 컬럼
            String codeName = (String) detailCodeTableModel.getValueAt(selectedRow, 0);
            String code = (String) detailCodeTableModel.getValueAt(selectedRow, 1);
            
            // 선택된 코드 찾기
            List<CommonCode> codes = commonCodeService.getAllCodes();
            selectedCode = codes.stream()
                .filter(c -> c.getCategory().equals(selectedCategory) && c.getCode().equals(code))
                .findFirst()
                .orElse(null);
            
            if (selectedCode != null) {
                enterEditMode();
                loadCodeToForm(selectedCode);
            }
        }
    }
    
    /**
     * 코드 데이터를 폼에 로드
     */
    private void loadCodeToForm(CommonCode code) {
        categoryField.setText(code.getCategory());
        codeField.setText(code.getCode());
        nameField.setText(code.getName());
        descriptionArea.setText(code.getDescription() != null ? code.getDescription() : "");
        sortOrderField.setText(String.valueOf(code.getSortOrder() != null ? code.getSortOrder() : 0));
        activeCheckBox.setSelected(code.getIsActive() != null && code.getIsActive());
    }
    
    /**
     * 신규 모드
     */
    private void enterNewMode() {
        isNewMode = true;
        selectedCode = null;
        clearForm();
        saveUpdateButton.setText("저장");
        
        // 선택된 카테고리가 있으면 자동으로 설정
        if (selectedCategory != null) {
            categoryField.setText(selectedCategory);
            categoryField.setEditable(false);
            codeField.requestFocus();
        } else {
            categoryField.setEditable(true);
            categoryField.requestFocus();
        }
        
        detailCodeTable.clearSelection();
    }
    
    /**
     * 수정 모드
     */
    private void enterEditMode() {
        isNewMode = false;
        saveUpdateButton.setText("수정");
        categoryField.setEditable(false);
    }
    
    /**
     * 저장 또는 수정
     */
    private void saveOrUpdateCode() {
        try {
            String category = categoryField.getText().trim().toUpperCase();
            String code = codeField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            Integer sortOrder = Integer.parseInt(sortOrderField.getText().trim());
            
            if (category.isEmpty() || code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "필수 항목을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (isNewMode) {
                selectedCode = commonCodeService.createCode(category, code, name, 
                    descriptionArea.getText(), sortOrder);
                JOptionPane.showMessageDialog(this, "✅ 공통코드가 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                selectedCode.setCategory(category);
                selectedCode.setCode(code);
                selectedCode.setName(name);
                selectedCode.setDescription(descriptionArea.getText());
                selectedCode.setSortOrder(sortOrder);
                selectedCode.setIsActive(activeCheckBox.isSelected());
                
                commonCodeService.updateCode(selectedCode);
                JOptionPane.showMessageDialog(this, "✅ 공통코드가 수정되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // 카테고리 목록 새로고침
            loadCategories();
            
            // 현재 카테고리의 상세코드 새로고침
            if (selectedCategory != null) {
                loadDetailCodes(selectedCategory);
            } else if (category != null) {
                selectedCategory = category;
                loadDetailCodes(selectedCategory);
            }
            
            enterEditMode();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "정렬 순서는 숫자여야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 코드 삭제
     */
    private void deleteCode() {
        if (selectedCode == null) {
            JOptionPane.showMessageDialog(this, "삭제할 코드를 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "정말 삭제하시겠습니까?", 
            "삭제 확인", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            commonCodeService.deleteCode(selectedCode.getId());
            JOptionPane.showMessageDialog(this, "삭제되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
            
            // 카테고리 목록 새로고침
            loadCategories();
            
            // 현재 카테고리의 상세코드 새로고침
            if (selectedCategory != null) {
                loadDetailCodes(selectedCategory);
            }
            
            enterNewMode();
        }
    }
    
    /**
     * 폼 초기화
     */
    private void clearForm() {
        // 선택된 카테고리가 있으면 유지
        if (selectedCategory == null) {
            categoryField.setText("");
            categoryField.setEditable(true);
        } else {
            categoryField.setText(selectedCategory);
            categoryField.setEditable(false);
        }
        
        codeField.setText("");
        nameField.setText("");
        descriptionArea.setText("");
        sortOrderField.setText("0");
        activeCheckBox.setSelected(true);
        selectedCode = null;
        detailCodeTable.clearSelection();
    }
}

