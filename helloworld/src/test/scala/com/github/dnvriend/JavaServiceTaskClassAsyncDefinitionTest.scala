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
import org.activiti.engine.history.HistoricVariableUpdate

class JavaServiceTaskClassAsyncDefinitionTest extends TestSpec {
  "JavaServiceTaskClassDefinition" should "execute process" in {
    val deploymentOperation = repositoryService.createDeployment()
      .addClasspathResource("processes/javaservicetask-classdef-async.bpmn20.xml")
      .doDeploy

    deploymentOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒

      val processInstanceOperation = runtimeService.startProcessByKey("javaservicetask-classdef-asyc", Map("name" -> "John Doe"))
      processInstanceOperation should be a 'success

      processInstanceOperation.foreach { processInstance ⇒
        // The JobExecutor is a component that manages a couple of threads to fire timers
        // (and later also asynchronous messages). For unit testing scenarios, it is cumbersome to work with
        // multiple threads. Therefore the API allows to query for (managementService.createJobQuery)
        // and execute jobs (managementService.executeJob) through the API so that job execution can be
        // controlled from within a unit test. To avoid interference by the job executor, it can be turned off.
        managementService.createJobQuery().asList.foreach { job ⇒
          // launch the pending job
          managementService.executeJob(job.getId)
        }

        eventually {
          historyService.createHistoricDetailQuery()
            .variableUpdates()
            .asList
            .collect {
              case hist: HistoricVariableUpdate ⇒ hist
            }.map(_.getVariableName) should contain allOf ("msg", "name")
        }
      }
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }
}
