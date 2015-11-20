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
import org.activiti.engine.history.HistoricProcessInstance
import org.activiti.engine.runtime.ProcessInstance
import org.activiti.engine.task.Task

class SimpleLineairFlowTest extends TestSpec {

  def getProcessInstance(id: String): Option[ProcessInstance] =
    runtimeService.createProcessInstanceQuery().processInstanceId(id).single

  def getTask(id: String): Option[Task] =
    taskService.createTaskQuery().processInstanceId(id).single

  def getHistory(id: String): Option[HistoricProcessInstance] =
    historyService.createHistoricProcessInstanceQuery().processInstanceId(id).single

  "SimpleLineairFlow" should "push process through steps A, B and C" in {
    val deploymentOperation = deploy("simple-lineair-flow.bpmn20.xml")
    deploymentOperation should be a 'success

    val startProcessOperation = runtimeService.startProcessByKey("SimpleLineairFlow")
    startProcessOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      startProcessOperation.foreach { instance ⇒

        // because the process has been started, there should be a history entry,
        // but the end time should not be set, because the process has not been completed.
        getHistory(instance.getId).value.endTime shouldBe 'empty

        // Also, a process instance should be found, because the process has not been completed
        getProcessInstance(instance.getId) should not be 'empty

        // the first call to getTask will return taskA that must be completed
        val taskAOption = getTask(instance.getId)
        taskAOption should not be 'empty
        taskAOption.foreach { task ⇒
          task.getName shouldBe "Task A"
          task.getTaskDefinitionKey shouldBe "userTaskA"
          task.getProcessInstanceId shouldBe instance.getId
          println("Completing taskA")
          taskService.completeTask(task.getId) should be a 'success
        }

        // after taskA has been completed, the next call to getTask will return taskB
        val taskBOption = getTask(instance.getId)
        taskBOption should not be 'empty
        taskBOption.foreach { task ⇒
          task.getName shouldBe "Task B"
          task.getTaskDefinitionKey shouldBe "userTaskB"
          task.getProcessInstanceId shouldBe instance.getId
          println("Completing taskB")
          taskService.completeTask(task.getId) should be a 'success
        }

        // after taskB has been completed, the next call to getTask will return taskC
        val taskCOption = getTask(instance.getId)
        taskCOption should not be 'empty
        taskCOption.foreach { task ⇒
          task.getName shouldBe "Task C"
          task.getTaskDefinitionKey shouldBe "userTaskC"
          task.getProcessInstanceId shouldBe instance.getId
          println("Completing taskC")
          taskService.completeTask(task.getId) should be a 'success
        }

        // after taskC has been completed, the next call to getTask will return no more tasks
        getTask(instance.getId) shouldBe 'empty

        // Also, querying for the processInstance using the runtime service returns no result for the
        // processInstance.
        getProcessInstance(instance.getId) shouldBe 'empty

        // there should be a history entry, with end time set, because the process has been completed
        getHistory(instance.getId).value.endTime should not be 'empty
      }

      // delete the deployment
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
      println("Sleeping")
      Thread.sleep(10000)
    }
  }
}
