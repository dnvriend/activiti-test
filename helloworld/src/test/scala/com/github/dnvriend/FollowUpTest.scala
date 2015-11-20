package com.github.dnvriend

import com.github.dnvriend.playround.followup.FollowUpRoutes.PaymentRejectionWithHistory
import com.github.dnvriend.playround.followup.FollowUpRoutes.PaymentRejection
import scala.collection.JavaConversions._
import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.CamelContext

class FollowUpTest extends CamelTestSupport with TestSpec {

  setUseRouteBuilder(true)
  
  override def context(): CamelContext = camelContext

  val testRoutes = new RouteBuilder {
    def configure(): Unit = {
      from("direct:retryCollection").to("mock:retryCollection")
      from("direct:withdrawTicket").to("mock:withdrawTicket")
      from("direct:notify").to("mock:notify")
      from("direct:cancelSubscription").to("mock:cancelSubscription")
    }
  }
  
  "FollowUp" should "have a clue" in {
    
    val deployExecute = deploy("followUpExecute.bpmn20.xml")
    val deployTrigger = deploy("followUpTrigger.bpmn20.xml")
    
    deployExecute should be a 'success
    deployTrigger should be a 'success
    
    testRoutes.addRoutesToCamelContext(camelContext)
    val endpoint = camelContext.getEndpoint("mock:notify")
    getMockEndpoint("mock:notify").expectedMessageCount(1)
    
    val rejection = PaymentRejection(0, 0, "P201601", "ABC", "DEF")
    val withHistory = PaymentRejectionWithHistory(rejection, List.empty)
    
    producerTemplate.sendBody("direct:rejectPayment", withHistory)
    
    assertMockEndpointsSatisfied()
  }
  
}