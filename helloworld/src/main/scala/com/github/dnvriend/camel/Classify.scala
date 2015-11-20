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
     
     val rejectionMapper = new Processor(){
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