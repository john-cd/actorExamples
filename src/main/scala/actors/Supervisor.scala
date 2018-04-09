package actors

import akka.actor.{Actor, ActorLogging, Props, Terminated}

object Supervisor {
  def props(): Props = Props[Supervisor]
}

class Supervisor extends BaseActor {

  //override def preStart(): Unit = { log.info("Supervisor started") }

  //override def postStop(): Unit = { log.info("Supervisor stopped") }

  // starts child, then keep an eye on it
  // If child is killed or stopped, the Parent actor is sent a Terminated(child) message
  protected def createChild(props: Props, name: String): Unit = {
    val child = context.actorOf(props, name)
    context.watch(child)
  }

//  override def receive: Receive = {
//    case Terminated(child) => {
//      log.info("Child $child killed")
//    }
//    case _ => println("Parent received an unknown message")
//  }

}

