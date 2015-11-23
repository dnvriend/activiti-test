#!/bin/bash

RUNNING_MACHINE=$(docker-machine ls | grep -i running | awk '{print $1;}')
MACHINE_IP=$(docker-machine ip $RUNNING_MACHINE)

function wait_for_server() {
  CHECK_RESPONSE="curl -s -o /dev/null -w "%{http_code}" http://$MACHINE_IP:8080/activiti-explorer"
  RESPONSE_CODE=$($CHECK_RESPONSE)
  
  until [ $RESPONSE_CODE -gt 0 ] && [ $RESPONSE_CODE -lt 400 ]; do
    sleep 1
	RESPONSE_CODE=$($CHECK_RESPONSE)
  done
}

function check_for_wildfly_image() {
if [[ "$(docker images -q dnvriend/wildfly 2> /dev/null)" == "" ]];
then
    cd wildfly
    ./docker-build.sh
    cd ..
fi
}

function check_for_activiti_image() {
if [[ "$(docker images -q dnvriend/activiti 2> /dev/null)" == "" ]];
then
    cd activiti
    ./docker-build.sh
    cd ..
fi
}

docker rm -f $(docker ps -aq)
docker-compose build --no-cache
docker-compose --x-networking up -d
check_for_wildfly_image
check_for_activiti_image
echo "Waiting on Activiti..."
wait_for_server
open http://admin:admin@$MACHINE_IP:8161/admin
open http://kermit:kermit@$MACHINE_IP:8080/activiti-explorer
