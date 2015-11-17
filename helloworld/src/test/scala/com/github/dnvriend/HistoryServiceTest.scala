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

import org.github.dnvriend.activity.ActivitiImplicits._

class HistoryServiceTest extends TestSpec {

  "HistoryService" should "show history" in {
    val deploymentOperation = repositoryService.createDeployment()
      .addClasspathResource("processes/simpletest.bpmn20.xml")
      .name("simpletest")
      .doDeploy

    deploymentOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      identityService.authenticateUserId("kermit") should be a 'success
      val startProcessOperation = runtimeService.startProcessByKey("simpletest")
      startProcessOperation should be a 'success
      startProcessOperation.foreach { processInstance ⇒
        historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId).asList should not be 'empty
        repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
      }
    }
  }
}
