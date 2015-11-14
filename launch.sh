#!/bin/bash
function wait_for_server() {
  until `docker exec wildfly /opt/jboss/wildfly/bin/jboss-cli.sh -c "ls /deployment" &> /dev/null`; do
    sleep 1
  done
}
docker rm -f $(docker ps -aq)
docker-compose build --no-cache
docker-compose --x-networking up -d
echo "Waiting on Activiti..."
wait_for_server
open http://$(docker-machine ip dev):8080/activiti-explorer