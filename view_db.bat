@echo off
echo ========================================
echo SQLite 데이터베이스 확인
echo ========================================
echo.

cd /d %~dp0
gradlew.bat run --args="--view-db" 2>nul

if errorlevel 1 (
    echo.
    echo Java로 데이터베이스 확인 중...
    gradlew.bat -q execute -PmainClass=com.softone.auto.util.DatabaseViewer
)

echo.
pause

