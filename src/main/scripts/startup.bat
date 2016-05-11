@echo off

REM  This Windows Powershell command (built in to Windows 7) is merely a 'nice-to-have' that
REM  sets a large console buffer size in order that the entire output can be scrolled around.
powershell -command "&{$H=get-host;$W=$H.ui.rawui;$B=$W.buffersize;$B.height=2000;$W.buffersize=$B;}" <NUL 1>NUL 2>NUL

REM  Locations without trailing '\'
SET SERVICE_HOME=%~dp0
IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD %SERVICE_HOME%

SET SERVICE_BINARY=${project.build.finalName}.${project.packaging}

# Holds FIS command-line parameters that modify the modes of remote services integration
SET FIS_MODES=

REM Possible modes are ("localMIF" - MIF running on local host, "remoteMIF" - MIF running on a remote host)
REM The following property should be set by the calling script
REM SET MIF_MODE=localMIF

REM Possible modes are ("localCTS" - CTS running on local host, "remoteCTS" - CTS running on a remote host)
REM The following property should be set by the calling script
REM SET CTS_MODE=localCTS

REM  See comment in config.properties regarding the important distinction between these three properties, and what they should be set to.
SET EXECUTION_HOST_FILESHARE_LOCAL=%TEMP%\mifshare
SET EXECUTION_HOST_FILESHARE=%EXECUTION_HOST_FILESHARE_LOCAL%
SET EXECUTION_HOST_FILESHARE_REMOTE=%EXECUTION_HOST_FILESHARE%
REM Parameter string specifying MIF URL, leave empty to use default
SET MIF_URL_PARAM=

REM Parameter string specifying CTS URL, leave empty to use default
SET CTS_URL_PARAM=

IF ["%MIF_MODE%"] == ["remoteMIF"] (
    ECHO "The following properties must be set manually: EXECUTION_HOST_FILESHARE_LOCAL, EXECUTION_HOST_FILESHARE, EXECUTION_HOST_FILESHARE_REMOTE!"
REM Location where the file share location is mounted on the local host. (e.g. Z:\mifshare-win)
    SET EXECUTION_HOST_FILESHARE_LOCAL=
REM Location where the file share is mounted on MIF host (e.g. /usr/global/mifshare-win)
    SET EXECUTION_HOST_FILESHARE=
REM Location where the file share is mounted on the execution (e.g. grid) host (e.g. /usr/global/mifshare-win)
    SET EXECUTION_HOST_FILESHARE_REMOTE=
REM Update the following property so it points to a host where MIF is deployed
    SET MIF_URL_PARAM=-Dfis.mif.url="http://<MIF_HOST>:<MIF_PORT>/MIFServer/REST/services/"
REM Don't modify the following line!
    SET FIS_MODES=-Dspring.profiles.active=%MIF_MODE%
) else (
REM  If the fileshare location is pointed somewhere else other than within the system temporary directory then
REM  this directory creation should be removed and the directory created manually if it doesn't already exist.
    IF NOT EXIST "%EXECUTION_HOST_FILESHARE_LOCAL%" (
        mkdir "%EXECUTION_HOST_FILESHARE_LOCAL%"
    )
)

IF ["%CTS_MODE%"] == ["remoteCTS"] (
   IF ["%FIS_MODES%"] == [""] (
     SET FIS_MODES=-Dspring.profiles.active=%CTS_MODE%
   ) ELSE (
     SET FIS_MODES=%FIS_MODES%,%CTS_MODE%
   )
   SET CTS_URL_PARAM=-Dfis.cts.url="http://<CTS_HOST>:<CTS_PORT>/" -Dfis.cts.management.url="http://<CTS_HOST>:<CTS_PORT>/" 
)
SET params= -Dfis.fileshare.fisHostPath="%EXECUTION_HOST_FILESHARE_LOCAL%" ^
 -Dfis.fileshare.mifHostPath="%EXECUTION_HOST_FILESHARE%" ^
 -Dfis.fileshare.executionHostPath="%EXECUTION_HOST_FILESHARE_REMOTE%" ^
 %MIF_URL_PARAM% ^
 %CTS_URL_PARAM% ^
 %FIS_MODES% ^
 -Dfis.mif.userName= ^
 -Dfis.mif.userPassword=

REM Increase permGen size
SET JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=128m

REM Add these properties to 'params' variable if FIS is to integrate with CTS and/or MIF via SSL using self-signed key
REM -Djavax.net.ssl.trustStore="%SERVICE_HOME%/keystore/cacerts" ^
REM -Djavax.net.ssl.trustStorePassword=ddmore
 
REM  - If the MIF user credentials are set above then these will be used for MIF job execution,
REM    otherwise jobs will be executed as the MIF service account user.
REM  - If a remote MIF is to be used for job execution, then the fis.mif.url property needs to be
REM    set/overridden in the parameters above (it defaults to localhost in config.properties).
REM  - The logging level can be overridden via appropriate system property: -Dlogging.level.eu.ddmore=DEBUG

java.exe %JAVA_OPTS% %params% -DFIS_HOME="%SERVICE_HOME%" -Dfis.workingDirectory.path="%SERVICE_HOME%\tmp" -jar "%SERVICE_HOME%"\%SERVICE_BINARY%

EXIT
