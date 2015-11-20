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

package com.github.dnvriend.activiti.event

import com.github.dnvriend.activiti.ActivitiImplicits._
import org.activiti.engine.delegate.event.{ ActivitiEvent, ActivitiEventListener, ActivitiEventType }
import org.apache.camel.ProducerTemplate
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.{ Formats, NoTypeHints }

import scala.util.Try

class CamelProducerTemplateEventListener(producerTemplate: ProducerTemplate) extends ActivitiEventListener {

  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  override def onEvent(event: ActivitiEvent): Unit = event.getType match {
    case ActivitiEventType.TASK_CREATED   ⇒ sendEvent(event)
    case ActivitiEventType.TASK_ASSIGNED  ⇒ sendEvent(event)
    case ActivitiEventType.TASK_COMPLETED ⇒ sendEvent(event)
    case _                                ⇒ //println("Skipping: " + event.getType.name)
  }

  def processEvent(event: ActivitiEvent): String =
    write(event.toMap.collect { case (key, Some(value)) ⇒ (key, value) })

  def sendEvent(event: ActivitiEvent): Unit = Try {
    println("Sending: " + event.getType.name())
    producerTemplate.sendBody("direct:sendToActivitiEventTopic", processEvent(event))
  }.recover {
    case t: Throwable ⇒
      t.printStackTrace()
  }

  override def isFailOnException: Boolean = false
}
