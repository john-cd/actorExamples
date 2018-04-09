package actors

import akka.actor.Props

// pattern for an Actor that can switch behavior at run-time
abstract class BecomeActor extends BaseActor {

  // default behavior
  override def receive: Receive = defaultBehavior

  protected def defaultBehavior: Receive = {
    // case SwitchBehavior => become(alternativeBehavior)
    case uh => unhandled(uh)
  }

  // def alternativeBehavior: Receive = {
  //    case SwitchBehavior => become(defaultBehavior)
  //    case _ => unhandled()
  //  }

}

object BecomeActor {

  // case object SwitchBehavior

  def props = Props(classOf[BecomeActor])
}

