@rem ***************************************************************************
@rem Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
@rem
@rem This program is free software: you can redistribute it and/or modify it under
@rem the terms of the GNU Affero General Public License as published by the
@rem Free Software Foundation, version 3.
@rem
@rem This program is distributed in the hope that it will be useful,
@rem but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
@rem or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
@rem for more details.
@rem
@rem You should have received a copy of the GNU Affero General Public License along
@rem with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
@rem ***************************************************************************
@echo off

REM  This Windows Powershell command (built in to Windows 7) is merely a 'nice-to-have' that
REM  sets a large console buffer size in order that the entire output can be scrolled around.
powershell -command "&{$H=get-host;$W=$H.ui.rawui;$B=$W.buffersize;$B.height=2000;$W.buffersize=$B;}" <NUL 1>NUL 2>NUL

REM  Locations without trailing '\'
SET SERVICE_HOME=%~dp0
IF %SERVICE_HOME:~-1%==\ SET SERVICE_HOME=%SERVICE_HOME:~0,-1%

CD %SERVICE_HOME%

SET SERVICE_BINARY=${project.build.finalName}.${project.packaging}
REM  See comment in config.properties regarding the important distinction between these three properties, and what they should be set to.
SET EXECUTION_HOST_FILESHARE_LOCAL=%TEMP%\mifshare
SET EXECUTION_HOST_FILESHARE=%EXECUTION_HOST_FILESHARE_LOCAL%
SET EXECUTION_HOST_FILESHARE_REMOTE=%EXECUTION_HOST_FILESHARE%

REM  If the fileshare location is pointed somewhere else other than within the system temporary directory then
REM  this directory creation should be removed and the directory created manually if it doesn't already exist.
IF NOT EXIST "%EXECUTION_HOST_FILESHARE_LOCAL%" (
    mkdir "%EXECUTION_HOST_FILESHARE_LOCAL%"
)

SET params= -Dexecution.host.fileshare.local="%EXECUTION_HOST_FILESHARE_LOCAL%" ^
 -Dexecution.host.fileshare="%EXECUTION_HOST_FILESHARE%" ^
 -Dexecution.host.fileshare.remote="%EXECUTION_HOST_FILESHARE_REMOTE%" ^
 -Dmif.userName= ^
 -Dmif.userPassword=
REM  - If the MIF user credentials are set above then these will be used for MIF job execution,
REM    otherwise jobs will be executed as the MIF service account user.
REM  - If a remote MIF is to be used for job execution, then the mif.url property needs to be
REM    set/overridden in the parameters above (it defaults to localhost in config.properties).
REM  - The logging level can be overridden via appropriate system property: -Dlogging.level.eu.ddmore=DEBUG

java.exe %JAVA_OPTS% %params% -DFIS_HOME="%SERVICE_HOME%" -jar "%SERVICE_HOME%"\%SERVICE_BINARY%

EXIT
