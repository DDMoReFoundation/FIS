SET SERVICE_HOME=%~dp0

IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD %SERVICE_HOME%

SET PUBLISH_INPUTS=%SERVICE_HOME%\scripts\publishInputs.groovy
SET RETRIEVE_OUTPUTS=%SERVICE_HOME%\scripts\retrieveOutputs.groovy
SET READ_RESOURCE=%SERVICE_HOME%\scripts\readResource.groovy
SET WRITE_RESOURCE=%SERVICE_HOME%\scripts\writeResource.groovy

SET params=-Dfis.publishInputs="%PUBLISH_INPUTS%"
SET params=-Dfis.retrieveOutputs="%RETRIEVE_OUTPUTS%" %params%
SET params=-Dfis.readResource="%READ_RESOURCE%" %params%
SET params=-Dfis.writeResource="%WRITE_RESOURCE%" %params%
SET params=-Dnonmem.setup.script="%~dp0\..\setup.bat" %params%
SET params=-Dconverter.toolbox.executable="%~dp0\..\converter-toolbox-bundle-0.0.1-SNAPSHOT\convert.bat" %params%

if "%PROCESSOR_ARCHITECTURE%" == "x86" ( 
    if not defined PROCESSOR_ARCHITEW6432 (
		set TRAINING_JAVA_HOME=%SERVICE_HOME%\..\MDL_IDE\x86\jre
	) else (
		set TRAINING_JAVA_HOME=%SERVICE_HOME%\..\MDL_IDE\x86_64\jre
	)
) else (
	set TRAINING_JAVA_HOME=%SERVICE_HOME%\..\MDL_IDE\x86_64\jre
)

IF EXIST "%TRAINING_JAVA_HOME%\bin\java.exe" (
    echo Using Java from SEE
    SET JAVA_CMD=%TRAINING_JAVA_HOME%\bin\java
) ELSE IF EXIST "%JAVA_HOME%\bin\java.exe" (
    echo Using Java from JAVA_HOME environment variable
    SET JAVA_CMD=%JAVA_HOME%\bin\java
) ELSE (
    echo Falling beck to using Java from path; it will fail if Java is not installed
    SET JAVA_CMD=java
)

"%JAVA_CMD%" %params% -Dmango.mif.invoker.shell="cmd.exe /C" -Dmango.mif.invoker.tmp.file.ext=".bat" -DFIS_HOME="%SERVICE_HOME%" -jar %SERVICE_HOME%\fis.jar