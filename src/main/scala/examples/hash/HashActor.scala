package examples.hash

import akka.actor.{Actor, ActorLogging, Props}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


trait HashService {

  def checksum(filePath: String): Future[String]

}


object HashActor {

  def props(hashService: HashService): Props = Props(classOf[HashActor], hashService)

  // messages
  sealed trait Message

  final case class Hash(id: Long, filePath: String) extends Message

  final case class Hashed(id: Long, filePath: String, hash: String) extends Message

  final case class HashTimeout(id: Long) extends Message

}

class HashActor(hashService: HashService) extends Actor with ActorLogging {

  import HashActor._

  def receive(): Receive = {

    case Hash(id, filePath) =>
      log.debug(s"Received message: Hash $id $filePath")
      import context.dispatcher
      val timeout = context.system.scheduler.scheduleOnce(10.seconds) {
        self ! HashTimeout(id)
        log.debug(s"Sent message: HashTimeout $id")
      }
      val originalSender = sender() // the future must close over an immutable variable
      hashService.checksum(filePath).onComplete {
        case Failure(x) =>
          throw new HashException(x)
        case Success(h) =>
          timeout.cancel
          originalSender ! Hashed(id, filePath, h)
      }
  } // receive
}


class HashException(message: String, nestedException: Throwable) extends Exception(message, nestedException) {
  def this() = this("", null)

  def this(message: String) = this(message, null)

  def this(nestedException: Throwable) = this("", nestedException)
}