REM %1 - job working directory
REM %2 - job execution files directory
REM %3 - final job status

xcopy /s/e/y %2 %1

exit /b %ERRORLEVEL%