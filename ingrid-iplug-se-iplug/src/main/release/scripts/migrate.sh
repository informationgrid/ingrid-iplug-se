#!/bin/sh

# add libs to CLASSPATH
INGRID_HOME=`cd "$THIS_DIR" ; pwd`
CLASSPATH=${CLASSPATH}:${INGRID_CONF_DIR:=$INGRID_HOME/conf}
for f in $INGRID_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
# restore ordinary behaviour
unset IFS

# run it
export CLASSPATH="$CLASSPATH"
INGRID_OPTS="$INGRID_OPTS -Dingrid_home=$INGRID_HOME -Dfile.encoding=UTF8"
CLASS=de.ingrid.iplug.se.migrate.Migrator


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
JAVA_HEAP_MAX=-Xmx128m

"$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS $CLASS $@