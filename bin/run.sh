#!/bin/bash
JAR="jmstoolkit-browser-jar-with-dependencies.jar"
COMMAND="-jar $JAR"
for J in `ls lib/*.jar 2>/dev/null`; do
  CLASSPATH=${J}:${CLASSPATH}
done
CLASSPATH="`pwd`/${JAR}:${CLASSPATH}"
# Set to the directory where your JMS provider jar files are
#JMS_PROVIDER_DIR=`pwd`/activemq
if [ "X${JMS_PROVIDER_DIR}" != "X" ]; then
  for J in `ls ${JMS_PROVIDER_DIR}/*.jar`; do
    CLASSPATH=${J}:${CLASSPATH}
  done
fi
echo "CLASSPATH: $CLASSPATH"
export CLASSPATH

COMMAND="com.jmstoolkit.queuebrowser.QueueBrowserApp" 
JAVA_OPTS="-Djava.util.logging.config.file=logging.properties"
# Change the name of the properties file:
#JAVA_OPTS="-Dapp.properties=myfile.props -Djndi.properties=some.props"

java -classpath $CLASSPATH $JAVA_OPTS $COMMAND $*

