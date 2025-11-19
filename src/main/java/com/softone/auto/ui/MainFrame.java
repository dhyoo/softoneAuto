package com.softone.auto.ui;

import com.softone.auto.model.Company;
import com.softone.auto.service.CompanyService;
import com.softone.auto.util.AppContext;
import com.softone.auto.util.ApplicationMode;
import com.softone.auto.util.AppConfig;
import com.softone.auto.util.ErrorMessageMapper;
import com.softone.auto.util.SampleDataInitializer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 메인 프레임 - 모던 디자인
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JLabel companyTitleLabel;  // 현재 선택된 회사 이름을 표시할 레이블
    private CompanyService companyService;  // final 제거 (샘플 데이터 초기화 후 재생성 가능하도록)
    private DashboardPanel dashboardPanel;  // 대시보드 참조
    
    public MainFrame() {
        // companyService는 final이므로 먼저 초기화
        this.companyService = new CompanyService();
        
        try {
            // 데이터 경로 초기화 (AppConfig 사용)
            String dataPath = com.softone.auto.util.AppConfig.getInstance().getOrSelectDataPath();
            System.out.println("=== 데이터 경로 설정: " + dataPath + " ===");
            
            // 애플리케이션 모드 확인
            AppConfig config = AppConfig.getInstance();
            ApplicationMode mode = config.getApplicationMode();
            System.out.println("=== 애플리케이션 모드: " + mode + " ===");
            System.out.println("=== 샘플 데이터 활성화: " + config.isSampleDataEnabled() + " ===");
            
            initializeUI();
            
            // 샘플 데이터 초기화 (모드에 따라 선택적 실행)
            if (config.isSampleDataEnabled()) {
                // UI 초기화 후 샘플 데이터 초기화 시작
                // initializeSampleData()의 done()에서 loadCompanies()를 호출함
                initializeSampleData();
                // 1년치 대량 샘플 데이터 생성 (백그라운드, 개발/데모 모드에서만)
                if (config.isDevelopmentMode()) {
                    generateBulkData();
                }
            } else {
                System.out.println("프로덕션 모드: 샘플 데이터 생성을 건너뜁니다.");
                // 프로덕션 모드에서는 기존 데이터 즉시 로드
                loadCompanies();
            }
        } catch (Exception e) {
            System.err.println("MainFrame 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            // 최소한 기본 창이라도 표시
            try {
                setTitle("SoftOne Auto Manager - 오류");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setSize(400, 200);
                setLocationRelativeTo(null);
                JLabel errorLabel = new JLabel(
                    "<html><center>프로그램 초기화 중 오류가 발생했습니다.<br>" +
                    "일부 기능이 제한될 수 있습니다.<br><br>" +
                    "오류: " + e.getMessage() + "</center></html>",
                    JLabel.CENTER);
                errorLabel.setVerticalAlignment(JLabel.CENTER);
                add(errorLabel);
            } catch (Exception uiEx) {
                System.err.println("기본 UI 생성도 실패: " + uiEx.getMessage());
                uiEx.printStackTrace();
            }
        }
    }
    
    /**
     * 샘플 데이터 초기화
     */
    private void initializeSampleData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    System.out.println("\n>>> 샘플 데이터 초기화 시작 (백그라운드 스레드) <<<");
                    SampleDataInitializer initializer = new SampleDataInitializer();
                    initializer.initializeAllSampleData();
                    System.out.println(">>> 샘플 데이터 초기화 완료 <<<\n");
                } catch (Exception e) {
                    // 오류를 콘솔에 상세히 출력
                    System.err.println("\n>>> 샘플 데이터 초기화 중 오류 발생 <<<");
                    System.err.println("오류 메시지: " + e.getMessage());
                    System.err.println("오류 클래스: " + e.getClass().getName());
                    e.printStackTrace();
                    ErrorMessageMapper.logError("샘플 데이터 초기화", e);
                    // 예외를 다시 던지지 않아서 애플리케이션이 계속 실행됨
                }
                return null;
            }
            
            @Override
            protected void done() {
                // 샘플 데이터 초기화 완료 후 회사 목록 다시 로드
                System.out.println("샘플 데이터 초기화 프로세스 종료");
                System.out.println(">>> 회사 목록 새로고침 시작 <<<");
                
                // CompanyService를 새로 생성하여 새로운 Connection 사용
                SwingUtilities.invokeLater(() -> {
                    try {
                        System.out.println("  → CompanyService 재생성 중...");
                        MainFrame.this.companyService = new CompanyService();
                        System.out.println("  ✓ CompanyService 재생성 완료");
                        
                        // 회사 목록 다시 로드
                        MainFrame.this.loadCompanies();
                        System.out.println(">>> 회사 목록 새로고침 완료 <<<");
                    } catch (Exception e) {
                        System.err.println("  ✗ CompanyService 재생성 실패: " + e.getMessage());
                        e.printStackTrace();
                        // 재생성 실패해도 기존 서비스로 시도
                        MainFrame.this.loadCompanies();
                    }
                });
            }
        };
        worker.execute();
    }
    
    /**
     * 1년치 대량 데이터 생성 (백그라운드)
     */
    private void generateBulkData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // 5초 대기 후 생성 (기본 샘플 데이터가 먼저 생성되도록)
                try {
                    Thread.sleep(5000);
                    com.softone.auto.util.BulkDataGenerator.generateYearlyData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("대량 데이터 생성이 중단되었습니다", e);
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    System.out.println("대량 데이터 생성 완료");
                } catch (Exception e) {
                    ErrorMessageMapper.logError("대량 데이터 생성", e);
                    // 오류는 조용히 로그만 기록 (백그라운드 작업)
                }
            }
        };
        worker.execute();
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        try {
            setTitle("SoftOne Auto Manager - 현장대리인 업무 자동화");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1400, 900);
            setMinimumSize(new Dimension(1000, 600)); // 최소 크기 설정
            setLocationRelativeTo(null); // 화면 중앙에 배치
            
            // 전체 배경 설정
            getContentPane().setBackground(ModernDesign.BG_PRIMARY);
            
            // 툴팁 스타일 설정
            ModernDesign.setupTooltips();
            
            // 메인 컨텐츠 패널
            JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
            mainPanel.setBackground(ModernDesign.BG_PRIMARY);
            
            // 상단 헤더바
            try {
                mainPanel.add(createHeaderBar(), BorderLayout.NORTH);
            } catch (Exception e) {
                System.err.println("헤더바 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 중앙 탭 패널
            try {
                mainPanel.add(createTabbedPane(), BorderLayout.CENTER);
            } catch (Exception e) {
                System.err.println("탭 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
                // 최소한 빈 패널이라도 추가
                mainPanel.add(new JPanel(), BorderLayout.CENTER);
            }
            
            // 하단 상태바
            try {
                mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
            } catch (Exception e) {
                System.err.println("상태바 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            add(mainPanel);
        } catch (Exception e) {
            System.err.println("UI 초기화 중 치명적 오류: " + e.getMessage());
            e.printStackTrace();
            // 최소한 창이라도 표시
            JOptionPane.showMessageDialog(null,
                "프로그램 초기화 중 오류가 발생했습니다.\n" +
                "일부 기능이 제한될 수 있습니다.\n\n" +
                "오류: " + e.getMessage(),
                "초기화 오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 헤더바 생성
     */
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        // 왼쪽: 로고 & 제목
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(ModernDesign.BG_DARK);
        
        // 로고 제거 (심플하게)
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(ModernDesign.BG_DARK);
        
        // 타이틀에 현재 회사 이름 표시 (이모지 제거)
        companyTitleLabel = new JLabel("SoftOne Auto Manager");
        companyTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        companyTitleLabel.setForeground(ModernDesign.TEXT_LIGHT);
        
        JLabel subtitleLabel = new JLabel("현장대리인 업무 자동화 시스템");
        subtitleLabel.setFont(ModernDesign.FONT_SMALL);
        subtitleLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        
        titlePanel.add(companyTitleLabel);
        titlePanel.add(subtitleLabel);
        leftPanel.add(titlePanel);
        
        header.add(leftPanel, BorderLayout.WEST);
        
        return header;
    }
    
    /**
     * 회사 목록 로드
     */
    private void loadCompanies() {
        System.out.println("=== loadCompanies() 시작 ===");
        
        try {
            List<Company> companies = companyService.getActiveCompanies();
            System.out.println("  → 조회된 회사 수: " + companies.size());
            
            if (companies.isEmpty()) {
                System.out.println("  ⚠️ 회사 데이터가 없습니다. 샘플 데이터 초기화를 기다리는 중...");
                companyTitleLabel.setText("SoftOne Auto Manager");
                return;
            }
            
            // 마지막으로 선택한 회사 ID 가져오기
            String lastCompanyId = AppConfig.getInstance().getLastCompanyId();
            Company selectedCompany = null;
            
            if (lastCompanyId != null && !lastCompanyId.isEmpty()) {
                // 마지막 선택 회사 찾기
                for (Company company : companies) {
                    if (company.getId().equals(lastCompanyId)) {
                        selectedCompany = company;
                        System.out.println("  → 마지막 선택 회사 복원: " + company.getName());
                        break;
                    }
                }
            }
            
            // 마지막 선택 회사가 없으면 첫 번째 회사 선택
            if (selectedCompany == null && !companies.isEmpty()) {
                selectedCompany = companies.get(0);
                System.out.println("  → 첫 번째 회사 선택: " + selectedCompany.getName());
            }
            
            // 회사 선택 및 타이틀 업데이트
            if (selectedCompany != null) {
                AppContext.getInstance().setCurrentCompany(selectedCompany);
                updateCompanyTitle(selectedCompany);
                refreshTabs();
            }
        } catch (Exception e) {
            System.err.println("  ✗ 회사 목록 로드 중 오류: " + e.getMessage());
            e.printStackTrace();
            companyTitleLabel.setText("SoftOne Auto Manager");
        }
        
        System.out.println("=== loadCompanies() 완료 ===\n");
    }
    
    /**
     * 회사 타이틀 업데이트
     */
    private void updateCompanyTitle(Company company) {
        if (company != null) {
            companyTitleLabel.setText(company.getName());
        } else {
            companyTitleLabel.setText("SoftOne Auto Manager");
        }
    }
    
    /**
     * 회사 목록 새로고침 (외부 호출용)
     */
    public void refreshCompanyList() {
        System.out.println("=== refreshCompanyList() 시작 ===");
        
        // 현재 선택된 회사 저장 (절대 변경되지 않도록 보장)
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        String currentCompanyId = currentCompany != null ? currentCompany.getId() : null;
        String currentCompanyName = currentCompany != null ? currentCompany.getName() : null;
        System.out.println("  현재 회사 ID: " + currentCompanyId);
        System.out.println("  현재 회사명: " + currentCompanyName);
        
        // 회사 목록 다시 로드
        loadCompanies();
        
        // 이전 선택 복원 시도 (ID로 정확히 찾기)
        if (currentCompanyId != null) {
            Company foundCompany = companyService.getCompanyById(currentCompanyId);
            if (foundCompany != null) {
                // ID 검증: 현재 회사 ID가 변경되지 않았는지 확인
                if (!foundCompany.getId().equals(currentCompanyId)) {
                    System.err.println("  ✗ 치명적 오류: 회사 ID가 변경되었습니다!");
                    System.err.println("    기존 ID: " + currentCompanyId);
                    System.err.println("    새 ID: " + foundCompany.getId());
                    throw new IllegalStateException("회사 ID가 변경되었습니다. 이는 허용되지 않습니다.");
                }
                
                System.out.println("  → 현재 회사 유지: " + foundCompany.getName() + " (ID: " + foundCompany.getId() + ")");
                AppContext.getInstance().setCurrentCompany(foundCompany);
                updateCompanyTitle(foundCompany);
                
                // 회사 수정 시에는 탭을 새로고침하지 않음 (데이터 손실 방지)
                // 회사 변경 시에만 refreshTabs() 호출
                // refreshTabs()는 changeCompany()에서만 호출됨
                System.out.println("  → 탭 새로고침 건너뜀 (회사 수정 시 데이터 보존)");
            } else {
                System.err.println("  ⚠️ 현재 회사를 찾을 수 없음 (ID: " + currentCompanyId + ")");
                // 회사를 찾을 수 없는 경우에도 현재 회사는 유지 (데이터 손실 방지)
                System.out.println("  → 현재 회사 정보 유지 (데이터 손실 방지)");
            }
        }
        
        System.out.println("=== refreshCompanyList() 완료 ===\n");
    }
    
    /**
     * 회사 변경 (시스템설정에서 호출)
     */
    public void changeCompany(Company company) {
        if (company != null) {
            AppContext.getInstance().setCurrentCompany(company);
            updateCompanyTitle(company);
            
            // 마지막 선택 회사 저장
            AppConfig.getInstance().setLastCompanyId(company.getId());
            AppConfig.getInstance().save();
            
            // 탭 패널 새로고침
            refreshTabs();
            
            System.out.println("  → 회사 변경: " + company.getName());
        }
    }
    
    /**
     * 모든 탭 새로고침
     */
    private void refreshTabs() {
        try {
            // 현재 선택된 탭 인덱스 저장
            int selectedIndex = tabbedPane.getSelectedIndex();
            
            // 모든 탭 제거
            tabbedPane.removeAll();
            
            // 탭 다시 추가 (에러 발생 시에도 계속 진행)
            try {
                // 대시보드 패널 재생성 및 참조 저장 (ServiceRegistry를 통한 의존성 주입)
                dashboardPanel = new DashboardPanel(
                    com.softone.auto.util.ServiceRegistry.getDeveloperService(),
                    com.softone.auto.util.ServiceRegistry.getAttendanceService(),
                    com.softone.auto.util.ServiceRegistry.getIssueService(),
                    com.softone.auto.util.ServiceRegistry.getCustomerCommunicationService()
                );
                tabbedPane.addTab("  대시보드  ", createIcon("dashboard"), dashboardPanel, "프로젝트 현황 대시보드");
            } catch (Exception e) {
                System.err.println("대시보드 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  파견회사  ", createIcon("company"), new CompanyPanel(), "파견회사 관리");
            } catch (Exception e) {
                System.err.println("파견회사 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  개발자 관리  ", createIcon("developer"), new DeveloperPanel(), "개발자 정보 관리");
            } catch (Exception e) {
                System.err.println("개발자 관리 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  근태 관리  ", createIcon("attendance"), new AttendancePanel(), "개발자 근태 관리");
            } catch (Exception e) {
                System.err.println("근태 관리 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  주간보고서  ", createIcon("report"), new WeeklyReportPanel(), "주간보고서 작성 및 생성");
            } catch (Exception e) {
                System.err.println("주간보고서 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  이슈 관리  ", createIcon("issue"), new IssuePanel(), "프로젝트 이슈 관리");
            } catch (Exception e) {
                System.err.println("이슈 관리 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  고객 소통  ", createIcon("communication"), new CustomerCommunicationPanel(), "고객 소통 관리");
            } catch (Exception e) {
                System.err.println("고객 소통 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            try {
                tabbedPane.addTab("  시스템 관리  ", createIcon("settings"), new SystemSettingsPanel(), "시스템 설정 및 공통코드 관리");
            } catch (Exception e) {
                System.err.println("시스템 관리 패널 생성 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 탭 변경 이벤트 리스너 재등록 (refreshTabs 후에도 유지)
            tabbedPane.addChangeListener(e -> {
                int idx = tabbedPane.getSelectedIndex();
                String tabName = idx >= 0 ? tabbedPane.getTitleAt(idx).trim() : "";
                System.out.println("탭 변경: " + tabName + " (인덱스: " + idx + ")");
                
                if (idx == 0 && dashboardPanel != null) {
                    SwingUtilities.invokeLater(() -> dashboardPanel.refresh());
                }
            });
            
            // 이전 선택 복원
            if (selectedIndex >= 0 && selectedIndex < tabbedPane.getTabCount()) {
                tabbedPane.setSelectedIndex(selectedIndex);
            }
            
        } catch (Exception e) {
            System.err.println("탭 새로고침 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "탭 새로고침 중 오류가 발생했습니다.\n" + e.getMessage(), 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 탭 패널 생성
     */
    private JComponent createTabbedPane() {
        // 탭 패널 생성
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernDesign.FONT_SUBHEADING);
        tabbedPane.setBackground(ModernDesign.BG_PRIMARY);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 각 기능별 패널 추가 (에러 발생 시에도 계속 진행)
        try {
            // 대시보드 패널 생성 및 참조 저장
            // ServiceRegistry를 통한 의존성 주입
            dashboardPanel = new DashboardPanel(
                com.softone.auto.util.ServiceRegistry.getDeveloperService(),
                com.softone.auto.util.ServiceRegistry.getAttendanceService(),
                com.softone.auto.util.ServiceRegistry.getIssueService(),
                com.softone.auto.util.ServiceRegistry.getCustomerCommunicationService()
            );
            tabbedPane.addTab("  대시보드  ", createIcon("dashboard"), dashboardPanel, "프로젝트 현황 대시보드");
        } catch (Exception e) {
            System.err.println("대시보드 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  파견회사  ", createIcon("company"), new CompanyPanel(), "파견회사 관리");
        } catch (Exception e) {
            System.err.println("파견회사 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  개발자 관리  ", createIcon("developer"), new DeveloperPanel(), "개발자 정보 관리");
        } catch (Exception e) {
            System.err.println("개발자 관리 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  근태 관리  ", createIcon("attendance"), new AttendancePanel(), "개발자 근태 관리");
        } catch (Exception e) {
            System.err.println("근태 관리 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  주간보고서  ", createIcon("report"), new WeeklyReportPanel(), "주간보고서 작성 및 생성");
        } catch (Exception e) {
            System.err.println("주간보고서 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  이슈 관리  ", createIcon("issue"), new IssuePanel(), "프로젝트 이슈 관리");
        } catch (Exception e) {
            System.err.println("이슈 관리 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  고객 소통  ", createIcon("communication"), new CustomerCommunicationPanel(), "고객 소통 관리");
        } catch (Exception e) {
            System.err.println("고객 소통 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            tabbedPane.addTab("  시스템 관리  ", createIcon("settings"), new SystemSettingsPanel(), "시스템 설정 및 공통코드 관리");
        } catch (Exception e) {
            System.err.println("시스템 관리 패널 생성 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 탭 변경 이벤트 리스너 추가
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String selectedTab = selectedIndex >= 0 ? tabbedPane.getTitleAt(selectedIndex).trim() : "";
            System.out.println("탭 변경: " + selectedTab + " (인덱스: " + selectedIndex + ")");
            
            // 대시보드 탭이 선택되면 갱신
            if (selectedIndex == 0) {  // 대시보드는 첫 번째 탭
                if (dashboardPanel != null) {
                    SwingUtilities.invokeLater(() -> dashboardPanel.refresh());
                }
            }
        });
        
        return tabbedPane;
    }
    
    /**
     * 상태바 생성
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(ModernDesign.BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ModernDesign.BORDER),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // 왼쪽: 상태 정보
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JLabel statusLabel = new JLabel("● 시스템 정상");
        statusLabel.setFont(ModernDesign.FONT_SMALL);
        statusLabel.setForeground(ModernDesign.SUCCESS);
        leftPanel.add(statusLabel);
        
        JSeparator separator1 = new JSeparator(SwingConstants.VERTICAL);
        separator1.setPreferredSize(new Dimension(1, 15));
        leftPanel.add(separator1);
        
        JLabel dateLabel = new JLabel("📅 " + java.time.LocalDate.now().toString());
        dateLabel.setFont(ModernDesign.FONT_SMALL);
        dateLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        leftPanel.add(dateLabel);
        
        // 오른쪽: 버전 정보
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JButton aboutButton = new JButton("ℹ️ 정보");
        aboutButton.setFont(ModernDesign.FONT_SMALL);
        aboutButton.setForeground(ModernDesign.PRIMARY);
        aboutButton.setBackground(ModernDesign.BG_SECONDARY);
        aboutButton.setBorderPainted(false);
        aboutButton.setFocusPainted(false);
        aboutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        aboutButton.addActionListener(e -> showAboutDialog());
        rightPanel.add(aboutButton);
        
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(ModernDesign.FONT_SMALL);
        versionLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        rightPanel.add(versionLabel);
        
        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    /**
     * 아이콘 생성 (실제로는 리소스 파일을 사용하지만, 여기서는 null 반환)
     */
    private Icon createIcon(String name) {
        // 추후 아이콘 파일 추가 시 사용
        return null;
    }
    
    /**
     * 정보 다이얼로그 표시
     */
    private void showAboutDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🚀 SoftOne Auto Manager");
        titleLabel.setFont(ModernDesign.FONT_HEADING);
        titleLabel.setForeground(ModernDesign.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(ModernDesign.FONT_BODY);
        versionLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(versionLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        JTextArea featuresArea = new JTextArea(
                "현장대리인 업무 자동화 도구\n\n" +
                "주요 기능:\n" +
                "✓ 개발자 관리\n" +
                "✓ 근태 관리\n" +
                "✓ 주간보고서 자동 생성\n" +
                "✓ 이슈 관리\n" +
                "✓ 고객 소통 관리\n\n" +
                "© 2025 SoftOne Corporation"
        );
        featuresArea.setFont(ModernDesign.FONT_BODY);
        featuresArea.setEditable(false);
        featuresArea.setBackground(panel.getBackground());
        featuresArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(featuresArea);
        
        JOptionPane.showMessageDialog(this, panel, "정보", JOptionPane.PLAIN_MESSAGE);
    }
}

