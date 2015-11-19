package com.github.dnvriend.activiti.event

import org.activiti.engine.delegate.event.{ActivitiEvent, BaseEntityEventListener}

class LoggingBaseEntityEventListener extends BaseEntityEventListener {
  override def onCreate(event: ActivitiEvent): Unit = {
    super.onCreate(event)
  }
}
