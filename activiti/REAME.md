# activiti
Based upon a Wildfly 9.0.2.Final that has been configured to communicate with Postgres and ActiveMQ, it packages
Activiti 5.19.0 that has been configured to use the Postgres JNDI data source `java:jboss/datasources/PostgresDS` 
to connect to the Postgres database. 

# Activiti-explorer
The Activiti-explorer is configured by changing files in the following location: ``activiti-explorer.war/META-INF/classes/` 

The following files have been changed: `activiti-custom-context.xml` and `db.properties`.