package com.softone.auto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공통코드 모델
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonCode {
    private String id;              // 코드 ID
    private String category;        // 카테고리 (예: POSITION, STATUS, SEVERITY)
    private String code;            // 코드 값
    private String name;            // 코드 명
    private String description;     // 설명
    private Integer sortOrder;      // 정렬 순서
    private Boolean isActive;       // 사용 여부
}

