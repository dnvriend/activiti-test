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

import org.activiti.engine.delegate.event.{ ActivitiEvent, ActivitiEventListener }
import org.apache.camel.ProducerTemplate

import scala.util.Try

class CamelProducerTemplateEventListener(producerTemplate: ProducerTemplate) extends ActivitiEventListener {

  def processEvent(event: ActivitiEvent): String = event.getType.name()

  override def onEvent(event: ActivitiEvent): Unit = Try {
    println("Sending: " + event.getType.name())
    producerTemplate.sendBody("activemq:topic:VirtualTopic.ActivitiEventTopic", processEvent(event))
  }.recover {
    case t: Throwable ⇒
      t.printStackTrace()
  }

  override def isFailOnException: Boolean = false
}
