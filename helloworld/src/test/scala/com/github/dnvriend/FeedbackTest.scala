package com.github.dnvriend

import org.github.dnvriend.activity.ActivitiImplicits._
import scala.collection.JavaConversions._
import org.activiti.engine.task.Task

class FeedbackTest extends TestSpec {

  def tasks: List[Task] = taskService.createTaskQuery().list().toList
  def validateTask = taskService.createTaskQuery().taskName("Validate")
  
  "Feedback" should "be correctly parsed" in {
    val deployment = repositoryService.createDeployment()
      .addClasspathResource("processes/feedback.bpmn20.xml")
      .doDeploy

    deployment should be a 'success

    val startProcessOperation = runtimeService.startProcessByKey("feedback")
    startProcessOperation should be a 'success

    deployment.foreach { deployment ⇒
      startProcessOperation.foreach { processInstance ⇒

        val initialTask = validateTask.singleResult

        initialTask.isSuspended should be(false)
        val initialForm = initialTask.getFormKey

        formService.submitTaskFormData(initialTask.getId(), Map("approved" -> "false"))

        val retryTask = validateTask.singleResult()
        retryTask.isSuspended() should be(false)

        val retryForm = retryTask.getFormKey
        formService.submitTaskFormData(retryTask.getId(), Map("approved" -> "true"))
        
        val historicProcess = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId).list()
        historicProcess should not be 'empty
      }
    }
  }
}