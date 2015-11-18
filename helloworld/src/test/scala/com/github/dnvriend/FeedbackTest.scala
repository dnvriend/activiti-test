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
