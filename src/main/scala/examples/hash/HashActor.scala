package examples.hash

import java.io.IOException
import java.nio.file._
import java.security.MessageDigest

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import org.apache.commons.codec.binary.Hex


trait HashService extends LazyLogging {

  def checksum(filePath: String): Future[String] = checksum(Paths.get(filePath).normalize)

  def checksum(path: Path): Future[String] = {
    require(path != null)
    logger.debug(s"Starting checksum of $path")

    import scala.concurrent.ExecutionContext.Implicits.global

    if (!Files.isReadable(path)) // readable = existing and accessible
      Future.failed(new IOException(s"File is not readable: $path"))
    else if (Files.isDirectory(path))
      Future.failed(new Exception(s"Can't hash a directory: $path"))
    else
      Future { // HACK blocking
        val byteArray = Files.readAllBytes(path)
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(byteArray) // MD5 is 128 bits
        new String(Hex.encodeHex(hash))
      }
  }
}


object HashActor {

  def props(hashService: HashService): Props = Props(classOf[HashActor], hashService)

  // messages
  sealed trait Message

  final case class Hash(id: Long, filePath: String) extends Message

  final case class Hashed(id: Long, filePath: String, hash: String) extends Message

  final case class HashTimeout(id: Long) extends Message

  // message to self
  private case class HashServiceResponse(id: Long, filePath: String, hash: String)

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
      //// Alternative:
      // import akka.pattern.pipe
      // hashService.checksum(filePath).map(hash => HashServiceResponse(id, filePath, hash)).pipeTo(self)(sender())

    case HashServiceResponse(id, filePath, h) =>
      sender() ! Hashed(id, filePath, h)

  } // receive
}


class HashException(message: String, nestedException: Throwable) extends Exception(message, nestedException) {
  def this() = this("", null)

  def this(message: String) = this(message, null)

  def this(nestedException: Throwable) = this("", nestedException)
}