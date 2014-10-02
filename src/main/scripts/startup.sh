#!/bin/bash

SERVICE_HOME=$(pwd)

PUBLISH_INPUTS=$SERVICE_HOME/scripts/publishInputs.groovy
RETRIEVE_OUTPUTS=$SERVICE_HOME/scripts/retrieveOutputs.groovy
READ_RESOURCE=$SERVICE_HOME/scripts/readResource.groovy
WRITE_RESOURCE=$SERVICE_HOME/scripts/writeResource.groovy

params=" -Dfis.publishInputs=\"$PUBLISH_INPUTS\" -Dfis.retrieveOutputs=\"$RETRIEVE_OUTPUTS\" -Dfis.readResource=\"$READ_RESOURCE\" -Dfis.writeResource=\"$WRITE_RESOURCE\" \
 -DFIS_HOME=\"$SERVICE_HOME\" \
 -Dconverter.toolbox.executable=echo "; # dummy converter toolbox until this is implemented for Linux as well as Windows

# Converter Toolbox involves Windows-only batch files :- 
#  -Dconverter.toolbox.executable="%SERVICE_HOME%\..\converter-toolbox-bundle\convert.bat"

echo Starting up FIS with parameters: $params

# This is a bit convoluted but this seemed to be the only way to get quoted paths with spaces in
# in the -D parameters treated as part of the same argument to the java call and not split into
# separate arguments on those spaces
eval  `echo  java $params -jar ./fis.jar`
