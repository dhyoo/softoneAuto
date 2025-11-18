package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 개발자 정보 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Developer {
    private String id;              // 개발자 ID
    private String companyId;       // 파견 회사 ID
    private String name;            // 이름
    private String position;        // 직급 (예: 선임, 책임, 수석)
    private String role;            // 역할 (예: Backend, Frontend, Full-stack)
    private String team;            // 소속 팀
    private String email;           // 이메일
    private String phone;           // 연락처
    private String emergencyPhone;  // 비상연락처
    private LocalDate joinDate;     // 투입일
    private String status;          // 상태 (ACTIVE, INACTIVE, VACATION)
    private String notes;           // 비고
}

