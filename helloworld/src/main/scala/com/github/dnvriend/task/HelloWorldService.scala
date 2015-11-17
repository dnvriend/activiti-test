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

package com.github.dnvriend.task

import org.activiti.engine.delegate.{ DelegateExecution, JavaDelegate }
import org.github.dnvriend.activity.ActivitiImplicits._

class HelloWorldService extends JavaDelegate {
  override def execute(execution: DelegateExecution): Unit = {
    val nameOption = execution.get("name")
    val msg: String = nameOption.map(name ⇒ s"Hello $name").getOrElse("Hello unknown!")
    execution.set("msg", msg)
  }
}
