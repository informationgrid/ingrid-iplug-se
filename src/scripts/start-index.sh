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
PID=$INGRID_HOME/ingrid-index.pid

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
  
  if [ -d "$INGRID_HOME/../repository/" ]; then
  	echo 'syncronize libs from repository...'
	rsync -av --update --existing $INGRID_HOME/../repository/ $INGRID_HOME/lib/ingrid-ext/
	echo 'finished syncronize.'
  fi
  
  export NUTCH_OPTS="-Dnutch.gui.theme=portal-u -Djava.security.auth.login.config=conf/nutchgui.auth -Dlog4j.configuration=conf/log4j-index.properties"
  nohup bin/nutch-se.sh de.ingrid.iplug.se.AdministrationApp --plugdescription conf/plugdescription-index.xml --descriptor conf/communication-index.xml --secure > logs/console-index.log &
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
