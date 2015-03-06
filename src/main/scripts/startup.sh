#!/bin/bash

SERVICE_HOME=$(pwd)

PUBLISH_INPUTS="$SERVICE_HOME/scripts/publishInputs.groovy"
RETRIEVE_OUTPUTS="$SERVICE_HOME/scripts/retrieveOutputs.groovy"
READ_RESOURCE="$SERVICE_HOME/scripts/readResource.groovy"
WRITE_RESOURCE="$SERVICE_HOME/scripts/writeResource.groovy"
MDL_CONVERTER="$SERVICE_HOME/scripts/mdlConverter.groovy"
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

params=" -Dfis.publishInputs=\"$PUBLISH_INPUTS\" -Dfis.retrieveOutputs=\"$RETRIEVE_OUTPUTS\" -Dfis.readResource=\"$READ_RESOURCE\" -Dfis.writeResource=\"$WRITE_RESOURCE\" -Dfis.mdlConverter=\"$MDL_CONVERTER\" \
 -Dexecution.host.fileshare.local=\"$EXECUTION_HOST_FILESHARE_LOCAL\" \
 -Dexecution.host.fileshare=\"$EXECUTION_HOST_FILESHARE\" \
 -Dexecution.host.fileshare.remote=\"$EXECUTION_HOST_FILESHARE_REMOTE\" \
 -Dmif.userName= -Dmif.userPassword= \
 -DFIS_HOME=\"$SERVICE_HOME\" \
 -Dconverter.toolbox.executable=echo "; # dummy converter toolbox until this is implemented for Linux as well as Windows (it currently uses Windows batch file)
#  - If FIS is executing in standalone mode, outside of SEE, then the location of the
#    converter toolbox executable will need to be set / amended above.
#  - If the MIF user credentials are set above then these will be used for MIF job execution,
#    otherwise jobs will be executed as the MIF service account user.
#  - If a remote MIF is to be used for job execution, then the mif.url property needs to be
#    set/overridden in the parameters above (it defaults to localhost in config.properties).

echo Starting up FIS with parameters: $params

# This is a bit convoluted but this seemed to be the only way to get quoted paths with spaces in
# in the -D parameters treated as part of the same argument to the java call and not split into
# separate arguments on those spaces
eval  `echo  java $params -jar ./fis.jar`
