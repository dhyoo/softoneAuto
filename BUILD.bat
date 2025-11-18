@echo off
chcp 65001 > nul
echo ========================================
echo  SoftOne Auto Manager 빌드
echo ========================================
echo.

echo JAR 파일 생성 중...
call gradlew.bat clean jar

if errorlevel 1 (
    echo.
    echo ❌ 빌드 실패
    pause
    exit /b 1
)

echo.
echo ✅ 빌드 완료!
echo.
echo 생성된 파일: build\libs\softoneAuto-1.0.0.jar
echo.
echo 실행 방법:
echo   java -jar build\libs\softoneAuto-1.0.0.jar
echo.
echo 또는 RUN.bat을 실행하세요.
echo.

pause

