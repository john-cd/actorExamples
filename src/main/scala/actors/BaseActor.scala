package actors

import akka.actor.{Actor, ActorLogging, Props}

object BaseActor {
  def props(): Props = Props[BaseActor]
}

/**
  * Base class for Actors in this project
  */
class BaseActor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    super.preStart()
    log.debug(s"${self.path} started")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.debug(s"${self.path} stopped")
  }

  // It is not recommended to fully override preRestart and postRestart
  // Default implementation: https://doc.akka.io/docs/akka/current/actors.html#actor-api
  // def preRestart(reason: Throwable, message: Option[Any]): Unit = {
  //   context.children foreach { child =>
  //     context.unwatch(child)
  //     context.stop(child)
  //   }
  //   postStop()
  // }
  //
  // def postRestart(reason: Throwable): Unit = {
  //   preStart()
  // }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(s"${self.path} restarting...")
    super.preRestart(reason, message) // stops all children, calls postStop( ) for crashing actor
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    log.info(s"${self.path} restarted...")
  }


  // No need to handle any messages here
  override def receive: Receive = Actor.emptyBehavior
}


