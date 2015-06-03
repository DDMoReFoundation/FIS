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

params=" -Dexecution.host.fileshare.local=\"$EXECUTION_HOST_FILESHARE_LOCAL\" \
 -Dexecution.host.fileshare=\"$EXECUTION_HOST_FILESHARE\" \
 -Dexecution.host.fileshare.remote=\"$EXECUTION_HOST_FILESHARE_REMOTE\" \
 -Dmif.userName= -Dmif.userPassword= \
 -DFIS_HOME=\"$SERVICE_HOME\" "

echo Starting up FIS with parameters: $params

# This is a bit convoluted but this seemed to be the only way to get quoted paths with spaces in
# in the -D parameters treated as part of the same argument to the java call and not split into
# separate arguments on those spaces
eval  `echo  java $params -jar ./$SERVICE_BINARY`
