# 로그 모니터링 스크립트
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "로그 모니터링 시작" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$logFile = "logs\softone.log"
$lastSize = 0

if (Test-Path $logFile) {
    $lastSize = (Get-Item $logFile).Length
    Write-Host "[초기] 로그 파일 크기: $lastSize bytes" -ForegroundColor Green
} else {
    Write-Host "[경고] 로그 파일이 없습니다: $logFile" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "새로운 로그를 모니터링합니다..." -ForegroundColor Yellow
Write-Host "애플리케이션에서 파견회사를 변경하면 로그가 출력됩니다." -ForegroundColor Yellow
Write-Host ""

$checkCount = 0
while ($true) {
    Start-Sleep -Seconds 2
    $checkCount++
    
    if (Test-Path $logFile) {
        $currentSize = (Get-Item $logFile).Length
        
        if ($currentSize -gt $lastSize) {
            Write-Host "=== 새로운 로그 발견 ($checkCount번째 체크) ===" -ForegroundColor Green
            Write-Host ""
            
            # 새로 추가된 부분만 읽기
            $newContent = Get-Content $logFile -Encoding UTF8 -Tail 20
            $newContent | ForEach-Object { Write-Host $_ }
            
            Write-Host ""
            $lastSize = $currentSize
        }
    }
    
    # 30초마다 상태 출력
    if ($checkCount % 15 -eq 0) {
        Write-Host "[모니터링 중...] $checkCount번째 체크" -ForegroundColor Gray
    }
}

