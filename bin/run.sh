#!/bin/bash
JAR="jmstoolkit-browserr-jar-with-dependencies.jar"
COMMAND="-jar $JAR"
# Set to the directory where your JMS provider jar files are
#JMS_PROVIDER_DIR=`pwd`/activemq
if [ "X${JMS_PROVIDER_DIR}" != "X" ]; then
  for J in `ls lib/*.jar`; do
    CLASSPATH=${J}:${CLASSPATH}
  done
  for J in `ls ${JMS_PROVIDER_DIR}/*.jar`; do
    CLASSPATH=${J}:${CLASSPATH}
  done
  CLASSPATH="`pwd`/${JAR}:${CLASSPATH}"
  echo "CLASSPATH: $CLASSPATH"
  export CLASSPATH
fi

COMMAND="com.jmstoolkit.queuebrowser.QueueBrowserApp" 
JAVA_OPTS="-Djava.util.logging.config.file=logging.properties"
# Change the name of the properties file:
#JAVA_OPTS="-Dapp.properties=myfile.props -Djndi.properties=some.props"

java -classpath $CLASSPATH $JAVA_OPTS $COMMAND $*

