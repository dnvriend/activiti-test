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

import com.github.dnvriend.camel.CamelImplicits._
import org.activiti.engine.TaskService
import org.apache.camel.{ Exchange, Processor }
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.{ Formats, NoTypeHints }

class CompleteTaskProcessor(taskService: TaskService, taskName: String) extends Processor {

  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  override def process(exchange: Exchange): Unit = {
    Thread.sleep(50) // give the system some time to process the transactions.. it's too fast!
    exchange.bodyOpt[String].foreach { json ⇒
      val map = parse(json).extract[Map[String, String]]
      val tuple = (map.get("EVENT_TYPE"), map.get("TASK_ID"), map.get("TASK_NAME"))
      tuple match {
        case (Some("TASK_CREATED"), Some(taskId), Some(`taskName`)) ⇒
          println(s"CompleteTask [$taskName] Processor - completing taskId: " + taskId)
          taskService.complete(taskId)
        case _ ⇒ println(s"CompleteTask [$taskName] Processor - Not handling: " + json)
      }
    }
  }
}
