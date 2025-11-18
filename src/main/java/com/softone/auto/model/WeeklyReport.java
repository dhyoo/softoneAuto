package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 주간 보고서 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReport {
    private String id;                          // 보고서 ID
    private String companyId;                   // 회사 ID
    private String title;                       // 제목
    private LocalDate startDate;                // 주간 시작일 (월요일)
    private LocalDate endDate;                  // 주간 종료일 (금요일)
    private String projectName;                 // 프로젝트명
    private String reporter;                    // 작성자
    
    // 전주 실적
    private List<WorkItem> lastWeekWork = new ArrayList<>();        // 전주 수행 업무
    
    // 금주 계획
    private List<WorkItem> thisWeekPlan = new ArrayList<>();        // 금주 계획 업무
    
    // 이슈 사항
    private List<IssueItem> issues = new ArrayList<>();             // 이슈 목록
    
    // 근태 현황
    private List<AttendanceSummary> attendanceSummaries = new ArrayList<>(); // 개발자별 근태 요약
    
    private String additionalNotes;             // 추가 특이사항
    private LocalDate createdDate;              // 작성일
    
    // 주요 10가지 체크 사항 (각 항목의 체크 여부)
    private List<Boolean> checkItems = new ArrayList<>();  // 10개의 체크박스 상태
    
    // 금주/차주 업무 통계 (수동 입력)
    private Integer thisWeekRequestCount = 0;   // 금주 요청 건수
    private Integer thisWeekCompleteCount = 0;  // 금주 완료 건수
    private Integer nextWeekRequestCount = 0;   // 차주 요청 건수
    private Integer nextWeekCompleteCount = 0;  // 차주 완료 건수
    
    // 금주/차주 업무 내용 (텍스트 그대로 저장)
    private String thisWeekTasksText = "";      // 금주 업무 내용
    private String nextWeekTasksText = "";      // 차주 계획 내용
    
    /**
     * 업무 항목
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkItem {
        private String task;            // 업무명
        private String assignee;        // 담당자
        private String status;          // 상태 (완료, 진행중, 지연)
        private Integer progress;       // 진척도 (%)
        private String notes;           // 비고
    }
    
    /**
     * 이슈 항목
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueItem {
        private String issue;           // 이슈 내용
        private String severity;        // 심각도 (높음, 보통, 낮음)
        private String status;          // 상태 (미해결, 진행중, 해결)
        private String action;          // 조치 사항
    }
    
    /**
     * 근태 요약
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceSummary {
        private String developerName;   // 개발자명
        private Integer workDays;       // 근무일수
        private Integer lateDays;       // 지각일수
        private Integer vacationDays;   // 휴가일수
        private String notes;           // 비고
    }
}

