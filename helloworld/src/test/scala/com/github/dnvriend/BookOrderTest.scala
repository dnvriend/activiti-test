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

import org.activiti.engine.task.Task

import scala.util.Try

class BookOrderTest extends TestSpec {

  def dumpTask(task: Task): Unit = println("- " + task.toString + " - processInstanceId=" + task.getProcessInstanceId)

  "BookOrder" should "should complete process" in {
    import scala.collection.JavaConversions._
    // deploy process
    repositoryService.createDeployment().addClasspathResource("processes/bookorder.simple.bpmn20.xml").deploy()

    // set user kermit
    Try(identityService.setAuthenticatedUserId("kermit")) should be a 'success

    // create a new process instance, start the process with the ISBN of '123456'
    val variableMap = Map("isbn" -> "123456")
    val processInstance = Option(runtimeService.startProcessInstanceByKey("bookorder", variableMap))
    processInstance should not be 'empty
    val procId = processInstance.get.getId

    // get the tasks for the group sales
    val taskList = taskService.createTaskQuery().taskCandidateGroup("sales").list()
    if (taskList.isEmpty) println("There are not tasks for sales") else {
      println("Tasks for sales:")
      taskList.foreach(dumpTask)
    }

    taskList should not be 'empty
    taskService.complete(taskList.head.getId)
    val historicProcessInstance = Option(historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult())
    historicProcessInstance should not be 'empty
  }
}
