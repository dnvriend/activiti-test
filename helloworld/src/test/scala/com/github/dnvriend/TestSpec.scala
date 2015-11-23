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

import com.github.dnvriend.activiti.ActivitiImplicits.{ DeploymentBuilderImplicits, _ }
import com.github.dnvriend.activiti.ActivitiService
import com.github.dnvriend.camel.CamelImplicits._
import org.activiti.engine.history.HistoricProcessInstance
import org.activiti.engine.repository.Deployment
import org.activiti.engine.runtime.ProcessInstance
import org.activiti.engine.task.Task
import org.apache.camel.ServiceStatus
import org.scalatest._
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.Span.convertDurationToSpan
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.scalatest.FlatSpecLike

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.duration.DurationInt
import scala.util.Try

trait TestSpec extends FlatSpecLike with Matchers with TryValues with OptionValues with Eventually with ScalaFutures with BeforeAndAfterAll with BeforeAndAfterEach with ActivitiService {

  implicit val p = PatienceConfig(timeout = 50.seconds)

  val springContext: ApplicationContext = new ClassPathXmlApplicationContext("/spring/spring-beans.xml")

  def startRoute(routeId: String): Try[ServiceStatus] = camelContext.start(routeId)

  def stopRoute(routeId: String): Try[ServiceStatus] = camelContext.stop(routeId)

  /**
   * Deploys a classPathResource relative to the 'processes' directory
   */
  def deploy(classPathResourceName: String): Try[Deployment] =
    repositoryService.createDeployment()
      .addClasspathResource(s"processes/$classPathResourceName")
      .doDeploy

  def getProcessInstance(id: String): Option[ProcessInstance] =
    runtimeService.createProcessInstanceQuery().processInstanceId(id).single

  def getTask(id: String): Option[Task] =
    taskService.createTaskQuery().processInstanceId(id).single

  def getHistory(id: String): Option[HistoricProcessInstance] =
    historyService.createHistoricProcessInstanceQuery().processInstanceId(id).single

  def sendBodyAndHeaders(endpointUrl: String, body: String = "", headers: Map[String, AnyRef] = Map()): Try[Unit] =
    Try(producerTemplate.sendBodyAndHeaders(endpointUrl, body, headers.asJava))

  def requestBodyAndHeaders(endpointUrl: String, body: String, headers: Map[String, AnyRef]): Try[Unit] =
    Try(producerTemplate.requestBodyAndHeaders(endpointUrl, body, headers.asJava))
}