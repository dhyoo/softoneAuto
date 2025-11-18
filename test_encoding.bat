@echo off
chcp 65001 > nul
echo ========================================
echo 인코딩 테스트
echo ========================================
echo.

echo [테스트] 한글 출력: 로그 확인 테스트
echo [테스트] 특수문자: !@#$%^&*()
echo.

java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp "build\classes\java\main;build\libs\*" -c "System.out.println(\"한글 테스트: 로그 확인\"); System.out.println(\"현재 인코딩: \" + System.getProperty(\"file.encoding\"));" 2>nul

if errorlevel 1 (
    echo.
    echo Java 실행 테스트:
    gradlew.bat -q run --args="--test-encoding" 2>nul || echo 애플리케이션 실행 필요
)

echo.
pause

