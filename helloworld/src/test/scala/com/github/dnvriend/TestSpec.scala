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
import com.github.dnvriend.spring.SpringImplicits._
import org.activiti.engine._
import org.activiti.engine.repository.Deployment
import org.apache.camel.{ CamelContext, ProducerTemplate }
import org.scalatest.concurrent.Eventually
import org.scalatest.{ OptionValues, FlatSpec, Matchers, TryValues }
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.Try

object Activity {
  /**
   * Central interface to provide configuration for an application.
   * This is read-only while the application is running, but may be
   * reloaded if the implementation supports this.
   */
  val context: ApplicationContext = new ClassPathXmlApplicationContext("/spring/spring-beans.xml")

  /**
   * Single instance of the Activity ProcessEngine
   */
  val processEngine: ProcessEngine = context.bean("processEngine")

  /**
   * Interface used to represent the context used to configure routes and the policies to use during
   * message exchanges between endpoints.
   */
  val camelContext: CamelContext = context.bean("camelContext")
}

trait TestSpec extends FlatSpec with Matchers with TryValues with OptionValues with Eventually {

  implicit val p = PatienceConfig(timeout = 50.seconds)

  val camelContext: CamelContext = Activity.camelContext

  /**
   * Template for working with Camel and sending Message instances in an Exchange to an Endpoint.
   */
  val producerTemplate: ProducerTemplate = camelContext.createProducerTemplate()

  /**
   * The runtime service provides an interface to start and query process instances.
   * In addition, process variables can be retrieved and set, and processes
   * can be signaled to leave a wait state.
   */
  val runtimeService: RuntimeService = Activity.processEngine.getRuntimeService

  /**
   * The repository service provides functionality to deploy, query, delete,
   * and retrieve process definitions.
   */
  val repositoryService: RepositoryService = Activity.processEngine.getRepositoryService

  /**
   * The identity service allows the management (creation, update, deletion, querying, …​) of groups and users.
   * It is important to understand that Activiti actually doesn’t do any checking on users at runtime.
   * For example, a task could be assigned to any user, but the engine does not verify if that user is known
   * to the system. This is because the Activiti engine can also be used in conjunction with services such as
   * LDAP, Active Directory, etc.
   */
  val identityService: IdentityService = Activity.processEngine.getIdentityService

  /**
   * With the task service you can do things with user tasks.
   * For example, you can create a new task and query Activiti
   * for a list of tasks that a specific user can claim.
   *
   * Tasks that need to be performed by actual human users of the system are core to a
   * BPM engine. Everything around tasks is grouped in the TaskService, such as
   * <ul>
   *   <li>Querying tasks assigned to users or groups</li>
   *   <li>Creating new standalone tasks. These are tasks that are not related to a process instances.</li>
   *   <li>Manipulating to which user a task is assigned or which users are in some way involved with the task.</li>
   *    <li>Claiming and completing a task. Claiming means that someone decided to be the assignee for the task,
   *        meaning that this user will complete the task. Completing means doing the work of the tasks.
   *       Typically this is filling in a form of sorts.</li>
   * </ul>
   */
  val taskService: TaskService = Activity.processEngine.getTaskService

  /**
   * The HistoryService exposes all historical data gathered by the Activiti engine.
   * When executing processes, a lot of data can be kept by the engine (this is configurable)
   * such as process instance start times, who did which tasks, how long it took to complete the
   * tasks, which path was followed in each process instance, etc.
   * This service exposes mainly query capabilities to access this data.
   */
  val historyService: HistoryService = Activity.processEngine.getHistoryService

  /**
   * The FormService is an optional service. Meaning that Activiti can perfectly be used without it,
   * without sacrificing any functionality. This service introduces the concept of a start form and a task
   * form. A start form is a form that is shown to the user before the process instance is started, while a task
   * form is the form that is displayed when a user wants to complete a form.
   */
  val formService: FormService = Activity.processEngine.getFormService

  /**
   * The ManagementService is typically not needed when coding custom application using Activiti.
   * It allows to retrieve information about the database tables and table metadata. Furthermore,
   * it exposes query capabilities and management operations for jobs. Jobs are used in Activiti
   * for various things such as timers, asynchronous continuations, delayed suspension/activation.
   */
  val managementService: ManagementService = Activity.processEngine.getManagementService

  /**
   * Deploys a classPathResource relative to the 'processes' directory
   */
  def deploy(classPathResourceName: String): Try[Deployment] =
    repositoryService.createDeployment()
      .addClasspathResource(s"processes/$classPathResourceName")
      .doDeploy

  def sendBodyAndHeaders(endpointUrl: String, body: String = "", headers: Map[String, AnyRef] = Map()): Try[Unit] =
    Try(producerTemplate.sendBodyAndHeaders(endpointUrl, body, headers.asJava))

  def requestBodyAndHeaders(endpointUrl: String, body: String, headers: Map[String, AnyRef]): Try[Unit] =
    Try(producerTemplate.requestBodyAndHeaders(endpointUrl, body, headers.asJava))
}

trait RichTestSpec extends TestSpec {
  
  import org.github.dnvriend.activity.ActivitiImplicits._
  
  def deploy(processDefPath: String) = 
    repositoryService.createDeployment().addClasspathResource(processDefPath).doDeploy
  
}
