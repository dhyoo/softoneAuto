# SQLite 마이그레이션 스키마 설계서

## 1. JSON 데이터 구조 분석

### 1.1 Company (파견회사)
```json
{
  "id": "company-001",
  "name": "LG전자",
  "projectName": "스마트홈 플랫폼 구축",
  "contractType": "파견",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "status": "ACTIVE",
  "notes": "비고 내용"
}
```

### 1.2 Developer (개발자)
```json
{
  "id": "dev-001",
  "companyId": "company-001",
  "name": "김철수",
  "position": "선임",
  "role": "Backend",
  "team": "개발팀",
  "email": "kim@example.com",
  "phone": "010-1234-5678",
  "emergencyPhone": "010-9876-5432",
  "joinDate": "2024-01-15",
  "status": "ACTIVE",
  "notes": "비고"
}
```

### 1.3 Attendance (근태)
```json
{
  "id": "att-001",
  "companyId": "company-001",
  "developerId": "dev-001",
  "developerName": "김철수",
  "date": "2024-11-18",
  "checkIn": "09:00",
  "checkOut": "18:00",
  "type": "NORMAL",
  "notes": "정상 출근",
  "workMinutes": 540
}
```

### 1.4 WeeklyReport (주간보고서)
```json
{
  "id": "report-001",
  "companyId": "company-001",
  "title": "2024년 11월 3주차 보고서",
  "startDate": "2024-11-11",
  "endDate": "2024-11-15",
  "projectName": "스마트홈 플랫폼",
  "reporter": "홍길동",
  "lastWeekWork": [
    {
      "task": "API 개발",
      "assignee": "김철수",
      "status": "완료",
      "progress": 100,
      "notes": "완료"
    }
  ],
  "thisWeekPlan": [
    {
      "task": "테스트",
      "assignee": "김철수",
      "status": "진행중",
      "progress": 50,
      "notes": "진행중"
    }
  ],
  "issues": [
    {
      "issue": "성능 이슈",
      "severity": "높음",
      "status": "미해결",
      "action": "조치 중"
    }
  ],
  "attendanceSummaries": [
    {
      "developerName": "김철수",
      "workDays": 5,
      "lateDays": 0,
      "vacationDays": 0,
      "notes": "정상"
    }
  ],
  "additionalNotes": "추가 특이사항",
  "createdDate": "2024-11-15",
  "checkItems": [true, true, false, true, true, true, true, true, true, true],
  "thisWeekRequestCount": 10,
  "thisWeekCompleteCount": 8,
  "nextWeekRequestCount": 12,
  "nextWeekCompleteCount": 0,
  "thisWeekTasksText": "금주 업무 내용",
  "nextWeekTasksText": "차주 계획 내용"
}
```

### 1.5 Issue (이슈)
```json
{
  "id": "issue-001",
  "companyId": "company-001",
  "title": "성능 이슈",
  "description": "API 응답 시간 지연",
  "category": "기술",
  "severity": "높음",
  "status": "OPEN",
  "reporter": "홍길동",
  "assignee": "김철수",
  "createdDate": "2024-11-18T10:00:00",
  "updatedDate": "2024-11-18T15:00:00",
  "resolvedDate": null,
  "resolution": "해결 방안",
  "notes": "이슈 내용"
}
```

### 1.6 CustomerCommunication (고객소통)
```json
{
  "id": "comm-001",
  "companyId": "company-001",
  "type": "MEETING",
  "title": "주간 회의",
  "content": "회의 내용",
  "customerName": "이영희",
  "ourRepresentative": "홍길동",
  "communicationDate": "2024-11-18T14:30:00",
  "status": "PENDING",
  "priority": "HIGH",
  "dueDate": "2024-11-20T18:00:00",
  "completedDate": null,
  "response": "응답 내용",
  "notes": "비고"
}
```

### 1.7 CommonCode (공통코드)
```json
{
  "id": "code-001",
  "category": "ATTENDANCE_TYPE",
  "code": "NORMAL",
  "name": "정상",
  "description": "정상 출근",
  "sortOrder": 1,
  "isActive": true
}
```

---

## 2. SQLite 테이블 스키마 설계

### 2.1 companies (파견회사)
```sql
CREATE TABLE companies (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    project_name TEXT,
    contract_type TEXT,
    start_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DD'
    end_date TEXT,    -- ISO 8601 형식: 'YYYY-MM-DD'
    status TEXT NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE, COMPLETED
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime'))
);

-- 인덱스
CREATE INDEX idx_companies_status ON companies(status);
CREATE INDEX idx_companies_name ON companies(name);
```

**설명:**
- `id`: UUID 문자열 (Primary Key)
- 날짜 필드는 TEXT로 저장 (ISO 8601 형식: 'YYYY-MM-DD')
- `status`에 인덱스 생성 (필터링 성능 향상)
- `name`에 인덱스 생성 (검색 성능 향상)

---

### 2.2 developers (개발자)
```sql
CREATE TABLE developers (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    name TEXT NOT NULL,
    position TEXT,
    role TEXT,
    team TEXT,
    email TEXT,
    phone TEXT,
    emergency_phone TEXT,
    join_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DD'
    status TEXT NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE, VACATION
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_developers_company_id ON developers(company_id);
CREATE INDEX idx_developers_status ON developers(status);
CREATE INDEX idx_developers_name ON developers(name);
CREATE INDEX idx_developers_company_status ON developers(company_id, status);
```

**설명:**
- `company_id`는 Foreign Key로 `companies.id` 참조
- `ON DELETE CASCADE`: 회사 삭제 시 개발자도 자동 삭제
- 복합 인덱스 `(company_id, status)`: 회사별 활성 개발자 조회 최적화

---

### 2.3 attendances (근태)
```sql
CREATE TABLE attendances (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    developer_id TEXT NOT NULL,
    developer_name TEXT NOT NULL,  -- 정규화 위반이지만 조회 성능을 위해 유지
    date TEXT NOT NULL,  -- ISO 8601 형식: 'YYYY-MM-DD'
    check_in TEXT,  -- ISO 8601 형식: 'HH:mm:ss'
    check_out TEXT,  -- ISO 8601 형식: 'HH:mm:ss'
    type TEXT NOT NULL DEFAULT 'NORMAL',  -- NORMAL, LATE, EARLY_LEAVE, ABSENT, VACATION, SICK_LEAVE
    notes TEXT,
    work_minutes INTEGER,  -- 근무 시간 (분)
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (developer_id) REFERENCES developers(id) ON DELETE CASCADE,
    UNIQUE(developer_id, date)  -- 개발자별 일자 중복 방지
);

-- 인덱스
CREATE INDEX idx_attendances_company_id ON attendances(company_id);
CREATE INDEX idx_attendances_developer_id ON attendances(developer_id);
CREATE INDEX idx_attendances_date ON attendances(date);
CREATE INDEX idx_attendances_type ON attendances(type);
CREATE INDEX idx_attendances_company_date ON attendances(company_id, date);
CREATE INDEX idx_attendances_developer_date ON attendances(developer_id, date);
```

**설명:**
- `developer_name`은 정규화 위반이지만 조회 성능을 위해 포함 (denormalization)
- `UNIQUE(developer_id, date)`: 같은 개발자의 같은 날짜 중복 방지
- 날짜 범위 조회를 위한 인덱스 최적화

---

### 2.4 weekly_reports (주간보고서)
```sql
CREATE TABLE weekly_reports (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    title TEXT NOT NULL,
    start_date TEXT NOT NULL,  -- ISO 8601 형식: 'YYYY-MM-DD'
    end_date TEXT NOT NULL,    -- ISO 8601 형식: 'YYYY-MM-DD'
    project_name TEXT,
    reporter TEXT,
    additional_notes TEXT,
    created_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DD'
    this_week_request_count INTEGER DEFAULT 0,
    this_week_complete_count INTEGER DEFAULT 0,
    next_week_request_count INTEGER DEFAULT 0,
    next_week_complete_count INTEGER DEFAULT 0,
    this_week_tasks_text TEXT,
    next_week_tasks_text TEXT,
    check_items TEXT,  -- JSON 배열 문자열: '[true,true,false,...]'
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    UNIQUE(company_id, start_date)  -- 회사별 주간 시작일 중복 방지
);

-- 인덱스
CREATE INDEX idx_weekly_reports_company_id ON weekly_reports(company_id);
CREATE INDEX idx_weekly_reports_start_date ON weekly_reports(start_date);
CREATE INDEX idx_weekly_reports_company_start ON weekly_reports(company_id, start_date);
```

**설명:**
- `check_items`는 JSON 배열을 TEXT로 저장 (SQLite JSON1 확장 사용 가능)
- `UNIQUE(company_id, start_date)`: 같은 회사의 같은 주간 보고서 중복 방지

---

### 2.5 weekly_report_work_items (주간보고서 업무 항목)
```sql
CREATE TABLE weekly_report_work_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    item_type TEXT NOT NULL,  -- 'LAST_WEEK' 또는 'THIS_WEEK'
    task TEXT NOT NULL,
    assignee TEXT,
    status TEXT,
    progress INTEGER,  -- 0-100
    notes TEXT,
    display_order INTEGER DEFAULT 0,
    FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_work_items_report_id ON weekly_report_work_items(report_id);
CREATE INDEX idx_work_items_report_type ON weekly_report_work_items(report_id, item_type);
```

**설명:**
- `item_type`으로 전주 실적/금주 계획 구분
- `display_order`로 정렬 순서 관리

---

### 2.6 weekly_report_issues (주간보고서 이슈 항목)
```sql
CREATE TABLE weekly_report_issues (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    issue TEXT NOT NULL,
    severity TEXT,  -- 높음, 보통, 낮음
    status TEXT,    -- 미해결, 진행중, 해결
    action TEXT,
    display_order INTEGER DEFAULT 0,
    FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_report_issues_report_id ON weekly_report_issues(report_id);
```

---

### 2.7 weekly_report_attendance_summaries (주간보고서 근태 요약)
```sql
CREATE TABLE weekly_report_attendance_summaries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    developer_name TEXT NOT NULL,
    work_days INTEGER DEFAULT 0,
    late_days INTEGER DEFAULT 0,
    vacation_days INTEGER DEFAULT 0,
    notes TEXT,
    display_order INTEGER DEFAULT 0,
    FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_attendance_summaries_report_id ON weekly_report_attendance_summaries(report_id);
```

---

### 2.8 issues (이슈)
```sql
CREATE TABLE issues (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    severity TEXT NOT NULL,  -- HIGH, MEDIUM, LOW
    status TEXT NOT NULL DEFAULT 'OPEN',  -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    assignee TEXT,
    reported_date TEXT NOT NULL,  -- ISO 8601 형식: 'YYYY-MM-DD'
    resolved_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DD'
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_issues_company_id ON issues(company_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_severity ON issues(severity);
CREATE INDEX idx_issues_assignee ON issues(assignee);
CREATE INDEX idx_issues_company_status ON issues(company_id, status);
CREATE INDEX idx_issues_reported_date ON issues(reported_date);
```

---

### 2.9 customer_communications (고객소통)
```sql
CREATE TABLE customer_communications (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    type TEXT NOT NULL,  -- MEETING, REQUEST, QA, EMAIL, PHONE
    title TEXT NOT NULL,
    content TEXT,
    customer_name TEXT,
    our_representative TEXT,
    communication_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    status TEXT NOT NULL DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, COMPLETED
    priority TEXT,  -- HIGH, MEDIUM, LOW
    due_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    response TEXT,
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- 인덱스
CREATE INDEX idx_communications_company_id ON customer_communications(company_id);
CREATE INDEX idx_communications_status ON customer_communications(status);
CREATE INDEX idx_communications_type ON customer_communications(type);
CREATE INDEX idx_communications_priority ON customer_communications(priority);
CREATE INDEX idx_communications_company_status ON customer_communications(company_id, status);
CREATE INDEX idx_communications_due_date ON customer_communications(due_date);
```

---

### 2.10 common_codes (공통코드)
```sql
CREATE TABLE common_codes (
    id TEXT PRIMARY KEY,
    category TEXT NOT NULL,  -- ATTENDANCE_TYPE, ISSUE_SEVERITY 등
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    active INTEGER DEFAULT 1,  -- 1: 활성, 0: 비활성 (BOOLEAN 대신 INTEGER)
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    UNIQUE(category, code)  -- 카테고리별 코드 중복 방지
);

-- 인덱스
CREATE INDEX idx_common_codes_category ON common_codes(category);
CREATE INDEX idx_common_codes_is_active ON common_codes(is_active);
CREATE INDEX idx_common_codes_category_active ON common_codes(category, is_active);
```

---

## 3. 전체 스키마 생성 스크립트

```sql
-- ============================================
-- SQLite 데이터베이스 스키마 생성 스크립트
-- ============================================

-- 트랜잭션 시작
BEGIN TRANSACTION;

-- 1. 파견회사
CREATE TABLE companies (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    project_name TEXT,
    contract_type TEXT,
    start_date TEXT,
    end_date TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX idx_companies_status ON companies(status);
CREATE INDEX idx_companies_name ON companies(name);

-- 2. 개발자
CREATE TABLE developers (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    name TEXT NOT NULL,
    position TEXT,
    role TEXT,
    team TEXT,
    email TEXT,
    phone TEXT,
    emergency_phone TEXT,
    join_date TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX idx_developers_company_id ON developers(company_id);
CREATE INDEX idx_developers_status ON developers(status);
CREATE INDEX idx_developers_name ON developers(name);
CREATE INDEX idx_developers_company_status ON developers(company_id, status);

-- 3. 근태
CREATE TABLE attendances (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    developer_id TEXT NOT NULL,
    developer_name TEXT NOT NULL,
    date TEXT NOT NULL,
    check_in TEXT,
    check_out TEXT,
    type TEXT NOT NULL DEFAULT 'NORMAL',
    notes TEXT,
    work_minutes INTEGER,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (developer_id) REFERENCES developers(id) ON DELETE CASCADE,
    UNIQUE(developer_id, date)
);

CREATE INDEX idx_attendances_company_id ON attendances(company_id);
CREATE INDEX idx_attendances_developer_id ON attendances(developer_id);
CREATE INDEX idx_attendances_date ON attendances(date);
CREATE INDEX idx_attendances_type ON attendances(type);
CREATE INDEX idx_attendances_company_date ON attendances(company_id, date);
CREATE INDEX idx_attendances_developer_date ON attendances(developer_id, date);

-- 4. 주간보고서
CREATE TABLE weekly_reports (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    title TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    project_name TEXT,
    reporter TEXT,
    additional_notes TEXT,
    created_date TEXT,
    this_week_request_count INTEGER DEFAULT 0,
    this_week_complete_count INTEGER DEFAULT 0,
    next_week_request_count INTEGER DEFAULT 0,
    next_week_complete_count INTEGER DEFAULT 0,
    this_week_tasks_text TEXT,
    next_week_tasks_text TEXT,
    check_items TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    UNIQUE(company_id, start_date)
);

CREATE INDEX idx_weekly_reports_company_id ON weekly_reports(company_id);
CREATE INDEX idx_weekly_reports_start_date ON weekly_reports(start_date);
CREATE INDEX idx_weekly_reports_company_start ON weekly_reports(company_id, start_date);

-- 5. 주간보고서 업무 항목
CREATE TABLE weekly_report_work_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    item_type TEXT NOT NULL,
    task TEXT NOT NULL,
    assignee TEXT,
    status TEXT,
    progress INTEGER,
    notes TEXT,
    display_order INTEGER DEFAULT 0,
    FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_items_report_id ON weekly_report_work_items(report_id);
CREATE INDEX idx_work_items_report_type ON weekly_report_work_items(report_id, item_type);

-- 6. 주간보고서 이슈 항목
CREATE TABLE weekly_report_issues (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    issue TEXT NOT NULL,
    severity TEXT,
    status TEXT,
    action TEXT,
    display_order INTEGER DEFAULT 0,
    FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

CREATE INDEX idx_report_issues_report_id ON weekly_report_issues(report_id);

-- 7. 주간보고서 근태 요약
CREATE TABLE weekly_report_attendance_summaries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    developer_name TEXT NOT NULL,
    work_days INTEGER DEFAULT 0,
    late_days INTEGER DEFAULT 0,
    vacation_days INTEGER DEFAULT 0,
    notes TEXT,
    display_order INTEGER DEFAULT 0,
    FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

CREATE INDEX idx_attendance_summaries_report_id ON weekly_report_attendance_summaries(report_id);

-- 8. 이슈
CREATE TABLE issues (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT,
    severity TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'OPEN',
    reporter TEXT,
    assignee TEXT,
    created_date TEXT NOT NULL,  -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    updated_date TEXT,           -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    resolved_date TEXT,          -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    resolution TEXT,
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX idx_issues_company_id ON issues(company_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_severity ON issues(severity);
CREATE INDEX idx_issues_category ON issues(category);
CREATE INDEX idx_issues_assignee ON issues(assignee);
CREATE INDEX idx_issues_reporter ON issues(reporter);
CREATE INDEX idx_issues_company_status ON issues(company_id, status);
CREATE INDEX idx_issues_created_date ON issues(created_date);

-- 9. 고객소통
CREATE TABLE customer_communications (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    content TEXT,
    customer_name TEXT,
    our_representative TEXT,
    communication_date TEXT,  -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    status TEXT NOT NULL DEFAULT 'PENDING',
    priority TEXT,
    due_date TEXT,            -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    completed_date TEXT,      -- ISO 8601 형식: 'YYYY-MM-DDTHH:mm:ss'
    response TEXT,
    notes TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX idx_communications_company_id ON customer_communications(company_id);
CREATE INDEX idx_communications_status ON customer_communications(status);
CREATE INDEX idx_communications_type ON customer_communications(type);
CREATE INDEX idx_communications_priority ON customer_communications(priority);
CREATE INDEX idx_communications_company_status ON customer_communications(company_id, status);
CREATE INDEX idx_communications_due_date ON customer_communications(due_date);

-- 10. 공통코드
CREATE TABLE common_codes (
    id TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    code TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active INTEGER DEFAULT 1,  -- 1: 활성, 0: 비활성 (BOOLEAN 대신 INTEGER)
    created_at TEXT DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT DEFAULT (datetime('now', 'localtime')),
    UNIQUE(category, code)
);

CREATE INDEX idx_common_codes_category ON common_codes(category);
CREATE INDEX idx_common_codes_is_active ON common_codes(is_active);
CREATE INDEX idx_common_codes_category_active ON common_codes(category, is_active);

-- 트랜잭션 커밋
COMMIT;
```

---

## 4. 마이그레이션 시 주의사항

### 4.1 날짜/시간 타입 변환

**문제점:**
- JSON: `LocalDate`, `LocalTime`, `LocalDateTime` 객체
- SQLite: TEXT 타입으로 ISO 8601 문자열 저장

**해결 방안:**
```java
// 저장 시
String dateStr = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);  // "2024-11-18"
String timeStr = localTime.format(DateTimeFormatter.ISO_LOCAL_TIME);  // "09:00:00"
String datetimeStr = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);  // "2024-11-18T14:30:00"

// 조회 시
LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_TIME);
LocalDateTime datetime = LocalDateTime.parse(datetimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
```

**주의:**
- NULL 값 처리: `null` 체크 필수
- 형식 일관성: ISO 8601 형식 유지
- 시간대: 로컬 시간 사용 (UTC 변환 불필요)

---

### 4.2 Boolean 타입 변환

**문제점:**
- SQLite는 BOOLEAN 타입이 없음
- INTEGER (0/1) 또는 TEXT ('true'/'false') 사용

**해결 방안:**
```sql
-- INTEGER 사용 (권장)
active INTEGER DEFAULT 1  -- 1: true, 0: false

-- Java 변환
int activeInt = active ? 1 : 0;
boolean active = activeInt == 1;
```

---

### 4.3 배열/리스트 타입 변환

**문제점:**
- JSON: `List<Boolean>`, `List<WorkItem>` 등
- SQLite: 별도 테이블로 정규화 또는 JSON 문자열 저장

**해결 방안:**

**방법 1: 정규화 (권장)**
```sql
-- weekly_report_work_items 테이블로 분리
-- weekly_report_issues 테이블로 분리
```

**방법 2: JSON 문자열 저장**
```sql
-- check_items TEXT  -- '[true,true,false,...]'
-- SQLite JSON1 확장 사용
SELECT json_extract(check_items, '$[0]') FROM weekly_reports;
```

---

### 4.4 중첩 객체 변환

**문제점:**
- `WeeklyReport.WorkItem`, `WeeklyReport.IssueItem` 등 중첩 객체

**해결 방안:**
- 별도 테이블로 분리 (정규화)
- `report_id`로 Foreign Key 연결
- `display_order`로 순서 관리

---

### 4.5 UUID/ID 변환

**문제점:**
- JSON: UUID 문자열
- SQLite: TEXT PRIMARY KEY

**해결 방안:**
- UUID 문자열 그대로 저장
- 인덱스 자동 생성 (PRIMARY KEY)

---

## 5. 성능 최적화 인덱스 전략

### 5.1 필수 인덱스 (이미 포함)

1. **Primary Key 인덱스**: 자동 생성
2. **Foreign Key 인덱스**: 조인 성능 향상
   - `developers.company_id`
   - `attendances.company_id`, `attendances.developer_id`
   - `weekly_reports.company_id`
   - `issues.company_id`
   - `customer_communications.company_id`

3. **필터링 인덱스**: WHERE 절 성능 향상
   - `companies.status`
   - `developers.status`
   - `attendances.type`, `attendances.date`
   - `issues.status`, `issues.severity`
   - `customer_communications.status`, `customer_communications.type`

### 5.2 복합 인덱스 (이미 포함)

**회사별 필터링 + 상태 필터링:**
```sql
CREATE INDEX idx_developers_company_status ON developers(company_id, status);
CREATE INDEX idx_issues_company_status ON issues(company_id, status);
CREATE INDEX idx_communications_company_status ON customer_communications(company_id, status);
```

**날짜 범위 조회 최적화:**
```sql
CREATE INDEX idx_attendances_company_date ON attendances(company_id, date);
CREATE INDEX idx_attendances_developer_date ON attendances(developer_id, date);
CREATE INDEX idx_weekly_reports_company_start ON weekly_reports(company_id, start_date);
```

### 5.3 추가 최적화 인덱스 (선택적)

**대용량 데이터 조회 시:**
```sql
-- 근태 통계 조회 최적화
CREATE INDEX idx_attendances_company_type_date ON attendances(company_id, type, date);

-- 이슈 기간별 조회 최적화
CREATE INDEX idx_issues_company_reported_date ON issues(company_id, reported_date);

-- 고객소통 기한별 조회 최적화
CREATE INDEX idx_communications_company_due_date ON customer_communications(company_id, due_date);
```

### 5.4 인덱스 사용 가이드

**인덱스가 효과적인 경우:**
- WHERE 절에서 자주 사용되는 컬럼
- JOIN에 사용되는 Foreign Key
- ORDER BY에 사용되는 컬럼
- 복합 조건 (AND) 조회

**인덱스가 비효율적인 경우:**
- 자주 UPDATE되는 컬럼 (인덱스 유지 비용 증가)
- 카디널리티가 낮은 컬럼 (예: 성별, 상태 값이 2-3개)
- LIKE '%pattern%' 패턴 (앞부분 와일드카드)

---

## 6. 데이터 무결성 제약조건

### 6.1 Primary Key
- 모든 테이블에 UUID 문자열 사용
- 자동 인덱스 생성

### 6.2 Foreign Key
- `ON DELETE CASCADE`: 부모 삭제 시 자식 자동 삭제
- 참조 무결성 보장

### 6.3 UNIQUE 제약조건
- `attendances(developer_id, date)`: 개발자별 일자 중복 방지
- `weekly_reports(company_id, start_date)`: 회사별 주간 보고서 중복 방지
- `common_codes(category, code)`: 카테고리별 코드 중복 방지

### 6.4 NOT NULL 제약조건
- 필수 필드에 적용
- 데이터 무결성 보장

### 6.5 DEFAULT 값
- `status` 필드: 기본값 설정
- `created_at`, `updated_at`: 자동 타임스탬프

---

## 7. 마이그레이션 체크리스트

### 7.1 사전 준비
- [ ] 기존 JSON 파일 백업
- [ ] SQLite JDBC 드라이버 추가 (`org.xerial:sqlite-jdbc`)
- [ ] 스키마 생성 스크립트 검증

### 7.2 데이터 변환
- [ ] JSON → SQLite 변환 스크립트 작성
- [ ] 날짜/시간 형식 변환 검증
- [ ] Boolean → INTEGER 변환 검증
- [ ] 배열/리스트 → 별도 테이블 변환 검증
- [ ] NULL 값 처리 검증

### 7.3 데이터 검증
- [ ] 레코드 수 비교 (JSON vs SQLite)
- [ ] 무결성 검증 (Foreign Key, UNIQUE)
- [ ] 샘플 데이터 조회 테스트
- [ ] 성능 테스트 (인덱스 효과 확인)

### 7.4 롤백 계획
- [ ] SQLite → JSON 역변환 스크립트 준비
- [ ] 문제 발생 시 즉시 롤백 가능 여부 확인

---

## 8. 예상 성능 개선 효과

### 8.1 조회 성능
- **기존 JSON**: 전체 파일 로드 후 메모리에서 필터링
- **SQLite**: 인덱스 기반 직접 조회
- **예상 개선**: 10-100배 향상 (데이터 크기에 따라)

### 8.2 저장 성능
- **기존 JSON**: 전체 파일 덮어쓰기
- **SQLite**: 트랜잭션 기반 증분 업데이트
- **예상 개선**: 5-20배 향상

### 8.3 손상 복구
- **기존 JSON**: 부분 복구 시도 (제한적)
- **SQLite**: `PRAGMA integrity_check` 자동 복구
- **예상 개선**: 근본적 해결

---

## 9. 다음 단계

1. **SQLite JDBC 드라이버 추가**
   ```gradle
   dependencies {
       implementation 'org.xerial:sqlite-jdbc:3.44.1.0'
   }
   ```

2. **SqliteRepository 클래스 구현**
   - `JsonRepository`와 동일한 인터페이스
   - JDBC를 사용한 CRUD 구현

3. **마이그레이션 스크립트 작성**
   - JSON 파일 읽기
   - SQLite INSERT 실행
   - 검증 및 롤백

4. **점진적 전환**
   - 하이브리드 모드 (JSON + SQLite 동시 지원)
   - 사용자 확인 후 JSON 제거

