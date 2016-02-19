@echo off

REM  This Windows Powershell command (built in to Windows 7) is merely a 'nice-to-have' that
REM  sets a large console buffer size in order that the entire output can be scrolled around.
powershell -command "&{$H=get-host;$W=$H.ui.rawui;$B=$W.buffersize;$B.height=2000;$W.buffersize=$B;}" <NUL 1>NUL 2>NUL

REM  Locations without trailing '\'
SET SERVICE_HOME=%~dp0
IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD %SERVICE_HOME%

SET SERVICE_BINARY=${project.build.finalName}.${project.packaging}

REM Possible modes are ("localMIF" - MIF running on local host, "remoteMIF" - MIF running on a remote host)
REM The following property should be set by the calling script
REM SET MIF_MODE=localMIF

REM  See comment in config.properties regarding the important distinction between these three properties, and what they should be set to.
SET EXECUTION_HOST_FILESHARE_LOCAL=%TEMP%\mifshare
SET EXECUTION_HOST_FILESHARE=%EXECUTION_HOST_FILESHARE_LOCAL%
SET EXECUTION_HOST_FILESHARE_REMOTE=%EXECUTION_HOST_FILESHARE%
REM Parameter string specifying MIF URL, leave empty to use default
SET MIF_URL_PARAM=
SET MIF_MODE_PARAM=

IF ["%MIF_MODE%"] == ["remoteMIF"] (
    ECHO "The following properties must be set manually: EXECUTION_HOST_FILESHARE_LOCAL, EXECUTION_HOST_FILESHARE, EXECUTION_HOST_FILESHARE_REMOTE!"
    PAUSE
    EXIT
REM Location where the file share location is mounted on the local host. (e.g. Z:\mifshare-win)
    SET EXECUTION_HOST_FILESHARE_LOCAL=
REM Location where the file share is mounted on MIF host (e.g. /usr/global/mifshare-win)
    SET EXECUTION_HOST_FILESHARE=
REM Location where the file share is mounted on the execution (e.g. grid) host (e.g. /usr/global/mifshare-win)
    SET EXECUTION_HOST_FILESHARE_REMOTE=
REM Update the following property so it points to a host where MIF is deployed
    SET MIF_URL_PARAM=-Dfis.mif.url="http://<MIF_HOST>:<MIF_PORT>/MIFServer/REST/services/"
REM Don't modify the following line!
    SET MIF_MODE_PARAM=-Dspring.profiles.active=%MIF_MODE%
) else (
REM  If the fileshare location is pointed somewhere else other than within the system temporary directory then
REM  this directory creation should be removed and the directory created manually if it doesn't already exist.
    IF NOT EXIST "%EXECUTION_HOST_FILESHARE_LOCAL%" (
        mkdir "%EXECUTION_HOST_FILESHARE_LOCAL%"
    )
)

SET params= -Dexecution.host.fileshare.local="%EXECUTION_HOST_FILESHARE_LOCAL%" ^
 -Dexecution.host.fileshare="%EXECUTION_HOST_FILESHARE%" ^
 -Dexecution.host.fileshare.remote="%EXECUTION_HOST_FILESHARE_REMOTE%" ^
 %MIF_URL_PARAM% ^
 %MIF_MODE_PARAM% ^
 -Dfis.mif.userName= ^
 -Dfis.mif.userPassword=
 
REM  - If the MIF user credentials are set above then these will be used for MIF job execution,
REM    otherwise jobs will be executed as the MIF service account user.
REM  - If a remote MIF is to be used for job execution, then the fis.mif.url property needs to be
REM    set/overridden in the parameters above (it defaults to localhost in config.properties).
REM  - The logging level can be overridden via appropriate system property: -Dlogging.level.eu.ddmore=DEBUG

java.exe %JAVA_OPTS% %params% -DFIS_HOME="%SERVICE_HOME%" -Dfis.workingDirectory="%SERVICE_HOME%\tmp" -jar "%SERVICE_HOME%"\%SERVICE_BINARY%

EXIT
