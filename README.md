# SoftOne Auto Manager

현장대리인을 위한 업무 자동화 데스크톱 애플리케이션

## 주요 기능

### 1. 개발자 관리
- 개발자 정보 등록/수정/삭제
- 개발자 목록 조회

### 2. 근태 관리
- 일별 출퇴근 기록
- 휴가/외출/조퇴 관리
- 월간 근태 현황 조회

### 3. 주간보고서 작성
- 전주 실적 입력
- 금주 계획 입력
- 이슈 사항 기록
- Excel/PDF 보고서 자동 생성

### 4. 고객 소통 관리
- 회의록 작성
- 고객 요청사항 관리
- 질의응답 기록

### 5. 이슈 관리
- 프로젝트 이슈 등록
- 이슈 상태 추적
- 해결 내역 관리

## 실행 방법

### 개발 환경에서 실행
```bash
./gradlew run
```

### JAR 파일 빌드
```bash
./gradlew clean jar
```

빌드된 JAR 파일은 `build/libs/softoneAuto-1.0.0.jar`에 생성됩니다.

### JAR 파일 실행
```bash
java -jar softoneAuto-1.0.0.jar
```

## 시스템 요구사항
- Java 17 이상
- 폐쇄망 환경에서 동작 (인터넷 연결 불필요)

## 데이터 저장
- 모든 데이터는 `data/` 폴더에 JSON 형식으로 저장
- 보고서는 `reports/` 폴더에 자동 생성
- Excel 템플릿은 `templates/` 폴더에서 관리

## 기술 스택
- Java 17
- Swing GUI
- Apache POI (Excel 처리)
- iText (PDF 생성)
- Gson (JSON 처리)

