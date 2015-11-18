###
# **************************************************-
# ingrid-iplug-se-iplug
# ==================================================
# Copyright (C) 2014 - 2015 wemove digital solutions GmbH
# ==================================================
# Licensed under the EUPL, Version 1.1 or – as soon they will be
# approved by the European Commission - subsequent versions of the
# EUPL (the "Licence");
# 
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
# http://ec.europa.eu/idabc/eupl5
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and
# limitations under the Licence.
# **************************************************#
###
#!/bin/sh

# add libs to CLASSPATH
INGRID_HOME=`cd "$THIS_DIR" ; pwd`
CLASSPATH=${CLASSPATH}:${INGRID_CONF_DIR:=$INGRID_HOME/conf}
for f in $INGRID_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
# restore ordinary behaviour
unset IFS

# include default options, i.e. debug, jmx and jvm options
if [ -f $INGRID_HOME/env.user.sh ]; then
  eval `sh $INGRID_HOME/env.user.sh`
elif [ -f $INGRID_HOME/env.sh ]; then
  eval `sh $INGRID_HOME/env.sh`
fi

# run it
export CLASSPATH="$CLASSPATH"
INGRID_OPTS="$INGRID_OPTS -Dingrid_home=$INGRID_HOME"
# Uncomment when migrating from Oracle Database
#INGRID_OPTS="$INGRID_OPTS -Djdbc.drivers=oracle.jdbc.OracleDriver"
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

"$JAVA" $INGRID_OPTS $CLASS $@
