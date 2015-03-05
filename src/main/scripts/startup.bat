@echo off

REM  This Windows Powershell command (built in to Windows 7) is merely a 'nice-to-have' that
REM  sets a large console buffer size in order that the entire output can be scrolled around.
powershell -command "&{$H=get-host;$W=$H.ui.rawui;$B=$W.buffersize;$B.height=2000;$W.buffersize=$B;}" <NUL 1>NUL 2>NUL

REM  Locations without trailing '\'
SET SERVICE_HOME=%~dp0
IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD %SERVICE_HOME%

SET PUBLISH_INPUTS=%SERVICE_HOME%\scripts\publishInputs.groovy
SET RETRIEVE_OUTPUTS=%SERVICE_HOME%\scripts\retrieveOutputs.groovy
SET READ_RESOURCE=%SERVICE_HOME%\scripts\readResource.groovy
SET WRITE_RESOURCE=%SERVICE_HOME%\scripts\writeResource.groovy
SET MDL_CONVERTER=%SERVICE_HOME%\scripts\mdlConverter.groovy
REM  See comment in config.properties regarding the important distinction between these three properties, and what they should be set to.
SET EXECUTION_HOST_FILESHARE_LOCAL=%TEMP%\mifshare
SET EXECUTION_HOST_FILESHARE=%EXECUTION_HOST_FILESHARE_LOCAL%
SET EXECUTION_HOST_FILESHARE_REMOTE=%EXECUTION_HOST_FILESHARE%

REM  If the fileshare location is pointed somewhere else other than within the system temporary directory then
REM  this directory creation should be removed and the directory created manually if it doesn't already exist.
IF NOT EXIST "%EXECUTION_HOST_FILESHARE_LOCAL%" (
    mkdir "%EXECUTION_HOST_FILESHARE_LOCAL%"
)

SET params= -Dfis.publishInputs="%PUBLISH_INPUTS%" ^
 -Dfis.retrieveOutputs="%RETRIEVE_OUTPUTS%" ^
 -Dfis.readResource="%READ_RESOURCE%" ^
 -Dfis.writeResource="%WRITE_RESOURCE%" ^
 -Dfis.mdlConverter="%MDL_CONVERTER%" ^
 -Dconverter.toolbox.executable="%SERVICE_HOME%\..\converter-toolbox-distribution\converter-toolbox\convert.bat" ^
 -Dexecution.host.fileshare.local="%EXECUTION_HOST_FILESHARE_LOCAL%" ^
 -Dexecution.host.fileshare="%EXECUTION_HOST_FILESHARE%" ^
 -Dexecution.host.fileshare.remote="%EXECUTION_HOST_FILESHARE_REMOTE%"
REM  If FIS is executing in standalone mode, outside of SEE, then the location of the
REM  converter toolbox executable will need to be amended above.

IF NOT DEFINED JAVA_CMD (
    echo FIS is executing in standalone mode, outside of SEE, which would have set JAVA_CMD
    IF EXIST "%JAVA_HOME%\bin\java.exe" (
        echo Using Java from JAVA_HOME environment variable
        SET JAVA_CMD="%JAVA_HOME%\bin\java"
    ) ELSE (
        echo Falling beck to using Java from system path; this will fail if Java is not installed
        SET JAVA_CMD=java
    )
)

%JAVA_CMD% %params% -DFIS_HOME="%SERVICE_HOME%" -jar "%SERVICE_HOME%"\fis.jar

EXIT
