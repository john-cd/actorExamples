package examples.hash

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import examples.hash.HashActor._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object HashClient extends LazyLogging {

  def run(): Unit = {

    val system = ActorSystem("HashClientSystem")
    val hashService = new HashService {}
    val hashA = system.actorOf(HashActor.props(hashService), "HashActor")
    implicit val timeout: Timeout = Timeout(15.seconds) // for the ? pattern

    for {
      res2 <- (hashA ? Hash(0L, "README.md")).mapTo[Hashed] // TODO handle the HashTimeOut message
    }
      logger.info(s"result $res2")

    system.scheduler.scheduleOnce(20.seconds) {
      system.terminate()
    }

    Await.ready(system.whenTerminated, 50.seconds).onComplete {
      case Success(_) => logger.info("Done!")
      case Failure(ex) => logger.error("Failure!", ex)
    }
  }
}
