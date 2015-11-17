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

import org.activiti.engine.repository.Deployment
import org.activiti.engine.runtime.ProcessInstance
import org.github.dnvriend.activity.ActivitiImplicits._

import scala.util.Try

class DeployProcessTest extends TestSpec {
  "Activity Repository" should "deploy a process" in {
    // deploying a process should return the deployment id
    val deploymentOperation = repositoryService.createDeployment()
      .addClasspathResource("processes/simpletest.bpmn20.xml")
      .name("simpletest")
      .doDeploy

    deploymentOperation should be a 'success

    deploymentOperation.foreach { deployment ⇒
      // get a list of all deployments
      val deployments: List[Deployment] = repositoryService.createDeploymentQuery().asList

      // it should find a deployment with the same id as the one we deployed earlier
      // ergo the process should be deployed
      deployments.find(_.getId == deployment.getId) should not be 'empty
    }
  }

  it should "run a process" in {
    // launch a process instance by
    val deploymentOption = repositoryService.createDeploymentQuery().deploymentName("simpletest").single
    deploymentOption should not be 'empty

    deploymentOption.foreach { deployment ⇒
      val processInstance: Try[ProcessInstance] = runtimeService.startProcessByKey("simpletest")
      processInstance should be a 'success
    }
  }

  it should "undeploy a process" in {
    val deploymentOption: Option[Deployment] = repositoryService.createDeploymentQuery().deploymentName("simpletest").single
    deploymentOption should not be 'empty
    deploymentOption.foreach { deployment ⇒
      repositoryService.deleteDeploymentById(deployment.id, cascade = true) should be a 'success
    }
    repositoryService.createDeploymentQuery().deploymentName("simpletest").single shouldBe 'empty
  }
}
