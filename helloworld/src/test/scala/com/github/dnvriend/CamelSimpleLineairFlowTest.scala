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

class CamelSimpleLineairFlowTest extends TestSpec {

  "CamelSimpleLineairFlowTest" should "push process through steps A, B and C" in {

    val deploymentOperation = deploy("simple-lineair-flow.bpmn20.xml")
    deploymentOperation should be a 'success

    val startProcessOperation = runtimeService.startProcessByKey("SimpleLineairFlow")
    startProcessOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      startProcessOperation.foreach { instance ⇒

        eventually {
          // after taskC has been completed, the next call to getTask will return no more tasks
          getTask(instance.getId) shouldBe 'empty

          // Also, querying for the processInstance using the runtime service returns no result for the
          // processInstance.
          getProcessInstance(instance.getId) shouldBe 'empty

          // there should be a history entry, with end time set, because the process has been completed
          getHistory(instance.getId).value.endTime should not be 'empty
        }
      }

      // delete the deployment
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }

  override protected def beforeAll(): Unit = {
    startRoute("ConsumerCompleteTaskA").success.value shouldBe 'started
    startRoute("ConsumerCompleteTaskB").success.value shouldBe 'started
    startRoute("ConsumerCompleteTaskC").success.value shouldBe 'started
  }

  override protected def afterAll(): Unit = {
    stopRoute("ConsumerCompleteTaskA").success.value shouldBe 'stopped
    stopRoute("ConsumerCompleteTaskB").success.value shouldBe 'stopped
    stopRoute("ConsumerCompleteTaskC").success.value shouldBe 'stopped
  }
}
