@echo off
chcp 65001 > nul
echo ========================================
echo 로그 실시간 모니터링
echo ========================================
echo.
echo 애플리케이션이 실행 중입니다.
echo 시스템 설정에서 파견회사를 변경하면 로그가 출력됩니다.
echo.
echo 종료하려면 Ctrl+C를 누르세요.
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; [Console]::InputEncoding = [System.Text.Encoding]::UTF8; $OutputEncoding = [System.Text.Encoding]::UTF8; $logFile = 'logs\softone.log'; $lastSize = 0; if (Test-Path $logFile) { $lastSize = (Get-Item $logFile).Length }; Write-Host '로그 모니터링 시작...' -ForegroundColor Green; Write-Host ''; while ($true) { Start-Sleep -Seconds 1; if (Test-Path $logFile) { $currentSize = (Get-Item $logFile).Length; if ($currentSize -gt $lastSize) { Write-Host \"=== 새로운 로그 ===\" -ForegroundColor Cyan; Get-Content $logFile -Encoding UTF8 -Tail 10 | ForEach-Object { Write-Host $_ }; Write-Host ''; $lastSize = $currentSize } } }"

