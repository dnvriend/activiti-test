FROM dnvriend/wildfly:9.0.2.Final

ADD ./activiti-explorer.war /opt/jboss/wildfly/standalone/deployments/activiti-explorer.war
ADD ./activiti-rest.war /opt/jboss/wildfly/standalone/deployments/activiti-rest.war
RUN touch /opt/jboss/wildfly/standalone/deployments/activiti-explorer.war.dodeploy && \
touch /opt/jboss/wildfly/standalone/deployments/activiti-rest.war.dodeploy

USER root

RUN chown -R jboss:jboss /opt/jboss/wildfly/standalone/deployments

USER jboss
