package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 근태 정보 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    private String id;                  // 근태 ID
    private String companyId;           // 파견 회사 ID
    private String developerId;         // 개발자 ID
    private String developerName;       // 개발자 이름
    private LocalDate date;             // 날짜
    private LocalTime checkIn;          // 출근 시간
    private LocalTime checkOut;         // 퇴근 시간
    private String type;                // 근태 유형 (NORMAL, LATE, EARLY_LEAVE, ABSENT, VACATION, SICK_LEAVE)
    private String notes;               // 비고
    private Integer workMinutes;        // 근무 시간 (분)
}

