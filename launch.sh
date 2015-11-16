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
docker rm -f $(docker ps -aq)
docker-compose build --no-cache
docker-compose --x-networking up -d
echo "Waiting on Activiti..."
wait_for_server
open http://kermit:kermit@$MACHINE_IP:8080/activiti-explorer
