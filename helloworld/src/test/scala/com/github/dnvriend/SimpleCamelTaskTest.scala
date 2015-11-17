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

import org.github.dnvriend.activity.ActivitiImplicits._

class SimpleCamelTaskTest extends TestSpec {
  "Activiti" should "support camel and activemq integration" in {
    val deploymentOperation = repositoryService.createDeployment()
      .addClasspathResource("processes/cameltask.bpmn20.xml")
      .doDeploy

    val variableMap: Map[String, Object] = Map("playroundId" -> "123456")

    deploymentOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      val processInstance = runtimeService.startProcessByKey("SimpleCamelCallProcess", variableMap)
      processInstance should be a 'success
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
  }
}
