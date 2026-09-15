@echo off
chcp 65001 >nul 2>&1
title CyreneAdmin E2E Tests
cd /d "%~dp0\.."

echo ============================================
echo   CyreneAdmin E2E Test Runner
echo ============================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-tests.ps1" %*

if %ERRORLEVEL% neq 0 (
    echo.
    echo [FAIL] Tests failed. Check logs:
    echo   logs\run.log
    echo   logs\backend.log
    echo   logs\frontend.log
    echo.
)

pause
