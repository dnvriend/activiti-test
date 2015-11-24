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

import org.activiti.engine.delegate.DelegateExecution
import org.activiti.engine.delegate.JavaDelegate

import com.github.dnvriend.playround.followup.FollowUpRoutes.PaymentRejectionWithHistory

object BusinessRules {

  type BusinessRule = PaymentRejectionWithHistory ⇒ Boolean
  type NamedBusinessRule = (String, PaymentRejectionWithHistory ⇒ Boolean)

  val followUpRules: Set[NamedBusinessRule] = Set(
    ("retry", shouldRetryCollection),
    ("cancel", shouldCancelSubscription),
    ("notify", shouldNotify))

  def shouldRetryCollection: BusinessRule = input ⇒
    input.rejection.mutationCode.contains("R") || input.rejection.reasonCode.contains("R")

  def shouldCancelSubscription: BusinessRule = input ⇒
    input.rejection.mutationCode.contains("C") || input.rejection.reasonCode.contains("C")

  def shouldNotify: BusinessRule = input ⇒
    input.rejection.mutationCode.contains("N") || input.rejection.reasonCode.contains("N")
}

class BusinessRules extends JavaDelegate {

  import BusinessRules._

  def execute(execution: DelegateExecution): Unit = {
    val rejection = execution.getVariable("camelBody").asInstanceOf[PaymentRejectionWithHistory]

    followUpRules foreach {
      case (name, rule) ⇒
        val ruling = rule(rejection)
        execution.setVariable(name, ruling)
        println(s"Verdict on $name = $ruling")
    }
  }
}
