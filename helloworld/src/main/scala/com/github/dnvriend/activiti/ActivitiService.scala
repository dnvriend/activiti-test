package com.github.dnvriend.activiti

import org.activiti.engine.FormService
import org.activiti.engine.HistoryService
import org.activiti.engine.IdentityService
import org.activiti.engine.ManagementService
import org.activiti.engine.ProcessEngine
import org.activiti.engine.RepositoryService
import org.activiti.engine.RuntimeService
import org.activiti.engine.TaskService
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.springframework.context.ApplicationContext

import com.github.dnvriend.spring.SpringImplicits.ApplicationContextImplicits


trait ActivitiService {
  
  /**
   * Central interface to provide configuration for an application.
   * This is read-only while the application is running, but may be
   * reloaded if the implementation supports this.
   */
  val context: ApplicationContext

  /**
   * Single instance of the Activity ProcessEngine
   */
  lazy val processEngine: ProcessEngine = context.bean("processEngine")

  /**
   * Interface used to represent the context used to configure routes and the policies to use during
   * message exchanges between endpoints.
   */
  lazy val camelContext: CamelContext = context.bean("camelContext")
  
  /**
   * Template for working with Camel and sending Message instances in an Exchange to an Endpoint.
   */
  lazy val producerTemplate: ProducerTemplate = camelContext.createProducerTemplate()

  /**
   * The runtime service provides an interface to start and query process instances.
   * In addition, process variables can be retrieved and set, and processes
   * can be signaled to leave a wait state.
   */
  lazy val runtimeService: RuntimeService = processEngine.getRuntimeService

  /**
   * The repository service provides functionality to deploy, query, delete,
   * and retrieve process definitions.
   */
  lazy val repositoryService: RepositoryService = processEngine.getRepositoryService

  /**
   * The identity service allows the management (creation, update, deletion, querying, …​) of groups and users.
   * It is important to understand that Activiti actually doesn’t do any checking on users at runtime.
   * For example, a task could be assigned to any user, but the engine does not verify if that user is known
   * to the system. This is because the Activiti engine can also be used in conjunction with services such as
   * LDAP, Active Directory, etc.
   */
  lazy val identityService: IdentityService = processEngine.getIdentityService

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
  lazy val taskService: TaskService = processEngine.getTaskService

  /**
   * The HistoryService exposes all historical data gathered by the Activiti engine.
   * When executing processes, a lot of data can be kept by the engine (this is configurable)
   * such as process instance start times, who did which tasks, how long it took to complete the
   * tasks, which path was followed in each process instance, etc.
   * This service exposes mainly query capabilities to access this data.
   */
  lazy val historyService: HistoryService = processEngine.getHistoryService

  /**
   * The FormService is an optional service. Meaning that Activiti can perfectly be used without it,
   * without sacrificing any functionality. This service introduces the concept of a start form and a task
   * form. A start form is a form that is shown to the user before the process instance is started, while a task
   * form is the form that is displayed when a user wants to complete a form.
   */
  lazy val formService: FormService = processEngine.getFormService

  /**
   * The ManagementService is typically not needed when coding custom application using Activiti.
   * It allows to retrieve information about the database tables and table metadata. Furthermore,
   * it exposes query capabilities and management operations for jobs. Jobs are used in Activiti
   * for various things such as timers, asynchronous continuations, delayed suspension/activation.
   */
  lazy val managementService: ManagementService = processEngine.getManagementService
}
