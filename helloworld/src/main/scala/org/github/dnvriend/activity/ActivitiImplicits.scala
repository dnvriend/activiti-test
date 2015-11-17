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

import org.activiti.engine.delegate.DelegateExecution
import org.activiti.engine.history.{HistoricVariableUpdate, HistoricDetail, HistoricProcessInstance}
import org.activiti.engine.identity.{Group, User}
import org.activiti.engine.query.Query
import org.activiti.engine.repository.{Deployment, DeploymentBuilder}
import org.activiti.engine.runtime.{Execution, ProcessInstance}
import org.activiti.engine.task.Task
import org.activiti.engine.{RepositoryService, IdentityService, RuntimeService, TaskService}

import scala.collection.JavaConversions._
import scala.util.Try

object ActivitiImplicits {

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

  implicit class RepositoryServiceImplicits(val service: RepositoryService) extends AnyVal {
    def deleteDeploymentById(deploymentId: Long): Try[Unit] = Try(service.deleteDeployment(deploymentId.toString))
    def deleteDeploymentById(deploymentId: Long, cascade: Boolean): Try[Unit] = Try(service.deleteDeployment(deploymentId.toString, cascade))
  }

  implicit class DeploymentBuilderImplicits(val builder: DeploymentBuilder) extends AnyVal {
    def doDeploy: Try[Deployment] = Try(builder.deploy)
  }

  implicit class RuntimeServiceImplicits(val service: RuntimeService) extends AnyVal {

    def getVar(executionId: String, variableName: String): Try[AnyRef] =
      Try(service.getVariable(executionId, variableName))

    def getVarAs[A](executionId: String, variableName: String): Try[A] =
      Try(service.getVariable(executionId, variableName).asInstanceOf[A])

    /**
      * All variables visible from the given execution scope (including parent scopes).
      */
    def getVars(executionId: String): Try[Map[String, AnyRef]] = Try(service.getVariables(executionId).toMap)

    /**
      * Starts a new process instance in the latest version of the process
      * definition with the given key.
      */
    def startProcessByKey(key: String): Try[ProcessInstance] = Try(service.startProcessInstanceByKey(key))
    def startProcessById(processDefinitionId: String): Try[ProcessInstance] = Try(service.startProcessInstanceById(processDefinitionId))

    def startProcessByKey(processDefinitionKey: String, variables: Map[String, AnyRef]): Try[ProcessInstance] =
      Try(service.startProcessInstanceByKey(processDefinitionKey, variables))
  }

  implicit class IdentityServiceImplicits(val service: IdentityService) extends AnyVal {
    /**
      * Sets the process initiator. Passes the authenticated user id for
      * this particular thread. All service method (from any service)
      * invocations done by the same thread will have access to this
      * authenticatedUserId.
      */
    def authenticateUserId(authenticatedUserId: String): Try[Unit] =
      Try(service.setAuthenticatedUserId(authenticatedUserId))

    /**
      * Saves the user. If the user already existed, the user is updated.
      */
    def save(user: User): Try[Unit] = Try(service.saveUser(user))

    /**
      * Saves the group. If the group already existed, the group is updated.
      */
    def save(group: Group): Try[Unit] = Try(service.saveGroup(group))

    def membership(userId: String, groupId: String): Try[Unit] = Try(service.createMembership(userId, groupId))
  }

  implicit class TaskServiceImplicits(val service: TaskService) extends AnyVal {
    /**
      * Called when the task is successfully executed.
      */
    def completeTask(taskId: String): Try[Unit] = Try(service.complete(taskId))
  }

  implicit class ProcessInstanceImplicits(val process: ProcessInstance) extends AnyVal {
    def processVariables: Map[String, AnyRef] = process.getProcessVariables.toMap

    def dump: String = {
      import process._
      s"""
         |ProcessInstance(
         |id=$getId,
         |suspended=$isSuspended,
         |ended=$isEnded,
         |activityId=$getActivityId,
         |processInstanceId=$getProcessInstanceId,
         |parentId=$getParentId,
         |tentantId=$getTenantId,
         |processDefinitionId=$getProcessDefinitionId,
         |processDefinitionName=$getProcessDefinitionName,
         |processDefinitionKey=$getProcessDefinitionKey,
         |processDefinitionVersion=$getProcessDefinitionVersion,
         |deploymentId=$getDeploymentId,
         |businessKey=$getBusinessKey,
         |processVariables=${getProcessVariables.toMap},
         |name=$getName
         |)
       """.stripMargin
    }
  }

  implicit class DelegateExecutionImplicits(val execution: DelegateExecution) extends AnyVal {
    def get(variableName: String): Option[AnyRef] = Option(execution.getVariable(variableName))
    def set(variableName: String, value: AnyRef): DelegateExecution = {
      execution.setVariable(variableName, value)
      execution
    }

    def dump: String = {
      import execution._
      s"""
         |DelegateExecution(
         |id=$getId,
         |processInstanceId=$getProcessInstanceId,
         |eventName=$getEventName,
         |businessKey=$getBusinessKey,
         |processBusinessKey=$getProcessBusinessKey,
         |processDefinitionId=$getProcessDefinitionId,
         |parentId=$getParentId,
         |superExecutionId=$getSuperExecutionId,
         |currentActivityId=$getCurrentActivityId,
         |currentAcivityName=$getCurrentActivityName,
         |tentantId=$getTenantId,
         |variables=${getVariables.toMap},
         |)
       """.stripMargin
    }
  }

  implicit class UserImplicits(val user: User) extends AnyVal {
    def dump: String = {
      import user._
      s"""
        |User(
        |id=$getId,
        |firstName=$getFirstName,
        |lastName=$getLastName,
        |email=$getEmail,
        |password=$getPassword,
        |pictureSet=$isPictureSet
        |)
      """.stripMargin
    }
  }

  implicit class GroupImplicits(val group: Group) extends AnyVal {
    def dump: String = {
      import group._
      s"""
         |Group(
         |id=$getId,
         |name=$getName,
         |type=$getType
         |)
       """.stripMargin
    }
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

  implicit class HistoricDetailImplicits(val history: HistoricDetail) extends AnyVal {
    def dump: String = {
      import history._
      s"""
         |HistoricDetail(
         |id=$getId,
         |processInstanceid=$getProcessInstanceId,
         |activityInstanceId=$getActivityInstanceId,
         |executionId=$getExecutionId,
         |taskId=$getTaskId,
         |time=$getTime
         |)
       """.stripMargin
    }
  }

  implicit class HistoricVariableUpdateImplicits(val history: HistoricVariableUpdate) extends AnyVal {
    def dump: String = {
      import history._
      s"""
         |HistoricVariableUpdate(
         |id=$getId,
         |processInstanceid=$getProcessInstanceId,
         |activityInstanceId=$getActivityInstanceId,
         |executionId=$getExecutionId,
         |taskId=$getTaskId,
         |time=$getTime,
         |variableName=$getVariableName,
         |variableTypeName=$getVariableTypeName,
         |value=$getValue,
         |revision=$getRevision
         |)
       """.stripMargin
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

  implicit class DeploymentImplicits(val deployment: Deployment) extends AnyVal {
    def id: Long = deployment.getId.toLong
    def dump: String = {
      import deployment._
      s"""
         |Deployment(
         |id=$getId,
         |name=$getName,
         |deploymentTime=$getDeploymentTime,
         |category=$getCategory,
         |tenantId=$getTenantId
         |)
       """.stripMargin
    }
  }

  /**
    * An Execution Represent a 'path of execution' in a process instance.
    * Note that a ProcessInstance also is an execution.
    */
  implicit class ExecutionImplicits(val execution: Execution) extends AnyVal {
    def dump: String = {
      import execution._
      s"""
         |Execution(
         |id=$getId,
         |suspended=$isSuspended,
         |ended=$isEnded,
         |activityId=$getActivityId,
         |processInstanceId=$getProcessInstanceId,
         |parentId=$getParentId,
         |tentantId=$getTenantId
         |)
       """.stripMargin
    }
  }
}
