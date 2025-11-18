@echo off
chcp 65001 > nul
echo ========================================
echo 로그 파일 확인 (UTF-8)
echo ========================================
echo.

REM PowerShell 인코딩 설정
powershell -NoProfile -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; [Console]::InputEncoding = [System.Text.Encoding]::UTF8; $OutputEncoding = [System.Text.Encoding]::UTF8"

if exist "logs\softone.log" (
    echo [최근 로그] softone.log (마지막 50줄)
    echo.
    powershell -NoProfile -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; Get-Content 'logs\softone.log' -Tail 50 -Encoding UTF8 | ForEach-Object { Write-Host $_ }"
    echo.
) else (
    echo 로그 파일이 없습니다.
)

if exist "logs\system.out.log" (
    echo.
    echo ========================================
    echo [System.out 로그] system.out.log (마지막 50줄)
    echo ========================================
    echo.
    powershell -NoProfile -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; Get-Content 'logs\system.out.log' -Tail 50 -Encoding UTF8 | ForEach-Object { Write-Host $_ }"
) else (
    echo.
    echo System.out 로그 파일이 없습니다.
)

echo.
pause

