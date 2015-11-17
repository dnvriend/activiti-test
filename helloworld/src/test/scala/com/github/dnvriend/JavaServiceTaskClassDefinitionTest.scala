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

import org.activiti.engine.history.HistoricVariableUpdate
import org.github.dnvriend.activity.ActivitiImplicits._

class JavaServiceTaskClassDefinitionTest extends TestSpec {
  "JavaServiceTaskClassDefinition" should "execute process" in {
    val deploymentOperation = repositoryService.createDeployment()
      .addClasspathResource("processes/javaservicetask-classdef.bpmn20.xml")
      .doDeploy

    deploymentOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒

      val processInstanceOperation = runtimeService.startProcessByKey("javaservicetask-classdef", Map("name" -> "John Doe"))
      processInstanceOperation should be a 'success

      // the process has finished, only the historic variables can be requested.
      processInstanceOperation.foreach { processInstance ⇒
        historyService.createHistoricDetailQuery()
          .variableUpdates()
          .asList
          .collect {
            case hist: HistoricVariableUpdate ⇒ hist
          }.map(_.getVariableName) should contain allOf ("msg", "name")
      }
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }
}
