@echo off
call gradlew.bat build
if errorlevel 1 (echo BUILD FAILED & pause & exit /b 1)
echo.
echo Gotowe: build\libs\giftclient-1.0.0.jar
pause
