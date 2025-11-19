@echo off
chcp 65001 > nul
echo ========================================
echo SoftOne Auto Manager EXE 빌드 스크립트
echo ========================================
echo.

REM JDK 버전 확인
echo [1/4] JDK 버전 확인 중...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [오류] Java가 설치되어 있지 않습니다.
    echo JDK 14 이상을 설치해주세요.
    pause
    exit /b 1
)

jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [오류] jpackage를 찾을 수 없습니다.
    echo JDK 14 이상이 필요합니다.
    pause
    exit /b 1
)

echo [완료] JDK 버전 확인 완료
echo.

REM JAR 파일 빌드
echo [2/4] JAR 파일 빌드 중...
call gradlew.bat clean build --no-daemon
if %errorlevel% neq 0 (
    echo [오류] JAR 파일 빌드 실패
    pause
    exit /b 1
)
echo [완료] JAR 파일 빌드 완료
echo.

REM EXE 파일 생성
echo [3/4] EXE 파일 생성 중...
echo 이 작업은 몇 분 정도 소요될 수 있습니다...
call gradlew.bat createExe --no-daemon
if %errorlevel% neq 0 (
    echo [오류] EXE 파일 생성 실패
    pause
    exit /b 1
)
echo [완료] EXE 파일 생성 완료
echo.

REM 결과 확인
echo [4/4] 빌드 결과 확인 중...
if exist "build\distributions\exe\SoftOneAutoManager\SoftOneAutoManager.exe" (
    echo.
    echo ========================================
    echo 빌드 성공!
    echo ========================================
    echo.
    echo EXE 파일 위치:
    echo   build\distributions\exe\SoftOneAutoManager\SoftOneAutoManager.exe
    echo.
    echo 배포 시 주의사항:
    echo   1. 샘플 데이터는 자동으로 생성되지 않습니다 (PRODUCTION 모드)
    echo   2. 사용자 데이터는 %%USERPROFILE%%\SoftOneAutoData\ 에 저장됩니다
    echo   3. 첫 실행 시 데이터 저장 위치를 선택하도록 안내됩니다
    echo.
) else (
    echo [오류] EXE 파일을 찾을 수 없습니다.
    pause
    exit /b 1
)

pause

