package com.softone.auto.ui;

import com.softone.auto.service.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 대시보드 패널 - 프로젝트 현황 요약 (모던 디자인)
 * 
 * <p>의존성 주입(DI) 패턴을 사용하여 Service 인스턴스를 외부에서 주입받습니다.</p>
 * <p>이를 통해 테스트 용이성과 결합도 감소를 달성합니다.</p>
 */
public class DashboardPanel extends JPanel {
    
    private final DeveloperService developerService;
    private final AttendanceService attendanceService;
    private final IssueService issueService;
    private final CustomerCommunicationService communicationService;
    
    /**
     * 생성자 - 의존성 주입 방식
     * 
     * @param developerService 개발자 관리 서비스
     * @param attendanceService 근태 관리 서비스
     * @param issueService 이슈 관리 서비스
     * @param communicationService 고객 소통 관리 서비스
     */
    public DashboardPanel(DeveloperService developerService,
                         AttendanceService attendanceService,
                         IssueService issueService,
                         CustomerCommunicationService communicationService) {
        this.developerService = developerService;
        this.attendanceService = attendanceService;
        this.issueService = issueService;
        this.communicationService = communicationService;
        
        try {
            initializeUI();
        } catch (Exception e) {
            System.err.println("DashboardPanel 초기화 오류: " + e.getMessage());
            e.printStackTrace();
            try {
                initializeUI();
            } catch (Exception uiEx) {
                System.err.println("UI 초기화도 실패: " + uiEx.getMessage());
            }
        }
    }
    
    /**
     * 대시보드 갱신 (탭 선택 시 호출)
     */
    public void refresh() {
        System.out.println("=== 대시보드 갱신 시작 ===");
        
        try {
            // UI를 다시 그림
            removeAll();
            initializeUI();
            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("대시보드 갱신 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 대시보드 갱신 완료 ===\n");
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ModernDesign.BG_PRIMARY);
        
        // 메인 스크롤 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ModernDesign.BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // 상단 헤더
        mainPanel.add(createHeaderSection());
        mainPanel.add(Box.createVerticalStrut(30));
        
        // 통계 카드 섹션
        mainPanel.add(createStatsSection());
        mainPanel.add(Box.createVerticalStrut(30));
        
        // 빠른 액세스 버튼 섹션
        mainPanel.add(createQuickActionsSection());
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 최근 활동 섹션
        mainPanel.add(createRecentActivitySection());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 헤더 섹션 생성
     */
    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernDesign.BG_PRIMARY);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // 왼쪽: 제목 & 설명 & 현재 회사
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JLabel titleLabel = ModernDesign.createTitleLabel("프로젝트 현황 대시보드");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("실시간 프로젝트 지표 및 주요 메트릭스");
        subtitleLabel.setFont(ModernDesign.FONT_BODY);
        subtitleLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        
        // 현재 회사 정보 표시
        JPanel companyInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        companyInfoPanel.setBackground(ModernDesign.BG_PRIMARY);
        companyInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        com.softone.auto.model.Company currentCompany = com.softone.auto.util.AppContext.getInstance().getCurrentCompany();
        String companyInfo = "전체";
        String projectInfo = "";
        
        if (currentCompany != null) {
            companyInfo = currentCompany.getName();
            projectInfo = " • " + currentCompany.getProjectName();
        }
        
        JLabel companyLabel = new JLabel("현재 보기: ");
        companyLabel.setFont(ModernDesign.FONT_BODY);
        companyLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        
        JLabel companyNameLabel = new JLabel(companyInfo);
        companyNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        companyNameLabel.setForeground(ModernDesign.PRIMARY);
        
        JLabel projectLabel = new JLabel(projectInfo);
        projectLabel.setFont(ModernDesign.FONT_SMALL);
        projectLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        
        companyInfoPanel.add(companyLabel);
        companyInfoPanel.add(companyNameLabel);
        companyInfoPanel.add(projectLabel);
        
        leftPanel.add(companyInfoPanel);
        
        // 오른쪽: 새로고침 버튼
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(ModernDesign.BG_PRIMARY);
        
        JButton refreshButton = createUnifiedButton("새로고침");
        refreshButton.addActionListener(e -> {
            refresh();
            JOptionPane.showMessageDialog(this, "데이터가 새로고침되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        });
        rightPanel.add(refreshButton);
        
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * 통계 카드 섹션 생성
     */
    private JPanel createStatsSection() {
        JPanel section = new JPanel(new GridLayout(2, 3, 20, 20));
        section.setBackground(ModernDesign.BG_PRIMARY);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        
        // 실제 데이터 조회 (안전하게)
        int developerCount = 0;
        int openIssueCount = 0;
        int pendingCommCount = 0;
        int completedIssueCount = 0;
        String attendanceRate = "0%";
        
        try {
            // 개발자 수
            var developers = developerService.getAllDevelopers();
            developerCount = developers != null ? developers.size() : 0;
            
            // 미해결 이슈 수
            var allIssues = issueService.getAllIssues();
            if (allIssues != null) {
                openIssueCount = (int) allIssues.stream()
                    .filter(issue -> "OPEN".equals(issue.getStatus()) || "IN_PROGRESS".equals(issue.getStatus()))
                    .count();
                
                // 완료된 이슈 수
                completedIssueCount = (int) allIssues.stream()
                    .filter(issue -> "RESOLVED".equals(issue.getStatus()) || "CLOSED".equals(issue.getStatus()))
                    .count();
            }
            
            // 대기중인 소통 수
            var allComms = communicationService.getAllCommunications();
            if (allComms != null) {
                pendingCommCount = (int) allComms.stream()
                    .filter(comm -> "PENDING".equals(comm.getStatus()))
                    .count();
            }
            
            // 금주 출석률 계산
            LocalDate today = LocalDate.now();
            LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate friday = monday.plusDays(4);
            
            if (developers != null && !developers.isEmpty()) {
                var attendances = attendanceService.getAttendanceByDateRange(monday, friday);
                if (attendances != null && !attendances.isEmpty()) {
                    long presentCount = attendances.stream()
                        .filter(att -> "PRESENT".equals(att.getType()) || "LATE".equals(att.getType()))
                        .count();
                    long totalExpected = (long) developers.size() * getDaysUntilToday(monday, today);
                    if (totalExpected > 0) {
                        attendanceRate = String.format("%.0f%%", (presentCount * 100.0 / totalExpected));
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("대시보드 데이터 로드 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 카드 생성
        section.add(ModernDesign.createStatsCard("전체 개발자", String.valueOf(developerCount), ModernDesign.PRIMARY, "👥"));
        section.add(ModernDesign.createStatsCard("미해결 이슈", String.valueOf(openIssueCount), ModernDesign.PRIMARY, "⚠️"));
        section.add(ModernDesign.createStatsCard("대기중인 소통", String.valueOf(pendingCommCount), ModernDesign.PRIMARY, "💬"));
        section.add(ModernDesign.createStatsCard("금주 출석률", attendanceRate, ModernDesign.PRIMARY, "📊"));
        section.add(ModernDesign.createStatsCard("완료된 이슈", String.valueOf(completedIssueCount), ModernDesign.PRIMARY, "✅"));
        section.add(ModernDesign.createStatsCard("현재 주차", getCurrentWeek(), ModernDesign.PRIMARY, "📅"));
        
        return section;
    }
    
    /**
     * 월요일부터 오늘까지 평일 수 계산
     */
    private int getDaysUntilToday(LocalDate monday, LocalDate today) {
        int days = 0;
        LocalDate current = monday;
        while (!current.isAfter(today) && !current.isAfter(monday.plusDays(4))) {
            if (current.getDayOfWeek().getValue() < 6) {  // 월~금
                days++;
            }
            current = current.plusDays(1);
        }
        return Math.max(1, days);  // 최소 1
    }
    
    /**
     * 빠른 액세스 섹션 생성
     */
    private JPanel createQuickActionsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(ModernDesign.BG_PRIMARY);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel sectionTitle = ModernDesign.createHeadingLabel("빠른 작업");
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonsPanel.setBackground(ModernDesign.BG_PRIMARY);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton devButton = createUnifiedButton("개발자 추가");
        JButton issueButton = createUnifiedButton("이슈 등록");
        JButton reportButton = createUnifiedButton("보고서 작성");
        JButton commButton = createUnifiedButton("소통 기록");
        
        buttonsPanel.add(devButton);
        buttonsPanel.add(issueButton);
        buttonsPanel.add(reportButton);
        buttonsPanel.add(commButton);
        
        section.add(buttonsPanel);
        
        return section;
    }
    
    /**
     * 최근 활동 섹션 생성
     */
    private JPanel createRecentActivitySection() {
        JPanel section = ModernDesign.createSection("최근 활동");
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        try {
            java.util.List<ActivityItem> activities = new java.util.ArrayList<>();
            
            // 최근 이슈 조회 (최대 2개)
            var issues = issueService.getAllIssues();
            if (issues != null && !issues.isEmpty()) {
                issues.stream()
                    .sorted((i1, i2) -> {
                        if (i1.getCreatedDate() == null) return 1;
                        if (i2.getCreatedDate() == null) return -1;
                        return i2.getCreatedDate().compareTo(i1.getCreatedDate());
                    })
                    .limit(2)
                    .forEach(issue -> {
                        String icon = "RESOLVED".equals(issue.getStatus()) ? "✅" : "⚠️";
                        String title = issue.getTitle();
                        String user = issue.getAssignee() != null ? issue.getAssignee() : "담당자 없음";
                        String time = issue.getCreatedDate() != null ? formatTimeAgo(issue.getCreatedDate().toLocalDate()) : "알 수 없음";
                        activities.add(new ActivityItem(icon, title, user, time));
                    });
            }
            
            // 최근 개발자 추가 (최대 1개)
            var developers = developerService.getAllDevelopers();
            if (developers != null && !developers.isEmpty()) {
                developers.stream()
                    .filter(dev -> dev.getJoinDate() != null)
                    .sorted((d1, d2) -> d2.getJoinDate().compareTo(d1.getJoinDate()))
                    .limit(1)
                    .forEach(dev -> {
                        String title = "새 개발자: " + dev.getName();
                        String time = formatTimeAgo(dev.getJoinDate().atStartOfDay().toLocalDate());
                        activities.add(new ActivityItem("👤", title, "시스템", time));
                    });
            }
            
            // 최근 소통 (최대 2개)
            var comms = communicationService.getAllCommunications();
            if (comms != null && !comms.isEmpty()) {
                comms.stream()
                    .filter(comm -> comm.getCommunicationDate() != null)
                    .sorted((c1, c2) -> c2.getCommunicationDate().compareTo(c1.getCommunicationDate()))
                    .limit(2)
                    .forEach(comm -> {
                        String title = comm.getTitle();
                        String user = comm.getOurRepresentative() != null ? comm.getOurRepresentative() : "담당자 없음";
                        String time = formatTimeAgo(comm.getCommunicationDate().toLocalDate());
                        activities.add(new ActivityItem("💬", title, user, time));
                    });
            }
            
            // 활동이 없으면 기본 메시지
            if (activities.isEmpty()) {
                JLabel noDataLabel = new JLabel("최근 활동이 없습니다.");
                noDataLabel.setFont(ModernDesign.FONT_BODY);
                noDataLabel.setForeground(ModernDesign.TEXT_SECONDARY);
                noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalStrut(20));
                contentPanel.add(noDataLabel);
            } else {
                // 최대 5개 항목 표시
                for (int i = 0; i < Math.min(5, activities.size()); i++) {
                    ActivityItem activity = activities.get(i);
                    contentPanel.add(createActivityItem(activity.icon, activity.title, activity.user, activity.time, ModernDesign.PRIMARY));
                    
                    if (i < Math.min(5, activities.size()) - 1) {
                        contentPanel.add(Box.createVerticalStrut(1));
                        contentPanel.add(createSeparator());
                        contentPanel.add(Box.createVerticalStrut(1));
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("최근 활동 로드 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            JLabel errorLabel = new JLabel("데이터를 불러올 수 없습니다.");
            errorLabel.setFont(ModernDesign.FONT_BODY);
            errorLabel.setForeground(ModernDesign.ERROR);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(errorLabel);
        }
        
        section.add(contentPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    /**
     * 날짜로부터 경과 시간 포맷
     */
    private String formatTimeAgo(LocalDate date) {
        if (date == null) return "알 수 없음";
        
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
        
        if (daysAgo == 0) return "오늘";
        if (daysAgo == 1) return "어제";
        if (daysAgo < 7) return daysAgo + "일 전";
        if (daysAgo < 30) return (daysAgo / 7) + "주 전";
        return (daysAgo / 30) + "개월 전";
    }
    
    /**
     * 활동 항목 데이터 클래스
     */
    private static class ActivityItem {
        String icon;
        String title;
        String user;
        String time;
        
        ActivityItem(String icon, String title, String user, String time) {
            this.icon = icon;
            this.title = title;
            this.user = user;
            this.time = time;
        }
    }
    
    /**
     * 구분선 생성
     */
    private JPanel createSeparator() {
        JPanel separator = new JPanel();
        separator.setBackground(ModernDesign.BORDER);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return separator;
    }
    
    /**
     * 활동 항목 생성
     */
    private JPanel createActivityItem(String icon, String title, String user, String time, Color color) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setBackground(ModernDesign.BG_SECONDARY);
        item.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 아이콘
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(color);
        item.add(iconLabel, BorderLayout.WEST);
        
        // 중앙: 제목 & 사용자
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ModernDesign.FONT_BODY);
        titleLabel.setForeground(ModernDesign.TEXT_PRIMARY);
        
        JLabel userLabel = new JLabel(user);
        userLabel.setFont(ModernDesign.FONT_SMALL);
        userLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        
        centerPanel.add(titleLabel);
        centerPanel.add(userLabel);
        item.add(centerPanel, BorderLayout.CENTER);
        
        // 시간
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(ModernDesign.FONT_SMALL);
        timeLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        item.add(timeLabel, BorderLayout.EAST);
        
        return item;
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
        button.setPreferredSize(new Dimension(100, 30));
        
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
     * 현재 주차 정보 가져오기
     */
    private String getCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate friday = monday.plusDays(4);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        return monday.format(formatter) + " ~ " + friday.format(formatter);
    }
    
}

