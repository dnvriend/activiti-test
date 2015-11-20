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

package com.github.dnvriend.playround.followup

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Processor
import org.apache.camel.Exchange
import org.activiti.engine.RuntimeService
import org.apache.camel.BeanInject
import scala.collection.JavaConversions._
import javax.annotation.Resource

object FollowUpRoutes {

  case class PaymentRejection(orderId: Int, orderItemId: Int, playround: String, mutationCode: String, reasonCode: String)
  case class PaymentRejectionWithHistory(rejection: PaymentRejection, history: Seq[PaymentRejection])
//
//  def paymentRejectionToMap(rejection: PaymentRejection): Map[String, Any] = {
//    import rejection._
//    Map(
//      "orderId" -> orderId,
//      "orderItemId" -> orderItemId,
//      "playround" -> playround,
//      "mutationCode" -> mutationCode,
//      "reasonCode" -> reasonCode
//    )
//  }
//
//  def rejectionHistoryMap(history: PaymentRejectionWithHistory): Map[String, Any] = Map(
//    "rejection" -> history.rejection,
//    "history" -> history.history
//  )
  
  class ToMessage extends Processor {
    
    @Resource
    var runtimeService: RuntimeService = _
    
    def process(exchange: Exchange): Unit = {
      val payload = exchange.getIn.getBody(classOf[PaymentRejectionWithHistory])
      runtimeService.startProcessInstanceByMessage("reversal", payload.rejection.playround, Map("input" -> payload))
    }
  }
}

class FollowUpRoutes extends RouteBuilder {

  import FollowUpRoutes._

  
  def configure(): Unit = {

    val messageProcessor = new ToMessage
    from("direct:rejectPayment")
      .to("activiti:followup")
//      .process(messageProcessor)
      
    from("activiti:followup:retryCollection")
      .to("direct:retryCollection")
      
    from("activiti:followup:withdrawTicket")
      .to("direct:withdrawTicket")

    from("activiti:followup:notify")
      .to("direct:notify")
      
    from("activiti:followup:cancelSubscription")
      .to("direct:cancelSubscription")
    
  }
}
