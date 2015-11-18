/*
 * Copyright 2015 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import com.github.dnvriend.activiti.ActivitiImplicits._

class SimpleCamelTaskTest extends TestSpec {

  final val JSON = """{"ACTIVITI_PROCESS_KEY":"javaservicetask-classdef","ACTIVITI_EXECUTION_ID":"4","ACTIVITI_PROCESS_DEFINITION_ID":"javaservicetask-classdef:1:3","ACTIVITI_PROCESS_INSTANCE_ID":"4","playroundId":"123456"}"""

  "Activiti" should "support camel and activemq integration" in {
    val deploymentOperation = deploy("cameltask.bpmn20.xml")
    deploymentOperation should be a 'success

    val variableMap: Map[String, Object] = Map("playroundId" -> "123456")
    val startProcessOperation = runtimeService.startProcessByKey("SimpleCamelCallProcess", variableMap)
    startProcessOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }

  "camel" should "launch an activiti process with no bells and whistles" in {
    val deploymentOperation = deploy("javaservicetask-classdef.bpmn20.xml")
    deploymentOperation should be a 'success

    sendBodyAndHeaders("direct:startProcessFirst") should be a 'success

    deploymentOperation.foreach { deployment ⇒
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }

  it should "launch an activiti process with a process initiator" in {
    val deploymentOperation = deploy("javaservicetask-classdef.bpmn20.xml")
    deploymentOperation should be a 'success

    sendBodyAndHeaders("direct:startProcessSecond") should be a 'success

    deploymentOperation.foreach { deployment ⇒
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }

  it should "launch an activiti process with JSON body parsed and exchange properties set " in {
    val deploymentOperation = deploy("javaservicetask-classdef.bpmn20.xml")
    deploymentOperation should be a 'success

    sendBodyAndHeaders("direct:startProcessThird", JSON) should be a 'success

    deploymentOperation.foreach { deployment ⇒
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }
}
