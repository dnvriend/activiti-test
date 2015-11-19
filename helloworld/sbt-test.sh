#!/bin/bash
export JAVA_OPTS="-XX:+CMSClassUnloadingEnabled -XX:MaxMetaspaceSize=1G -XX:MetaspaceSize=256M -Xms8G -Xmx8G"
sbt
