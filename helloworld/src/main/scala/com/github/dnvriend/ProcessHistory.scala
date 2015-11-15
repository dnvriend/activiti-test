package com.github.dnvriend

import java.text.SimpleDateFormat
import java.util.Date

import org.activiti.engine.ProcessEngineConfiguration

import scala.util.Try

object ProcessHistory extends App {

  implicit class DateFormat(date: Date) {
    def format: Option[String] = Try(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(date)).toOption
  }

  val procId = "17504"
  // create activiti process engine
  val processEngine = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("activiti.cfg.xml")
    .buildProcessEngine()

  // verify that the process is actually finished
  val historyService = processEngine.getHistoryService
  val historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult()
  val endDateOption = historicProcessInstance.getEndTime.format
  if(endDateOption.nonEmpty) {
    endDateOption.foreach { endTime =>
        println(s"Process instance $procId end time: $endTime")
    }
  } else {
    println(s"Process '$procId' has not been completed")
  }
}
