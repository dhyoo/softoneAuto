package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 고객 소통 관리 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCommunication {
    private String id;                  // 소통 ID
    private String companyId;           // 파견 회사 ID
    private String type;                // 유형 (MEETING, REQUEST, QA, EMAIL, PHONE)
    private String title;               // 제목
    private String content;             // 내용
    private String customerName;        // 고객 담당자명
    private String ourRepresentative;   // 우리측 담당자
    private LocalDateTime communicationDate; // 소통 일시
    private String status;              // 상태 (PENDING, IN_PROGRESS, COMPLETED)
    private String response;            // 답변/조치 내용
    private String priority;            // 우선순위 (HIGH, MEDIUM, LOW)
    private LocalDateTime dueDate;      // 처리 기한
    private LocalDateTime completedDate;// 완료일
    private String notes;               // 비고
}

