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

# run it
export CLASSPATH="$CLASSPATH"
INGRID_OPTS="$INGRID_OPTS -Dingrid_home=$INGRID_HOME -Dfile.encoding=UTF8 -XX:+UseG1GC -XX:NewRatio=1"
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

# check java version
JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
JAVA_VERSION_PART_0=`echo $JAVA_VERSION | awk '{split($0, array, "-")} END{print array[1]}'`
JAVA_VERSION_PART_1=`echo $JAVA_VERSION_PART_0 | awk '{split($0, array, "_")} END{print array[1]}'`
JAVA_VERSION_PART_2=`echo $JAVA_VERSION_PART_0 | awk '{split($0, array, "_")} END{print array[2]}'`
if [[ "$JAVA_VERSION_PART_1" > "1.7.0" ]]; then
	LENGTH="${#JAVA_VERSION_PART_2}"
	if [[ "$LENGTH" < "2" ]]; then
		JAVA_VERSION_PART_2="0"$JAVA_VERSION_PART_2
	fi
	if [[ "$JAVA_VERSION_PART_2" > "19" ]]; then
		INGRID_OPTS="$INGRID_OPTS -XX:+UseStringDeduplication"
	elif [[ "$JAVA_VERSION_PART_1" > "1.8.0" ]]; then
		INGRID_OPTS="$INGRID_OPTS -XX:+UseStringDeduplication"
	fi
fi

"$JAVA" $INGRID_OPTS $CLASS $@
