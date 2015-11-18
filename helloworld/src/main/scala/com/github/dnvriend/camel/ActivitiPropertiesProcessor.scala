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

package com.github.dnvriend.camel

import com.github.dnvriend.activiti.CamelBehaviorCamelJsonBody
import com.github.dnvriend.camel.CamelImplicits._
import org.activiti.camel.ActivitiProducer
import org.apache.camel.{ Exchange, Processor }
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.{ Formats, NoTypeHints }
import scala.collection.JavaConverters._
import scala.util.{ Failure, Try }

class ActivitiPropertiesProcessor extends Processor {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  def jsonToMap(json: String): Option[Map[String, AnyVal]] =
    Try(parse(json).extract[Map[String, AnyVal]])
      .recoverWith {
        case t: Throwable ⇒
          println("JSON=" + json)
          t.printStackTrace()
          Failure(t)
      }.toOption

  override def process(exchange: Exchange): Unit = {
    exchange.bodyOpt[String]
      .flatMap(jsonToMap)
      .foreach { map ⇒
        exchange.setProperty(ActivitiProducer.PROCESS_KEY_PROPERTY, map.getOrElse(CamelBehaviorCamelJsonBody.ActivitiProcessKey, ""))
        exchange.setProperty(ActivitiProducer.PROCESS_ID_PROPERTY, map.getOrElse(CamelBehaviorCamelJsonBody.ActivitiProcessInstanceId, ""))
        exchange.getIn.setBody((map - CamelBehaviorCamelJsonBody.ActivitiProcessKey - CamelBehaviorCamelJsonBody.ActivitiProcessInstanceId - CamelBehaviorCamelJsonBody.ActivitiExecutionId - CamelBehaviorCamelJsonBody.ActivitiProcessDefinitionId).asJava)
      }
  }
}
