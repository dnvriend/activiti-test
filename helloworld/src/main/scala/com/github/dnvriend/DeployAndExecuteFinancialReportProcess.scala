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

import org.activiti.engine.ProcessEngineConfiguration
import org.activiti.engine.identity.Group
import org.activiti.engine.task.Task
import org.github.dnvriend.activity.ActivitiImplicits._

import scala.util.Try

/**
 * Note: Be sure to add an 'accountancy' group
 */
object DeployAndExecuteFinancialReportProcess extends App {

  // create activiti process engine
  val processEngine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
    .buildProcessEngine()

  // get Activiti services
  val repositoryService = processEngine.getRepositoryService
  val runtimeService = processEngine.getRuntimeService
  val identityService = processEngine.getIdentityService
  val historyService = processEngine.getHistoryService

  val accountancy: Option[Group] = identityService.createGroupQuery().asList.find(_.getId == "accountancy")
  if (accountancy.isEmpty) {
    println("Creating accountancy group")
    val accountancyGroup = identityService.newGroup("accountancy")
    accountancyGroup.setName("Accountancy")
    accountancyGroup.setType("assignment")
    identityService.saveGroup(accountancyGroup)
  } else println("Accountancy group is already present")

  // deploy the process definition
  val deploymentOperation = repositoryService.createDeployment()
    .name("Financial Report")
    .addClasspathResource("processes/FinancialReportProcess.bpmn20.xml")
    .doDeploy

  deploymentOperation.foreach { deployment ⇒
    // start a process instance
    val startProcessOperation: Try[String] = runtimeService.startProcessByKey("financialReport").map(_.getId)
    startProcessOperation.foreach { procId ⇒
      println(s"The BPM process instance id: $procId")
      // get the task service
      val taskService = processEngine.getTaskService

      // dump tasks
      val tasksKermitUser: List[Task] = taskService.createTaskQuery().taskCandidateUser("kermit").asList
      val tasksAccountancyGroup: List[Task] = taskService.createTaskQuery().taskCandidateGroup("accountancy").asList
      println("Tasks for kermit:")
      tasksKermitUser.foreach(task ⇒ println(task.dump))
      println("Tasks for Accountancy:")
      tasksAccountancyGroup.foreach(task ⇒ println(task.dump))

      // when there is a task, fozzie will claim the first one
      tasksAccountancyGroup.foreach { task ⇒
        println(s"Fozzie claims task: " + task.toString)
        taskService.claim(task.getId, "fozzie")
      }

      println("Tasks assigned to Fozzie:")
      val tasksAssignedToFozzie: List[Task] = taskService.createTaskQuery().taskAssignee("fozzie").asList
      tasksAssignedToFozzie.foreach(task ⇒ println(task.dump))

      // complete only the first task assigned to Fozzie
      tasksAssignedToFozzie.foreach { task ⇒
        println(s"Fozzie completes task: " + task.toString)
        taskService.complete(task.getId)
      }

      println("Number of tasks for fozzie: " + taskService.createTaskQuery().taskAssignee("fozzie").count())

      val tasksManagementGroup: List[Task] = taskService.createTaskQuery().taskCandidateGroup("management").asList
      println("Tasks for Management:")
      tasksManagementGroup.foreach(task ⇒ println(task.dump))

      // kermit claims the task that has been assigned to management
      tasksManagementGroup.foreach { task ⇒
        println(s"Kermit claims task: " + task.toString)
        taskService.claim(task.getId, "kermit")
      }

      // kermit completes all tasks assigned to him
      val tasksAssignedToKermit: List[Task] = taskService.createTaskQuery().taskAssignee("kermit").asList
      tasksAssignedToKermit.foreach { task ⇒
        println(s"Kermit completes task: " + task.toString)
        taskService.complete(task.getId)
      }

      // verify that the process is actually finished
      val historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).single
      if (historicProcessInstance.nonEmpty) {
        historicProcessInstance.foreach { history ⇒
          println(s"Process instance $procId end time: ${history.getEndTime}")
        }
      } else {
        println(s"Process '$procId' does not exist or has not been completed")
      }
    }

    startProcessOperation.failed.map(_.printStackTrace())
  }

  deploymentOperation.failed.map(_.printStackTrace())
}
