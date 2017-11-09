#!/bin/bash
# Run the JMSToolKit CLI
# Scott Douglass <scott@swdouglass.com>
# License: GPLv3
# Copyright: 2017
#

# Make sure things work on Cygwin shell with Windows JDK
FS="/"
CS=":"
OS="$(uname)"
case "$OS" in
"Cygwin-*")
  FS="\\"
  CS=";"
  CYGWIN=1
;;
esac

_path() {
  if [ -n "$CYGWIN" ]; then
    cygpath -w "$(pwd)/$1"
  else
    echo "$(pwd)/$1"
  fi  
}

# Make sure we use the java we expect
if [ -n "$JAVA_HOME" ]; then
  export PATH=${JAVA_HOME}/bin:$PATH
fi
CLASSPATH="."
JAR="$(_path jmstoolkit-browser-jar-with-dependencies.jar)"
CLASSPATH="${JAR}${CS}${CLASSPATH}"
# Set to the directory where your JMS provider jar files are
JMS_PROVIDER_DIR=activemq
if [ -d "${JMS_PROVIDER_DIR}" ]; then
  for J in $(ls ${JMS_PROVIDER_DIR}/*.jar); do
    CLASSPATH=$(_path $J)${CS}${CLASSPATH}
  done
fi
if [ -d lib ]; then
  for J in $(ls lib/*.jar 2>/dev/null); do
    CLASSPATH=$(_path $J)${CS}${CLASSPATH}
  done
fi
export CLASSPATH
