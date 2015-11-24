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

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.test.junit4.CamelTestSupport
import org.scalatest.BeforeAndAfterAll

import com.github.dnvriend.playround.followup.FollowUpRoutes.PaymentRejection
import com.github.dnvriend.playround.followup.FollowUpRoutes.PaymentRejectionWithHistory

class FollowUpTest extends CamelTestSupport with TestSpec with BeforeAndAfterAll {

  override def createCamelContext(): CamelContext = camelContext
  override protected def beforeAll(): Unit = setUp() // required as CamelTestSupport uses junit before for initialization

  override def createRouteBuilder(): RouteBuilder = new RouteBuilder {
    def configure(): Unit = {
      from("direct:retryCollection").to("mock:retryCollection")
      from("direct:withdrawTicket").to("mock:withdrawTicket")
      from("direct:notify").to("mock:notify")
      from("direct:cancelSubscription").to("mock:cancelSubscription")
    }
  }

  lazy val mockRetryCollection = getMockEndpoint("mock:retryCollection")
  lazy val mockWithdrawTicket = getMockEndpoint("mock:withdrawTicket")
  lazy val mockNotify = getMockEndpoint("mock:notify")
  lazy val mockCancelSubscription = getMockEndpoint("mock:cancelSubscription")

  "FollowUp" should "trigger actions in line with the classification of the payment rejection" in {

    val deployExecute = deploy("followUpExecute.bpmn20.xml")
    val deployTrigger = deploy("followUpTrigger.bpmn20.xml")

    deployExecute should be a 'success
    deployTrigger should be a 'success

    val rejection = PaymentRejection(0, 0, "P201601", "ABC", "DEF")
    val withHistory = PaymentRejectionWithHistory(rejection, List.empty)

    val msgCount = 1000

    mockRetryCollection.expectedMessageCount(0)
    mockWithdrawTicket.expectedMessageCount(msgCount)
    mockNotify.expectedMessageCount(0)
    mockCancelSubscription.expectedMessageCount(msgCount)

    1 to msgCount foreach { _ ⇒
      producerTemplate.sendBody("direct:rejectPayment", withHistory)
    }

    assertMockEndpointsSatisfied()
  }
}
