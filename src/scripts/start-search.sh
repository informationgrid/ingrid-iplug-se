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
PID=$INGRID_HOME/ingrid-search.pid

# functions
stopIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  if [ -f $PID ]; then
		procid=`cat $PID`
		idcount=`ps -p $procid | wc -l`
		if [ $idcount -eq 2 ]; then
			echo stopping $command, wait 3 sec to exit.
			kill `cat $PID`
			sleep 3
			idcount=`ps -p $procid | wc -l`
			if [ $idcount -eq 1 ]; then
				echo "process ($procid) has been terminated."
				unlink $PID
				exit 0
			else
				COUNTER=1
				SECS=0
				while [  $COUNTER -lt 10 ]; 
				do
					COUNTER=$(($COUNTER + 1))
					echo "process is still running. wait 1 more sec."
					sleep 1
					idcount=`ps -p $procid | wc -l`
					if [ $idcount -eq 1 ]; then
						SECS=$(($COUNTER + $SECS))
					    echo "process ($procid) has been terminated after $SECS sec."
					    unlink $PID
					    exit 0
					fi					
				done
				echo "process is still running. force kill -9."
				kill -9 `cat $PID`
				exit 0
			fi 
		else
			echo "process is not running. Exit."
			unlink $PID 
		fi
	else
		echo "process is not running. Exit."
	fi
}

stopNoExitIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  if [ -f $PID ]; then
		procid=`cat $PID`
		idcount=`ps -p $procid | wc -l`
		if [ $idcount -eq 2 ]; then
			echo stopping $command, wait 3 sec to exit.
			kill `cat $PID`
			sleep 3
			idcount=`ps -p $procid | wc -l`
			if [ $idcount -eq 1 ]; then
				echo "process ($procid) has been terminated."
				unlink $PID
			else
				COUNTER=1
				SECS=0
				while [  $COUNTER -lt 10 ]; 
				do
					COUNTER=$(($COUNTER + 1))
					echo "process is still running. wait 1 more sec."
					sleep 1
					idcount=`ps -p $procid | wc -l`
					if [ $idcount -eq 1 ]; then
						SECS=$(($COUNTER + $SECS))
					    echo "process ($procid) has been terminated after $SECS sec."
					    unlink $PID
					fi					
				done
				echo "process is still running. force kill -9."
				kill -9 `cat $PID`
			fi
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
  
  export NUTCH_OPTS="-Dlog4j.configuration=conf/log4j-search.properties"
  nohup bin/nutch-se.sh de.ingrid.iplug.PlugServer --plugdescription conf/plugdescription-search.xml --descriptor conf/communication-search.xml > logs/console-search.log &
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
