#!/bin/bash

SERVICE_HOME=$(pwd)

PUBLISH_INPUTS="$SERVICE_HOME/scripts/publishInputs.groovy"
RETRIEVE_OUTPUTS="$SERVICE_HOME/scripts/retrieveOutputs.groovy"
READ_RESOURCE="$SERVICE_HOME/scripts/readResource.groovy"
WRITE_RESOURCE="$SERVICE_HOME/scripts/writeResource.groovy"
#  If only local execution will be performed then these are the same path string, e.g. a directory within the system temporary directory.
#  Otherwise execution.host.fileshare must point to a directory within a virtual filesystem mapped from the remote host, and
#  execution.host.fileshare.remote must point to that directory on the remote host.
EXECUTION_HOST_FILESHARE=/tmp/mifshare
EXECUTION_HOST_FILESHARE_REMOTE=$EXECUTION_HOST_FILESHARE

#  If the fileshare location is pointed somewhere else other than within the system temporary directory then
#  this directory creation should be removed and the directory created manually if it doesn't already exist.
if [ -d "$EXECUTION_HOST_FILESHARE" ]
then
    mkdir "$EXECUTION_HOST_FILESHARE"
fi

params=" -Dfis.publishInputs=\"$PUBLISH_INPUTS\" -Dfis.retrieveOutputs=\"$RETRIEVE_OUTPUTS\" -Dfis.readResource=\"$READ_RESOURCE\" -Dfis.writeResource=\"$WRITE_RESOURCE\" \
 -Dexecution.host.fileshare=\"$EXECUTION_HOST_FILESHARE\" \
 -Dexecution.host.fileshare.remote=\"$EXECUTION_HOST_FILESHARE_REMOTE\" \
 -DFIS_HOME=\"$SERVICE_HOME\" \
 -Dconverter.toolbox.executable=echo "; # dummy converter toolbox until this is implemented for Linux as well as Windows (it currently uses Windows batch file)
#  If FIS is executing in standalone mode, outside of SEE, then the location of the
#  converter toolbox executable will need to be set / amended above.

echo Starting up FIS with parameters: $params

# This is a bit convoluted but this seemed to be the only way to get quoted paths with spaces in
# in the -D parameters treated as part of the same argument to the java call and not split into
# separate arguments on those spaces
eval  `echo  java $params -jar ./fis.jar`
