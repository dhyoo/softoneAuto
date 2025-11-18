package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 파견 회사(고객사) 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    private String id;                  // 회사 ID
    private String name;                // 회사명 (예: LG전자, SK텔레콤)
    private String projectName;         // 프로젝트명
    private String contractType;        // 계약 형태 (파견, 용역 등)
    private LocalDate startDate;        // 계약 시작일
    private LocalDate endDate;          // 계약 종료일
    private String status;              // 상태 (ACTIVE, INACTIVE, COMPLETED)
    private String notes;               // 비고
    
    public Company(String id, String name, String projectName) {
        this.id = id;
        this.name = name;
        this.projectName = projectName;
        this.status = "ACTIVE";
    }
}

