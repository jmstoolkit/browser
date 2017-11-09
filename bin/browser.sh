#!/bin/bash
BIN_DIR=$(dirname $0)
. $BIN_DIR/jtk.sh
COMMAND=com.jmstoolkit.queuebrowser.QueueBrowserApp
JAVA_OPTS="-Djava.util.logging.config.file=logging.properties"
# Change the name of the properties file:
#JAVA_OPTS="-Dapp.properties=myfile.props -Djndi.properties=some.props"

java $JAVA_OPTS $COMMAND $*

