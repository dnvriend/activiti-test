# activiti-test
[Activiti](http://activiti.org/) is a light-weight workflow and Business Process Management (BPM) Platform targeted at 
business people, developers and system admins. 

# Usage
Start the project with `launch.sh`. 

The Activiti Explorer application should be available at `http://$(docker-machine ip dev):8080/activiti-explorer` and 
the Activiti REST application should be available at `http://$(docker-machine ip dev)/activiti-rest/service/`.
 
Both the `Activiti explorer`, the `Activiti REST` application and the `helloworld` standalone application should persist 
to the `postgres` instance. The postgres is available running the `plsql-cli.sh` script.

# Default example processes, users and groups
Activiti will be launched in `demo` mode (`WEB-INF/classes/engine.properties`), which means that the 
[following users and groups](http://activiti.org/userguide/index.html#activiti.setup) will be automatically created by 
Activiti together with demo processes. 

The following users are available (username:password:group): 


Username | Password | Group
---------|----------|------
kermit   | kermit   | admin
gonzo    | gonzo    | manager
fozzie   | fozzie   | user

# Activiti Resources
- [User Guide for 5.19.0](http://activiti.org/userguide/index.html)
- [JavaDocs](http://www.activiti.org/javadocs/index.html)
- [Github](https://github.com/Activiti/Activiti)

# Deploying processes
- [Deploying processes](http://activiti.org/userguide/index.html#chDeployment)

# Eclipse Designer Plugin
- [Installing the Eclipse Designer Plugin](http://activiti.org/userguide/index.html#eclipseDesignerInstallation)

