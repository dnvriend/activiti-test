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

import org.activiti.engine.ProcessEngineConfiguration
import org.github.dnvriend.activity.ActivitiImplicits._

object ProcessHistory extends App {

  val procId = "17504"
  // create activiti process engine
  val processEngine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
    .buildProcessEngine()

  // verify that the process is actually finished
  val historyService = processEngine.getHistoryService
  val historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).single
  if (historicProcessInstance.nonEmpty) {
    historicProcessInstance.foreach { history ⇒
      println(s"Process instance $procId end time: ${history.getEndTime}")
    }
  } else {
    println(s"Process '$procId' does not exist or has not been completed")
  }
}
