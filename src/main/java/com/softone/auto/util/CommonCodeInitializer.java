package com.softone.auto.util;

import com.softone.auto.service.CommonCodeService;

/**
 * 공통코드 초기 데이터 생성
 */
public class CommonCodeInitializer {
    
    /**
     * 기본 공통코드 초기화
     */
    public static void initializeDefaultCodes(CommonCodeService codeService) {
        System.out.println("=== 공통코드 초기 데이터 생성 ===");
        
        // 이미 데이터가 있으면 스킵
        if (!codeService.getAllCodes().isEmpty()) {
            System.out.println("공통코드 이미 존재. 초기화 스킵.");
            return;
        }
        
        // 직급 코드
        codeService.createCode("POSITION", "JUNIOR", "사원", "신입 개발자", 1);
        codeService.createCode("POSITION", "SENIOR", "선임", "2-5년차 개발자", 2);
        codeService.createCode("POSITION", "STAFF", "책임", "5-10년차 개발자", 3);
        codeService.createCode("POSITION", "CHIEF", "수석", "10년차 이상 개발자", 4);
        
        // 개발자 상태
        codeService.createCode("DEV_STATUS", "ACTIVE", "활성", "정상 근무중", 1);
        codeService.createCode("DEV_STATUS", "INACTIVE", "비활성", "휴직/퇴사", 2);
        codeService.createCode("DEV_STATUS", "VACATION", "휴가", "휴가중", 3);
        
        // 이슈 심각도
        codeService.createCode("SEVERITY", "CRITICAL", "긴급", "즉시 조치 필요", 1);
        codeService.createCode("SEVERITY", "HIGH", "높음", "빠른 조치 필요", 2);
        codeService.createCode("SEVERITY", "MEDIUM", "보통", "일반 처리", 3);
        codeService.createCode("SEVERITY", "LOW", "낮음", "참고 사항", 4);
        
        // 이슈 카테고리
        codeService.createCode("ISSUE_CATEGORY", "BUG", "버그", "소프트웨어 결함", 1);
        codeService.createCode("ISSUE_CATEGORY", "FEATURE", "기능", "신규 기능 요청", 2);
        codeService.createCode("ISSUE_CATEGORY", "IMPROVEMENT", "개선", "기능 개선", 3);
        codeService.createCode("ISSUE_CATEGORY", "PERFORMANCE", "성능", "성능 개선", 4);
        
        // 이슈 상태
        codeService.createCode("ISSUE_STATUS", "OPEN", "열림", "신규 등록", 1);
        codeService.createCode("ISSUE_STATUS", "IN_PROGRESS", "진행중", "처리중", 2);
        codeService.createCode("ISSUE_STATUS", "RESOLVED", "해결", "해결 완료", 3);
        codeService.createCode("ISSUE_STATUS", "CLOSED", "닫힘", "종료", 4);
        
        // 근태 상태
        codeService.createCode("ATTENDANCE_STATUS", "PRESENT", "출근", "정상 출근", 1);
        codeService.createCode("ATTENDANCE_STATUS", "LATE", "지각", "지각", 2);
        codeService.createCode("ATTENDANCE_STATUS", "ABSENT", "결근", "결근", 3);
        codeService.createCode("ATTENDANCE_STATUS", "VACATION", "휴가", "연차/휴가", 4);
        codeService.createCode("ATTENDANCE_STATUS", "SICK", "병가", "병가", 5);
        
        // 소통 유형
        codeService.createCode("COMM_TYPE", "PHONE", "전화", "전화 통화", 1);
        codeService.createCode("COMM_TYPE", "EMAIL", "이메일", "이메일", 2);
        codeService.createCode("COMM_TYPE", "MEETING", "회의", "대면 회의", 3);
        codeService.createCode("COMM_TYPE", "CHAT", "메신저", "메신저 대화", 4);
        
        // 소통 상태
        codeService.createCode("COMM_STATUS", "PENDING", "대기", "응답 대기중", 1);
        codeService.createCode("COMM_STATUS", "COMPLETED", "완료", "응답 완료", 2);
        
        System.out.println("=== 공통코드 초기화 완료 (31개) ===");
    }
}

