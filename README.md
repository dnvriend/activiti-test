# activiti-test
[Activiti](http://activiti.org/) is a light-weight workflow and Business Process Management (BPM) Platform targeted at 
business people, developers and system admins. 

# Usage
Start the project with `launch.sh`. Activiti should be available on `http://$(docker-machine ip dev):8080/activiti-explorer`. 
Activiti should persist to the `postgres` instance. The postgres is available runnint the `plsql-cli.sh` script.