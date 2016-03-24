#!/bin/bash

SERVICE_HOME=$(pwd)


SERVICE_BINARY=${project.build.finalName}.${project.packaging}
#  See comment in config.properties regarding the important distinction between these three properties, and what they should be set to.
EXECUTION_HOST_FILESHARE_LOCAL=/tmp/mifshare
EXECUTION_HOST_FILESHARE=$EXECUTION_HOST_FILESHARE_LOCAL
EXECUTION_HOST_FILESHARE_REMOTE=$EXECUTION_HOST_FILESHARE

#  If the fileshare location is pointed somewhere else other than within the system temporary directory then
#  this directory creation should be removed and the directory created manually if it doesn't already exist.
if [ ! -d "$EXECUTION_HOST_FILESHARE_LOCAL" ]
then
    mkdir "$EXECUTION_HOST_FILESHARE_LOCAL"
fi

params=" -Dfis.fileshare.fisHostPath=\"$EXECUTION_HOST_FILESHARE_LOCAL\" \
 -Dfis.fileshare.mifHostPath=\"$EXECUTION_HOST_FILESHARE\" \
 -Dfis.fileshare.executionHostPath=\"$EXECUTION_HOST_FILESHARE_REMOTE\" \
 -Dmif.userName= -Dmif.userPassword= \
 -DFIS_HOME=\"$SERVICE_HOME\" \
 -Dfis.workingDirectory.path=\"$SERVICE_HOME\tmp\" "
REM  - If the MIF user credentials are set above then these will be used for MIF job execution,
REM    otherwise jobs will be executed as the MIF service account user.
REM  - If a remote MIF is to be used for job execution, then the mif.url property needs to be
REM    set/overridden in the parameters above (it defaults to localhost in config.properties).
REM  - The logging level can be overridden via appropriate system property: -Dlogging.level.eu.ddmore=DEBUG

echo Starting up FIS with parameters: $params

# This is a bit convoluted but this seemed to be the only way to get quoted paths with spaces in
# in the -D parameters treated as part of the same argument to the java call and not split into
# separate arguments on those spaces
eval  `echo  java $params -jar ./$SERVICE_BINARY`
