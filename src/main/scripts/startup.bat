@echo off

REM  Locations without trailing '\'
SET SERVICE_HOME=%~dp0
IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD %SERVICE_HOME%

SET PUBLISH_INPUTS=%SERVICE_HOME%\scripts\publishInputs.groovy
SET RETRIEVE_OUTPUTS=%SERVICE_HOME%\scripts\retrieveOutputs.groovy
SET READ_RESOURCE=%SERVICE_HOME%\scripts\readResource.groovy
SET WRITE_RESOURCE=%SERVICE_HOME%\scripts\writeResource.groovy

SET params= -Dfis.publishInputs="%PUBLISH_INPUTS%" ^
 -Dfis.retrieveOutputs="%RETRIEVE_OUTPUTS%" ^
 -Dfis.readResource="%READ_RESOURCE%" ^
 -Dfis.writeResource="%WRITE_RESOURCE%" ^
 -Dconverter.toolbox.executable="%SERVICE_HOME%\..\converter-toolbox-bundle\convert.bat"

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
