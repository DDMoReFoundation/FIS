SET WORKING_DIR=%1
SET MIF_WORKING_DIR=%2
SET EXECUTION_FILE=%3
SET CONVERTER_TOOLBOX_EXE=%4
SET SCRIPT_DIR=%~dp0
SET MODEL_NAME=%~n3
SET MODEL_EXT=%~x3

echo %1 %2 %3 %4

IF %MODEL_EXT% == .mdl (
    cd %SCRIPT_DIR%\..\mockData

    cp warfarin_PK_PRED.csv %MIF_WORKING_DIR%\%MODEL_NAME%_data.csv
    cp warfarin_PK_PRED.xml %MIF_WORKING_DIR%\%MODEL_NAME%.xml
)

exit /b %errorlevel%