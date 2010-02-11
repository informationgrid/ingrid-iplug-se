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


THIS="$0"

# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`
PID=$INGRID_HOME/ingrid-admin.pid

# functions
stopIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo stopping $command
        kill `cat $PID`
        echo "process ($procid) has been terminated."
      else
        echo "process is not running. Exit."
        exit 1
      fi
    else
      echo "process is not running. Exit."
      exit 1
    fi
  
}

stopNoExitIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo stopping $command
        kill `cat $PID`
        echo "process ($procid) has been terminated."
      else
        echo "process is not running. Exit."
      fi
    else
      echo "process is not running. Exit."
    fi
}


startIplug()
{
  echo "Try starting ingrid component ($INGRID_HOME)..."
  if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo plug running as process `cat $PID`.  Stop it first.
        exit 1
      fi
  fi
  
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
  PORT=${2:-'50059'}
  echo "trying to start configuration user interface on port $PORT";
  
  nohup "$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS -Dlog4j.configuration=conf/log4j-search.properties -classpath "$CLASSPATH" $CLASS $PORT webapp --plugdescription conf/plugdescription-search.xml --descriptor conf/communication-search.xml > logs/admin-search.log &
  echo "ingrid component ($INGRID_HOME) started."
  echo $! > $PID
}

# make sure the current user has the privilege to execute that script
if [ "$INGRID_USER" = "" ]; then
  INGRID_USER="ingrid"
fi

STARTING_USER=`whoami`
if [ "$STARTING_USER" != "$INGRID_USER" ]; then
  echo "You must be user '$INGRID_USER' to start that script! Set INGRID_USER in environment to overwrite this."
  exit 1
fi 

case "$1" in
  start)
    startIplug
    ;;
  stop)
    stopIplug
    ;;
  restart)
    stopNoExitIplug
    echo "sleep 3 sec ..."
    sleep 3
    startIplug
    ;;
  status)
    if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo "process ($procid) is running."
      else
        echo "process is not running. Exit."
      fi
    else
      echo "process is not running. Exit."
    fi
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac