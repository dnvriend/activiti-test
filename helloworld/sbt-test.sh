#!/bin/bash
export JAVA_OPTS="-XX:+CMSClassUnloadingEnabled -XX:MaxMetaspaceSize=512M -XX:MetaspaceSize=256M -Xms2G -Xmx2G"
sbt
