@echo off
echo ========================================
echo SQLite 데이터베이스 확인 스크립트
echo ========================================
echo.

REM 기본 데이터 경로 확인
set DEFAULT_PATH=%USERPROFILE%\SoftOneAutoData\data
set DB_FILE=%DEFAULT_PATH%\softone.db

echo 데이터베이스 파일 위치:
echo %DB_FILE%
echo.

if exist "%DB_FILE%" (
    echo [OK] 데이터베이스 파일이 존재합니다.
    echo.
    echo 파일 크기:
    dir "%DB_FILE%" | findstr "softone.db"
    echo.
    echo.
    echo ========================================
    echo 데이터베이스 확인 방법:
    echo ========================================
    echo.
    echo 1. SQLite 명령줄 도구 사용:
    echo    sqlite3 "%DB_FILE%"
    echo.
    echo 2. DB Browser for SQLite 사용:
    echo    - https://sqlitebrowser.org/ 에서 다운로드
    echo    - 파일 열기: %DB_FILE%
    echo.
    echo 3. 테이블 목록 확인:
    echo    sqlite3 "%DB_FILE%" ".tables"
    echo.
    echo 4. 회사 데이터 확인:
    echo    sqlite3 "%DB_FILE%" "SELECT * FROM companies;"
    echo.
    echo 5. 개발자 데이터 확인:
    echo    sqlite3 "%DB_FILE%" "SELECT * FROM developers;"
    echo.
    echo 6. 근태 데이터 확인:
    echo    sqlite3 "%DB_FILE%" "SELECT * FROM attendances;"
    echo.
) else (
    echo [경고] 데이터베이스 파일이 존재하지 않습니다.
    echo 경로: %DB_FILE%
    echo.
    echo 애플리케이션을 실행하면 자동으로 생성됩니다.
)

echo.
pause

