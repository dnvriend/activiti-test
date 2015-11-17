package com.github.dnvriend

import org.github.dnvriend.activity.ActivitiImplicits._
import scala.util.Try
import org.activiti.engine.repository.Deployment
import scala.collection.JavaConversions._
import org.activiti.engine.history.HistoricDetail
import org.activiti.engine.history.HistoricVariableUpdate

class SuperProcessTest extends TestSpec {
  
  "Superprocess" should "include its subprocesses" in {
    val subDeploy = repositoryService.createDeployment()
      .addClasspathResource("processes/subprocess.bpmn20.xml")
      .name("subprocess")
      .doDeploy
    
      subDeploy should be a 'success
      
    val superDeploy = repositoryService.createDeployment()
      .addClasspathResource("processes/superprocess.bpmn20.xml")
      .name("superProcessTest")
      .doDeploy
      
    
    superDeploy should be a 'success 

    val variableMap: Map[String, Object] = Map("text" -> "-")
    
    val startProcessOperation = runtimeService.startProcessByKey("superProcessTest", variableMap)
    startProcessOperation should be a 'success
    
    superDeploy.foreach { deployment ⇒
      startProcessOperation.foreach { processInstance ⇒
          historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId).asList should not be 'empty
          
          val textUpdates: List[HistoricDetail] = historyService.createHistoricDetailQuery().variableUpdates().list.toList
          
          textUpdates should not be 'empty
          
          val varUpdates = textUpdates.map(_.asInstanceOf[HistoricVariableUpdate])
          val lastValue = varUpdates.last.getValue.asInstanceOf[String]
          
          lastValue should startWith ("subprocess")
          lastValue should endWith ("superprocess")
        }
    }
  }
}