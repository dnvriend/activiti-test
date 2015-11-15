package com.github.dnvriend

import org.activiti.engine.ProcessEngineConfiguration
import org.activiti.engine.task.Task

/**
  * Note: Be sure to add an 'accountancy' group
  */
object DeployAndExecuteFinancialReportProcess extends App {

  def dumpTask(task: Task): Unit = println("- " + task.toString + " - processInstanceId=" + task.getProcessInstanceId)

  // create activiti process engine
  val processEngine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
    .buildProcessEngine()

  // get Activiti services
  val repositoryService = processEngine.getRepositoryService
  val runtimeService = processEngine.getRuntimeService

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
  import scala.collection.JavaConversions._
  val tasksKermitUser = taskService.createTaskQuery().taskCandidateUser("kermit").list()
  val tasksAccountancyGroup = taskService.createTaskQuery().taskCandidateGroup("accountancy").list()
  println("Tasks for kermit:")
  tasksKermitUser.foreach(dumpTask)
  println("Tasks for Accountancy:")
  tasksAccountancyGroup.foreach(dumpTask)

  // when there is a task, fozzie will claim the first one
  tasksAccountancyGroup.foreach { task =>
    println(s"Fozzie claims task: " + task.toString)
    taskService.claim(task.getId, "fozzie")
  }

  println("Tasks assigned to Fozzie:")
  val tasksAssignedToFozzie = taskService.createTaskQuery().taskAssignee("fozzie").list()
  tasksAssignedToFozzie.foreach(dumpTask)

  // complete only the first task assigned to Fozzie
  tasksAssignedToFozzie.foreach { task =>
    println(s"Fozzie completes task: " + task.toString)
    taskService.complete(task.getId)
  }

  println("Number of tasks for fozzie: " + taskService.createTaskQuery().taskAssignee("fozzie").count())

  val tasksManagementGroup = taskService.createTaskQuery().taskCandidateGroup("management").list()
  println("Tasks for Management:")
  tasksManagementGroup.foreach(dumpTask)

  // kermit claims the task that has been assigned to management
  tasksManagementGroup.foreach { task =>
    println(s"Kermit claims task: " + task.toString)
    taskService.claim(task.getId, "kermit")
  }

  // kermit completes all tasks assigned to him
  val tasksAssignedToKermit = taskService.createTaskQuery().taskAssignee("kermit").list()
  tasksAssignedToKermit.foreach { task =>
    println(s"Kermit completes task: " + task.toString)
    taskService.complete(task.getId)
  }

  // verify that the process is actually finished
  val historyService = processEngine.getHistoryService
  val historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult()
  println(s"Process instance $procId end time: " + historicProcessInstance.getEndTime)
}
