package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이슈 관리 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Issue {
    private String id;                  // 이슈 ID
    private String companyId;           // 파견 회사 ID
    private String title;               // 제목
    private String description;         // 상세 내용
    private String category;            // 카테고리 (기술, 일정, 인력, 기타)
    private String severity;            // 심각도 (높음, 보통, 낮음)
    private String status;              // 상태 (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
    private String reporter;            // 보고자
    private String assignee;            // 담당자
    private LocalDateTime createdDate;  // 등록일
    private LocalDateTime updatedDate;  // 수정일
    private LocalDateTime resolvedDate; // 해결일
    private String resolution;          // 해결 방안
    private String notes;               // 비고
}

