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

import java.text.SimpleDateFormat
import java.util.Date

import org.activiti.engine.ProcessEngineConfiguration
import org.activiti.engine.task.Task

import scala.util.Try

/**
 * Note: Be sure to add an 'accountancy' group
 */
object DeployAndExecuteFinancialReportProcess extends App {

  implicit class DateFormat(val date: Date) extends AnyVal {
    def format: Option[String] = Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(date)).toOption
  }

  def dumpTask(task: Task): Unit = println("- " + task.toString + " - processInstanceId=" + task.getProcessInstanceId)

  // create activiti process engine
  val processEngine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
    .buildProcessEngine()

  // get Activiti services
  val repositoryService = processEngine.getRepositoryService
  val runtimeService = processEngine.getRuntimeService

  import scala.collection.JavaConversions._
  val identityService = processEngine.getIdentityService
  val accountancy = identityService.createGroupQuery().list().find(_.getId == "accountancy")
  if (accountancy.isEmpty) {
    println("Creating accountancy group")
    val accountancyGroup = identityService.newGroup("accountancy")
    accountancyGroup.setName("Accountancy")
    accountancyGroup.setType("assignment")
    identityService.saveGroup(accountancyGroup)
  } else println("Accountancy group is already present")

  // deploy the process definition
  repositoryService.createDeployment()
    .name("Financial Report")
    .addClasspathResource("processes/FinancialReportProcess.bpmn20.xml")
    .deploy()

  // start a process instance
  val procId = runtimeService.startProcessInstanceByKey("financialReport").getId
  println(s"The BPM process instance id: $procId")

  // get the task service
  val taskService = processEngine.getTaskService

  // dump tasks
  val tasksKermitUser = taskService.createTaskQuery().taskCandidateUser("kermit").list()
  val tasksAccountancyGroup = taskService.createTaskQuery().taskCandidateGroup("accountancy").list()
  println("Tasks for kermit:")
  tasksKermitUser.foreach(dumpTask)
  println("Tasks for Accountancy:")
  tasksAccountancyGroup.foreach(dumpTask)

  // when there is a task, fozzie will claim the first one
  tasksAccountancyGroup.foreach { task ⇒
    println(s"Fozzie claims task: " + task.toString)
    taskService.claim(task.getId, "fozzie")
  }

  println("Tasks assigned to Fozzie:")
  val tasksAssignedToFozzie = taskService.createTaskQuery().taskAssignee("fozzie").list()
  tasksAssignedToFozzie.foreach(dumpTask)

  // complete only the first task assigned to Fozzie
  tasksAssignedToFozzie.foreach { task ⇒
    println(s"Fozzie completes task: " + task.toString)
    taskService.complete(task.getId)
  }

  println("Number of tasks for fozzie: " + taskService.createTaskQuery().taskAssignee("fozzie").count())

  val tasksManagementGroup = taskService.createTaskQuery().taskCandidateGroup("management").list()
  println("Tasks for Management:")
  tasksManagementGroup.foreach(dumpTask)

  // kermit claims the task that has been assigned to management
  tasksManagementGroup.foreach { task ⇒
    println(s"Kermit claims task: " + task.toString)
    taskService.claim(task.getId, "kermit")
  }

  // kermit completes all tasks assigned to him
  val tasksAssignedToKermit = taskService.createTaskQuery().taskAssignee("kermit").list()
  tasksAssignedToKermit.foreach { task ⇒
    println(s"Kermit completes task: " + task.toString)
    taskService.complete(task.getId)
  }

  // verify that the process is actually finished
  val historyService = processEngine.getHistoryService
  val historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult()
  val endDateOption = Option(historicProcessInstance).flatMap(_.getEndTime.format)
  if (endDateOption.nonEmpty) {
    endDateOption.foreach { endTime ⇒
      println(s"Process instance $procId end time: $endTime")
    }
  } else {
    println(s"Process '$procId' does not exist or has not been completed")
  }
}
