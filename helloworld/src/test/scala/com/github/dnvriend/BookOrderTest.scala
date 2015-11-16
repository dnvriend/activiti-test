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

import org.activiti.engine.history.HistoricProcessInstance
import org.activiti.engine.runtime.ProcessInstance
import org.activiti.engine.task.Task
import org.github.dnvriend.activity.ActivityImplicits._

import scala.util.Try

class BookOrderTest extends TestSpec {

  "BookOrder" should "should complete process" in {
    // deploy process
    val deploymentOption = repositoryService.createDeployment()
      .addClasspathResource("processes/bookorder.simple.bpmn20.xml")
      .doDeploy

    deploymentOption should be a 'success

    deploymentOption.foreach { deployment ⇒
      // set user kermit
      identityService.authenticateUserId("kermit") should be a 'success

      // create a new process instance, start the process with the ISBN of '123456'
      val processInstance: Try[ProcessInstance] = runtimeService.startProcessByKey("bookorder", Map("isbn" -> "123456"))
      processInstance should be a 'success
      val procId = processInstance.get.getId

      // get the tasks for the group sales
      val taskList: List[Task] = taskService.createTaskQuery().taskCandidateGroup("sales").asList
      taskList should not be 'empty
      taskService.completeTask(taskList.head.getId) should be a 'success
      val historyOption: Option[HistoricProcessInstance] = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).single
      historyOption should not be 'empty
      repositoryService.deleteProcess(deployment.id, cascade = true) should be a 'success
    }
  }
}
