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

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder

/**
 * Activiti simply activates a specific Camel Route that can be identified by the
 * Activiti process id='SimpleCamelCallProcess' appended with
 * the Activiti task id='simpleCall'.
 *
 * In the CamelRoute the 'from' component has the following format:
 * 'from("activiti:SimpleCamelCallProcess:simpleCall")'
 *
 * Notice the format of the from endpoint. It is consisted of three parts:
 * 1. activiti              : refers to Activiti endpoint
 * 2. SimpleCamelCallProcess: name of the process
 * 3. simpleCall            : name of the Camel service in the process
 *
 * For more information read the *whole* section, from top to bottom about the
 * CamelTask here: http://activiti.org/userguide/index.html#bpmnCamelTask
 */
class HelloWorldRoute extends RouteBuilder {

  override def configure(): Unit = {
    /**
     * activated by the task `simpleCall` from the process `SimpleCamelCallProcess`
     * that is defined in the file `cameltask.bpmn20.xml`.
     *
     * The body should contain a JSON Map of process variables, including
     * the `ACTIVITI_PROCESS_INSTANCE_ID` and the `ACTIVITI_PROCESS_DEFINITION_ID`
     * and other process variables that have been saved in the Activiti process.
     */
    from("activiti:SimpleCamelCallProcess:simpleCall")
      .log(LoggingLevel.INFO, "activiti:SimpleCamelCallProcess:simpleCall")
      .to("activemq:queue:LogQueue")

    /**
     * Just launch the activiti process "javaservicetask-classdef",
     * no bells and whistles
     */
    from("direct:startProcessFirst")
      .to("activiti:javaservicetask-classdef")

    /**
     * Launch the activiti process with a process initiator 'kermit'
     */
    from("direct:startProcessSecond")
      .setHeader("CamelProcessInitiatorHeader", constant("kermit"))
      .to("activiti:javaservicetask-classdef?processInitiatorHeaderName=CamelProcessInitiatorHeader")

    /**
     * Launch the activiti process with the ActivitiPropertiesProcessor
     * that will set headers needed for the execution of a process
     */
    from("direct:startProcessThird")
      .process(new ActivitiPropertiesProcessor)
      .to("activiti:javaservicetask-classdef")
    //      .to("log:startProcessThird?showAll=true")

    /**
     * Consuming messages from the LogQueue
     */
    from("activemq:queue:LogQueue")
      .id("Consume from LogQueue")
      .to("log:Consume from LogQueue?showAll=true")
  }
}
