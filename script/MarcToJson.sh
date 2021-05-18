#!/bin/bash
BUILD_DIR=/cul/src/order-import-poc
export CLASSPATH=`mvn -f ../pom.xml -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath"`
export OPTS="-Dlog4j.configuration=file:../src/main/resources/log4j.properties"
java -cp $CLASSPATH $OPTS org.olf.folio.order.MarcToJson $*
