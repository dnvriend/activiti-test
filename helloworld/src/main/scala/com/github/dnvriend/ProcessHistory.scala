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

import java.text.SimpleDateFormat
import java.util.Date

import org.activiti.engine.ProcessEngineConfiguration
import org.activiti.engine.history.HistoricProcessInstanceQuery

import scala.util.Try

object ProcessHistory extends App {

  implicit class DateFormat(val date: Date) extends AnyVal {
    def format: Option[String] = Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(date)).toOption
  }

  val procId = "17504"
  // create activiti process engine
  val processEngine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
    .buildProcessEngine()

  // verify that the process is actually finished
  val historyService = processEngine.getHistoryService
  val historicProcessQueryResults: HistoricProcessInstanceQuery =
    historyService.createHistoricProcessInstanceQuery().processInstanceId(procId)

  val historicProcessInstance = historicProcessQueryResults.singleResult()
  val endDateOption = Option(historicProcessInstance) flatMap (_.getEndTime.format)
  if (endDateOption.nonEmpty) {
    endDateOption.foreach { endTime ⇒
      println(s"Process instance $procId end time: $endTime")
    }

  } else if (historicProcessQueryResults.count() == 1) {
    println(s"Process '$procId' exists but has not been completed")

  } else {
    println(s"Process '$procId' does not exist")
  }
}
