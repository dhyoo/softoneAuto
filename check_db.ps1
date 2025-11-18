# SQLite 데이터베이스 확인 스크립트

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SQLite 데이터베이스 확인 스크립트" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 기본 데이터 경로 확인
$defaultPath = "$env:USERPROFILE\SoftOneAutoData\data"
$dbFile = "$defaultPath\softone.db"

# config.json에서 경로 확인
if (Test-Path "config.json") {
    try {
        $config = Get-Content "config.json" | ConvertFrom-Json
        if ($config.dataPath) {
            $dbFile = "$($config.dataPath)\data\softone.db"
            Write-Host "설정 파일에서 경로 확인: $($config.dataPath)" -ForegroundColor Green
        }
    } catch {
        Write-Host "설정 파일 읽기 실패, 기본 경로 사용" -ForegroundColor Yellow
    }
}

Write-Host "데이터베이스 파일 위치:" -ForegroundColor Yellow
Write-Host $dbFile -ForegroundColor White
Write-Host ""

if (Test-Path $dbFile) {
    Write-Host "[OK] 데이터베이스 파일이 존재합니다." -ForegroundColor Green
    
    $fileInfo = Get-Item $dbFile
    Write-Host "파일 크기: $([math]::Round($fileInfo.Length / 1KB, 2)) KB" -ForegroundColor Cyan
    Write-Host "수정 시간: $($fileInfo.LastWriteTime)" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "데이터베이스 확인 방법:" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "1. SQLite 명령줄 도구 사용:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`"" -ForegroundColor White
    Write-Host ""
    
    Write-Host "2. DB Browser for SQLite 사용:" -ForegroundColor Yellow
    Write-Host "   - https://sqlitebrowser.org/ 에서 다운로드" -ForegroundColor White
    Write-Host "   - 파일 열기: $dbFile" -ForegroundColor White
    Write-Host ""
    
    Write-Host "3. 테이블 목록 확인:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`" `.tables" -ForegroundColor White
    Write-Host ""
    
    Write-Host "4. 회사 데이터 확인:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`" `"SELECT * FROM companies;`"" -ForegroundColor White
    Write-Host ""
    
    Write-Host "5. 개발자 데이터 확인:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`" `"SELECT * FROM developers;`"" -ForegroundColor White
    Write-Host ""
    
    Write-Host "6. 근태 데이터 확인:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`" `"SELECT * FROM attendances;`"" -ForegroundColor White
    Write-Host ""
    
    Write-Host "7. 회사별 개발자 수 확인:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`" `"SELECT c.name, COUNT(d.id) as dev_count FROM companies c LEFT JOIN developers d ON c.id = d.company_id GROUP BY c.id, c.name;`"" -ForegroundColor White
    Write-Host ""
    
    Write-Host "8. 회사별 근태 수 확인:" -ForegroundColor Yellow
    Write-Host "   sqlite3 `"$dbFile`" `"SELECT c.name, COUNT(a.id) as att_count FROM companies c LEFT JOIN attendances a ON c.id = a.company_id GROUP BY c.id, c.name;`"" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host "[경고] 데이터베이스 파일이 존재하지 않습니다." -ForegroundColor Red
    Write-Host "경로: $dbFile" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "애플리케이션을 실행하면 자동으로 생성됩니다." -ForegroundColor Cyan
}

Write-Host ""
Read-Host "Press Enter to exit"

