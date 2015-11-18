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

package com.github.dnvriend.activiti

import java.util

import org.activiti.camel.impl.CamelBehaviorBodyAsMapImpl
import org.activiti.camel.{ ActivitiEndpoint, ActivitiProducer }
import org.activiti.engine.impl.pvm.delegate.ActivityExecution
import org.apache.camel.Exchange
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.{ Formats, NoTypeHints }

import scala.collection.JavaConversions._

/**
 * Converts the variables to a JSON Map String literal
 */
object CamelBehaviorCamelJsonBody {
  final val ActivitiProcessInstanceId = "ACTIVITI_PROCESS_INSTANCE_ID"

  final val ActivitiProcessDefinitionId = "ACTIVITI_PROCESS_DEFINITION_ID"

  final val ActivitiProcessKey = "ACTIVITI_PROCESS_KEY"

  final val ActivitiExecutionId = "ACTIVITI_EXECUTION_ID"
}

class CamelBehaviorCamelJsonBodyImpl extends CamelBehaviorBodyAsMapImpl {
  import CamelBehaviorCamelJsonBody._

  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  override def createExchange(activityExecution: ActivityExecution, endpoint: ActivitiEndpoint): Exchange = {
    val exchange = super.createExchange(activityExecution, endpoint)
    exchange.setProperty(ActivitiProcessDefinitionId, activityExecution.getProcessInstance.getProcessDefinitionId)
    updateTargetVariables(endpoint)
    copyVariables(activityExecution.getVariables, exchange, endpoint)
    exchange
  }

  override def copyVariablesToBodyAsMap(variables: util.Map[String, AnyRef], exchange: Exchange): Unit = {
    val processInstanceId: Option[String] = Option(exchange.getProperty(ActivitiProducer.PROCESS_ID_PROPERTY)).map(_.asInstanceOf[String])
    val processDefinitionId: Option[String] = Option(exchange.getProperty(ActivitiProcessDefinitionId)).map(_.asInstanceOf[String])
    val executionId: Option[String] = Option(exchange.getProperty(ActivitiProducer.EXECUTION_ID_PROPERTY)).map(_.asInstanceOf[String])
    val processKey: Option[String] = processDefinitionId.flatMap(_.split(":").headOption)
    val json: String = write(variables.toMap ++
      Map(
        ActivitiProcessInstanceId -> processInstanceId,
        ActivitiProcessDefinitionId -> processDefinitionId,
        ActivitiExecutionId -> executionId,
        ActivitiProcessKey -> processKey

      ).collect {
          case (key, Some(value)) ⇒ (key, value)
        }
    )
    exchange.getIn.setBody(json)
  }
}
