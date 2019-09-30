#!/bin/sh
JAVA_OPTS="$JAVA_OPTS"
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
java $JAVA_OPTS  $1 -jar $2