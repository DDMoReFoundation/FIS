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
@echo off
REM 'warfarin_PK_PRED' example
REM    cp warfarin_PK_PRED.csv %MIF_WORKING_DIR%\%MODEL_NAME%_data.csv
REM    cp warfarin_PK_PRED.xml %MIF_WORKING_DIR%\%MODEL_NAME%.xml
REM    cp warfarin_PK_PRED.xml %MIF_WORKING_DIR%\%MODEL_NAME%.pharmml

REM 'small' data
REM    cp example3_data.csv %MIF_WORKING_DIR%\%MODEL_NAME%_data.csv
@echo on
    cp example3_full_data_MDV.csv %MIF_WORKING_DIR%\%MODEL_NAME%_data.csv
    cp example3.xml %MIF_WORKING_DIR%\%MODEL_NAME%.xml
    cp example3.xml %MIF_WORKING_DIR%\%MODEL_NAME%.pharmml
)
IF %MODEL_EXT% == .xml (
    cp %MIF_WORKING_DIR%\%EXECUTION_FILE% %MIF_WORKING_DIR%\%MODEL_NAME%.pharmml
)

exit /b %errorlevel%