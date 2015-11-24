package com.github.dnvriend.playround.followup

import org.activiti.engine.delegate.DelegateExecution
import org.activiti.engine.delegate.JavaDelegate

import com.github.dnvriend.playround.followup.FollowUpRoutes.PaymentRejectionWithHistory

object BusinessRules {

  type BusinessRule = PaymentRejectionWithHistory => Boolean
  type NamedBusinessRule = (String, PaymentRejectionWithHistory => Boolean)

  val followUpRules: Set[NamedBusinessRule] = Set(
    ("retry", shouldRetryCollection),
    ("cancel", shouldCancelSubscription),
    ("notify", shouldNotify))

  def shouldRetryCollection: BusinessRule = input =>
    input.rejection.mutationCode.contains("R") || input.rejection.reasonCode.contains("R")

  def shouldCancelSubscription: BusinessRule = input =>
    input.rejection.mutationCode.contains("C") || input.rejection.reasonCode.contains("C")

  def shouldNotify: BusinessRule = input =>
    input.rejection.mutationCode.contains("N") || input.rejection.reasonCode.contains("N")
}

class BusinessRules extends JavaDelegate {

  import BusinessRules._

  def execute(execution: DelegateExecution): Unit = {
    val rejection = execution.getVariable("camelBody").asInstanceOf[PaymentRejectionWithHistory]

    followUpRules foreach {
      case (name, rule) =>
        val ruling = rule(rejection)
        execution.setVariable(name, ruling)
        println(s"Verdict on $name = $ruling")
    }
  }
}