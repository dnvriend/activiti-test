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

import org.activiti.engine.runtime.ProcessInstance
import org.github.dnvriend.activity.ActivitiImplicits._

import scala.util.Try

class JavaServiceTaskClassDefinitionTest extends TestSpec {
  "JavaServiceTaskClassDefinition" should "execute process" in {
    val deploymentOperation = repositoryService.createDeployment()
      .addClasspathResource("processes/javaservicetask-classdef.bpmn20.xml")
      .doDeploy
    deploymentOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      val processInstance: Try[ProcessInstance] = runtimeService.startProcessByKey("javaservicetask-classdef", Map("name" -> "John Doe"))
      processInstance should be a 'success
      repositoryService.deleteProcess(deployment.id, cascade = true) should be a 'success
    }
  }
}
