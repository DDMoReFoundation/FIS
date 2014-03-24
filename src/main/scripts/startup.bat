SET FIS_HOME=%~dp0

IF %FIS_HOME:~-1%==\ SET FIS_HOME=%FIS_HOME:~0,-1%
SET PUBLISH_INPUTS=%FIS_HOME%\scripts\publishInputs.groovy
SET PUBLISH_OUTPUTS=%FIS_HOME%\scripts\retrieveOutputs.groovy

SET params=-Dfis.publishInputs="%PUBLISH_INPUTS%"
SET params=-Dfis.retrieveOutputs="%PUBLISH_OUTPUTS%" %params%

java %params% -Dmango.mif.invoker.shell="cmd.exe /C" -Dmango.mif.invoker.tmp.file.ext=".bat" -DFIS_HOME="%FIS_HOME%" -jar %FIS_HOME%\fis.jar