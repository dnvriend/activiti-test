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

import org.activiti.engine.runtime.Execution
import scala.collection.JavaConversions._
import com.github.dnvriend.activiti.ActivitiImplicits._
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.test.junit4.CamelTestSupport
import org.activiti.camel.ActivitiProducer._

object ReceiveTask {
  case class TestType(i: Int, s: String)
}

class ReceiveTask extends CamelTestSupport with TestSpec {

  import ReceiveTask._

  val process1Id = "P201501"
  val process2Id = "P201502"

  override def createCamelContext(): CamelContext = camelContext
  override protected def beforeAll(): Unit = setUp() // required as CamelTestSupport uses junit before for initialization

  override def createRouteBuilder(): RouteBuilder = new RouteBuilder {
    def configure(): Unit = {
      from("direct:continue")
        .setProperty(PROCESS_KEY_PROPERTY, header(PROCESS_KEY_PROPERTY))
        .to("activiti:receiveTaskTest:continuation")
    }
  }

  def playroundQuery(playroundId: String) = runtimeService.createExecutionQuery().variableValueEquals("playroundId", playroundId)

  def isRunning(playroundId: String) = playroundQuery(playroundId).list().size() > 0

  def continueUsingSignal(playroundId: String) = {
    val continuation = playroundQuery(playroundId).activityId("continuation").singleResult()
    continuation should not be null
    runtimeService.signal(continuation.getId, Map("output" -> "BOOH"))
  }

  def continueUsingCamel(playroundId: String) = {
    sendBodyAndHeaders("direct:continue", TestType(1337, "1337"), Map("PROCESS_KEY_PROPERTY" -> playroundId))
  }

  "ReceiveTask" should "finish up the process once triggered" in {

    val deployment = deploy("receiveTask.bpmn20.xml")
    deployment should be a 'success

    val params = Map("output" -> "none")

    val process1Params = params + ("playroundId" -> process1Id)
    val process2Params = params + ("playroundId" -> process2Id)

    // Start two instances of the process
    val startProcess1 = runtimeService.startProcessByKey("receiveTaskTest", process1Id, process1Params)
    val startProcess2 = runtimeService.startProcessByKey("receiveTaskTest", process2Id, process2Params)

    startProcess1 should be a 'success
    startProcess2 should be a 'success

    isRunning(process1Id) should be(true)
    isRunning(process2Id) should be(true)

    continueUsingSignal(process1Id)
    isRunning(process1Id) should be(false)
    isRunning(process2Id) should be(true)

    continueUsingCamel(process2Id)
    isRunning(process2Id) should be(false)
  }

}
