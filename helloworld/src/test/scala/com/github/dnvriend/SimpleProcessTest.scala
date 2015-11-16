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

class SimpleProcessTest extends TestSpec {

  "SimpleTest" should "deploy a process instance" in {
    repositoryService.createDeployment().addClasspathResource("processes/bookorder.simple.bpmn20.xml").deploy()
    val processInstance = Option(runtimeService.startProcessInstanceByKey("simplebookorder"))
    processInstance should not be 'empty
    processInstance.map(_.getProcessDefinitionId)
  }
}
