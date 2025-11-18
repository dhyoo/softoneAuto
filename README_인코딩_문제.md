# 한글 인코딩 문제 해결 가이드

## 문제
터미널에서 한글이 깨져 보이는 현상

## 해결 방법

### 1. 애플리케이션 실행 시
- `gradlew.bat run` 또는 `RUN.bat` 사용
- 자동으로 UTF-8 인코딩이 설정됩니다

### 2. 로그 파일 확인 시
- `view_logs.bat` 실행 (UTF-8로 자동 설정)
- 또는 PowerShell에서:
  ```powershell
  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
  Get-Content logs\softone.log -Encoding UTF8
  ```

### 3. 수동 설정 (PowerShell)
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

## 적용된 수정 사항

1. **SoftoneApplication.java**: System.out/err을 UTF-8로 강제 설정
2. **build.gradle**: 모든 JavaExec 태스크에 UTF-8 인코딩 옵션 추가
3. **gradlew.bat**: chcp 65001 및 JVM 인코딩 옵션 추가
4. **view_logs.bat**: PowerShell 인코딩 자동 설정

## 참고
- 로그 파일은 UTF-8로 저장됩니다
- 터미널 출력도 UTF-8로 설정됩니다
- Windows 콘솔의 기본 인코딩(CP949)과 충돌할 수 있으므로 위 설정이 필요합니다

