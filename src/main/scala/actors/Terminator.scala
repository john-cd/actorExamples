package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}

class Terminator(ref: ActorRef) extends Actor with ActorLogging {

  context watch ref

  def receive: Receive = {
    case Terminated(_) =>
      log.info("{} has terminated, shutting down system", ref.path)
      context.system.terminate()
  }
}