//package parser
//
//import akka.actor.{Actor, ActorLogging, Props}
//
//object Parser {
//
//  def props: Props = Props[Parser]
//
//  final case class ParseRequest(requestId: Long, toparse: String)
//
//  final case class ParseResponse(requestId: Long, value: Option[Any])
//
//}
//
//class Parser(id: String) extends Actor with ActorLogging {
//
//  import Parser._
//
//  override def preStart(): Unit = println("Parser $id started")
//
//  override def postStop(): Unit = println("Parser $id stopped")
//
//
//  def receive = {
//    case ParseRequest(toparse) => {
//      log.info(s"Parse Request received (from ${sender()}): $toparse")
//      // sender() ! ParseResponse(id, value)
//    }
//  }
//}
