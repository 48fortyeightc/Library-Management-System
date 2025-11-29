@echo off
setlocal
pushd backend
echo [INFO] Starting backend (Spring Boot)...
call mvn spring-boot:run
if %ERRORLEVEL% NEQ 0 (
  echo [ERROR] Backend failed. Check the log above and ensure MySQL is running with user=root password=123123.
) else (
  echo [INFO] Backend process exited.
)
echo Press any key to close...
pause >nul
popd
endlocal
