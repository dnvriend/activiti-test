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

import org.activiti.engine.history.{ HistoricDetail, HistoricVariableUpdate }
import org.github.dnvriend.activity.ActivitiImplicits._

import scala.collection.JavaConversions._

class SuperProcessTest extends TestSpec {

  "Superprocess" should "include its subprocesses" in {
    val subDeploy = repositoryService.createDeployment()
      .addClasspathResource("processes/subprocess.bpmn20.xml")
      .name("subprocess")
      .doDeploy

    subDeploy should be a 'success

    val superDeploy = repositoryService.createDeployment()
      .addClasspathResource("processes/superprocess.bpmn20.xml")
      .name("superProcessTest")
      .doDeploy

    superDeploy should be a 'success

    val variableMap: Map[String, Object] = Map("text" -> "-")

    val startProcessOperation = runtimeService.startProcessByKey("superProcessTest", variableMap)
    startProcessOperation should be a 'success

    superDeploy.foreach { deployment ⇒
      startProcessOperation.foreach { processInstance ⇒
        historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId).asList should not be 'empty

        val textUpdates: List[HistoricDetail] = historyService.createHistoricDetailQuery().variableUpdates().list.toList

        textUpdates should not be 'empty

        val varUpdates = textUpdates.map(_.asInstanceOf[HistoricVariableUpdate])
        val lastValue = varUpdates.last.getValue.asInstanceOf[String]

        lastValue should startWith("subprocess")
        lastValue should endWith("superprocess")
      }
    }
  }
}
