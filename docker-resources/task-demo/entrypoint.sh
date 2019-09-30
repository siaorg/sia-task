#!/bin/sh
JAVA_OPTS="$JAVA_OPTS"
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
java $JAVA_OPTS -jar $1