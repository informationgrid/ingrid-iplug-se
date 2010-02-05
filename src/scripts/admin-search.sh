#!/bin/sh
# 
##
# Environment Variables
#
#   INGRID_JAVA_HOME Overrides JAVA_HOME.
#
#   INGRID_HEAPSIZE  heap to use in mb, if not setted we use 1000.
#
#   INGRID_OPTS      addtional java runtime options
#

# resolve links - $0 may be a softlink
THIS="$0"
while [ -h "$THIS" ]; do
  ls=`ls -ld "$THIS"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    THIS="$link"
  else
    THIS=`dirname "$THIS"`/"$link"
  fi
done

# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`

# first sync libs
#echo '... syncronize libs from repository'
#rsync -av --update --existing $INGRID_HOME/../repository/ $INGRID_HOME/lib/


# some Java parameters
if [ "$INGRID_JAVA_HOME" != "" ]; then
  #echo "run java in $INGRID_JAVA_HOME"
  JAVA_HOME=$INGRID_JAVA_HOME
fi
  
if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA=$JAVA_HOME/bin/java
JAVA_HEAP_MAX=-Xmx1000m 

# check envvars which might override default args
if [ "$INGRID_HEAPSIZE" != "" ]; then
  #echo "run with heapsize $INGRID_HEAPSIZE"
  JAVA_HEAP_MAX="-Xmx""$INGRID_HEAPSIZE""m"
  #echo $JAVA_HEAP_MAX
fi

# CLASSPATH initially contains $INGRID_CONF_DIR, or defaults to $INGRID_HOME/conf
CLASSPATH=${INGRID_CONF_DIR:=$INGRID_HOME/conf}
CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:${INGRID_HOME}


# so that filenames w/ spaces are handled correctly in loops below
IFS=


# add libs to CLASSPATH
for f in $INGRID_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
for f in $INGRID_HOME/lib/jetty-ext/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
for f in $INGRID_HOME/lib/ingrid-ext/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

for f in $INGRID_HOME/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done


# restore ordinary behaviour
unset IFS


CLASS=de.ingrid.iplug.AdminServer


# cygwin path translation
if expr `uname` : 'CYGWIN*' > /dev/null; then
  CLASSPATH=`cygpath -p -w "$CLASSPATH"`
fi

# port
PORT=${1:-'50059'}
echo "trying to start configuration user interface on port $PORT";

# run it
nohup "$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS -Dlog4j.configuration=conf/log4j-search.properties -classpath "$CLASSPATH" $CLASS $PORT webapp --plugdescription conf/plugdescription-search.xml --descriptor conf/communication-search.xml > logs/admin-search.log &
echo "user interface startet with process id $!";
