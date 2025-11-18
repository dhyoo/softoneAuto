@echo off
chcp 65001 > nul
echo ========================================
echo  SoftOne Auto Manager 빠른 실행
echo ========================================
echo.
echo [빠른 실행] 변경된 파일만 컴파일하고 실행합니다...
echo.

call gradlew.bat quickRun --no-daemon

if errorlevel 1 (
    echo.
    echo ❌ 실행 실패
    pause
    exit /b 1
)

pause

