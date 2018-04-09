package actors


// pattern for a Actor that can aggregate multiple behavior
// https://doc.akka.io/docs/akka/2.0/scala/actors.html
trait ComposableActor extends BaseActor {

  private var receives: List[Receive] = List()

  protected def registerReceive(receive: Receive) {
    receives = receive :: receives
  }

  override def receive: Receive = receives reduce {
    _ orElse _
  }
}


// Example:
//class MyComposableActor extends ComposableActor {
//
//  override def preStart(): Unit = {
//    registerReceive({
//      case "foo" ⇒ /* Do something */
//    })
//
//    registerReceive({
//      case "bar" ⇒ /* Do something */
//    })
//  }
//}


// You can also compose using self types:
// https://doc.akka.io/docs/akka/snapshot/actors.html#extending-actors-using-partialfunction-chaining
//
//trait ReadFileBehavior {
//  this: Actor =>
//  val readFileBehavior: Receive = {
//    case _ => // ...
//  }
//}
//
//class MyActor extends Actor with ReadFileBehavior  {
//  def receive = readFileBehavior.orElse[Any, Unit](secondBehavior)
//}

