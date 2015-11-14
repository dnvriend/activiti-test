# Wildfly 9.0.2.Final
The script `docker-build.sh` builds a configured Wildfly 9.0.2.Final that contains a `postgres` datasource 
and an `ActiveMQ` resource adapter. 

# Server configuration
The server is configured with the following
 
# Postgres Data Source
- Driver Version: postgresql-9.4-1205.jdbc42.jar
- Data Source: java:jboss/datasources/PostgresDS
- connection-url: jdbc:postgresql://postgres:5432/postgres
- username: postgres
- password: postgres
 
# ActiveMQ Resource Adapter
- ActiveMQ Version: 5.10.0
- ConnectionFactory: java:/ActiveMQConnectionFactory
- Queue: java:/queue/HELLOWORLDMDBQueue
- Topic: java:/topic/HELLOWORLDMDBTopic
- Username: admin
- Password: adminactivemq
- ServerUrl: tcp://activemq:61616