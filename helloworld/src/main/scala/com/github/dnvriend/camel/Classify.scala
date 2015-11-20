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

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Processor
import org.apache.camel.Exchange

object Classify {

  case class PaymentRejection(orderId: Int, orderItemId: Int, playround: String, mutationCode: String, reasonCode: String)
  case class PaymentRejectionWithHistory(rejection: PaymentRejection, history: Seq[PaymentRejection])

  def paymentRejectionToMap(rejection: PaymentRejection): Map[String, Any] = {
    import rejection._
    Map(
      "orderId" -> orderId,
      "orderItemId" -> orderItemId,
      "playround" -> playround,
      "mutationCode" -> mutationCode,
      "reasonCode" -> reasonCode
    )
  }

  def rejectionHistoryMap(history: PaymentRejectionWithHistory): Map[String, Any] = Map(
    "rejection" -> history.rejection,
    "history" -> history.history
  )
}

class Classify extends RouteBuilder {

  import Classify._

  def configure(): Unit = {

    val rejectionMapper = new Processor() {
      override def process(exchange: Exchange): Unit = {
        val rejectionHistory = exchange.getIn.getBody(classOf[PaymentRejectionWithHistory])
        val map = rejectionHistoryMap(rejectionHistory)
        exchange.getIn.setBody(map)
      }
    }

    from("direct:rejectPayment")
      .process(rejectionMapper)
      .to("activiti:rejectPayment")
  }
}
