package com.example

import akka.actor.{Actor, ActorLogging}

/**
 * Created by alex on 12/14/15.
 */
abstract class ActorWithLogging extends Actor with ActorLogging {

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.info("preStart")
    super.preStart()
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    log.info("postStop")
    super.postStop()
  }

  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info("preRestart")
    super.preRestart(reason, message)
  }

  @throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    log.info("postRestart")
    super.postRestart(reason)
  }
}
