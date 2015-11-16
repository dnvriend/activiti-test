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

package org.github.dnvriend.activity

import org.activiti.engine.history.HistoricProcessInstance
import org.activiti.engine.task.Task
import org.activiti.engine.{TaskService, IdentityService, RuntimeService}
import org.activiti.engine.query.Query
import org.activiti.engine.runtime.ProcessInstance
import scala.collection.JavaConversions._
import scala.util.Try

object ActivityImplicits {

  implicit class QueryImplicits[A <: Query[_, _], B](val query: Query[A, B]) extends AnyVal {
    /**
      * Executes the query and returns the resulting entity or None if no
      * entity matches the query criteria.
      */
    def single: Option[B] = Option(query.singleResult)

    /**
      * Executes the query and get a list of entities as the result.
      */
    def asList: List[B] = query.list().toList
  }

  implicit class RuntimeServiceImplicits(val service: RuntimeService) extends AnyVal {
    /**
      * Starts a new process instance in the latest version of the process
      * definition with the given key.
      */
    def startProcessByKey(key: String): Try[ProcessInstance] = Try(service.startProcessInstanceByKey(key))

    def startProcessByKey(processDefinitionKey: String, variables: Map[String, AnyRef]): Try[ProcessInstance] =
      Try(service.startProcessInstanceByKey(processDefinitionKey, variables))
  }

  implicit class IdentityServiceImplicits(val service: IdentityService) extends AnyVal {
    /**
      * Passes the authenticated user id for this particular thread.
      * All service method (from any service) invocations done by the same
      * thread will have access to this authenticatedUserId.
      */
    def setAuthUserId(authenticatedUserId: String): Try[Unit] =
      Try(service.setAuthenticatedUserId(authenticatedUserId))
  }

  implicit class TaskServiceImplicits(val service: TaskService) extends AnyVal {
    /**
      * Called when the task is successfully executed.
      */
    def completeTask(taskId: String): Try[Unit] = Try(service.complete(taskId))
  }

  implicit class HistoricProcessInstanceImplicits(val history: HistoricProcessInstance) extends AnyVal {
    def dump: String = {
      import history._
      s"""
        |HistoricProcessInstance(
        |id=$getId,
        |BusinessKey=$getBusinessKey,
        |processDefinitionId=$getProcessDefinitionId,
        |startTime=$getStartTime,
        |endTime=$getEndTime,
        |durationInMillis=$getDurationInMillis,
        |endActivityId=$getEndActivityId,
        |startUserId=$getStartUserId,
        |startActivityId=$getStartActivityId,
        |deleteReason=$getDeleteReason,
        |superProcessInstanceId=$getSuperProcessInstanceId,
        |tentantId=$getTenantId,
        |name=$getName,
        |processVariables=${getProcessVariables.toMap},
        |)
      """.
        stripMargin
      }
  }

  implicit class TaskImplicits(val task: Task) extends AnyVal {
    def dump: String = {
      import task._
      s"""
        |Task(
        |id=$getId,
        |name=$getName,
        |description=$getDescription,
        |priority=$getPriority,
        |owner=$getOwner,
        |assignee=$getAssignee,
        |processInstanceId=$getProcessInstanceId,
        |executionId=$getExecutionId,
        |processDefinitionId=$getProcessDefinitionId,
        |createTime=$getCreateTime,
        |taskDefinitionKey=$getTaskDefinitionKey,
        |dueDate=$getDueDate,
        |category=$getCategory,
        |parentTaskId=$getParentTaskId,
        |tentantId=$getTenantId,
        |formKey=$getFormKey,
        |taskLocalVariables=${getTaskLocalVariables.toMap},
        |processVariables=${getProcessVariables.toMap}
        |)
      """.stripMargin
    }
  }
}
