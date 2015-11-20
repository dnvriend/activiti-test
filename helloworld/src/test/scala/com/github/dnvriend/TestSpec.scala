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

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.duration.DurationInt
import scala.util.Try
import org.activiti.engine.repository.Deployment
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.TryValues
import org.scalatest.concurrent.Eventually
import org.scalatest.time.Span.convertDurationToSpan
import com.github.dnvriend.activiti.ActivitiImplicits.DeploymentBuilderImplicits
import com.github.dnvriend.activiti.ActivitiService
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

trait TestSpec extends FlatSpec with Matchers with TryValues with OptionValues with Eventually with ActivitiService {

  implicit val p = PatienceConfig(timeout = 50.seconds)

  val context: ApplicationContext = new ClassPathXmlApplicationContext("/spring/spring-beans.xml")

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