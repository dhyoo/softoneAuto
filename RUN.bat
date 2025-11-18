@echo off
chcp 65001 > nul
echo ========================================
echo  SoftOne Auto Manager 실행
echo ========================================
echo.

REM 빌드 및 실행
if exist "build\libs\softoneAuto-1.0.0.jar" (
    echo [빠른 실행] 이미 빌드된 파일을 사용합니다...
    echo.
    java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar build\libs\softoneAuto-1.0.0.jar
) else (
    echo [빌드] JAR 파일 생성 중 (처음 실행 시에만 시간이 걸립니다)...
    call gradlew.bat jar --no-daemon
    
    if errorlevel 1 (
        echo.
        echo ❌ 빌드 실패
        pause
        exit /b 1
    )
    
    echo.
    echo [실행] 애플리케이션 실행 중...
    java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar build\libs\softoneAuto-1.0.0.jar
)

if errorlevel 1 (
    echo.
    echo ❌ 실행 실패
    pause
    exit /b 1
)

pause

