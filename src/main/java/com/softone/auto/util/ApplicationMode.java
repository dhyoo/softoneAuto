package com.softone.auto.util;

/**
 * 애플리케이션 실행 모드
 */
public enum ApplicationMode {
    /**
     * 개발 모드 - 샘플 데이터 자동 생성, 디버그 기능 활성화
     */
    DEVELOPMENT,
    
    /**
     * 프로덕션 모드 - 샘플 데이터 생성 안 함, 최적화된 설정
     */
    PRODUCTION,
    
    /**
     * 데모 모드 - 샘플 데이터 생성, 데모용 기능 활성화
     */
    DEMO
}

