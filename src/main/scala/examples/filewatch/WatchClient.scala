package examples.filewatch

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._
import java.nio.file._
import akka.pattern.ask


object WatchClient extends LazyLogging {

  def run(): Unit = {

    // create the system and actor
    val system = ActorSystem("WatchClientSystem")
    val watch = system.actorOf(FileSystemWatchActor.props(), s"WatchActor")

    implicit val timeout: Timeout = Timeout(1900.seconds) // for the ? pattern

    logger.info(s"Register")
    val fut = ask(watch, FileSystemWatchActor.Register(Paths.get("./src/test/resources/"), true))
          .mapTo[FileSystemWatchActor.Message]

    fut.map {
      case FileSystemWatchActor.Created(path) =>
        logger.info(s">>>>>>>>>> WatchClient - Created $path")
      case FileSystemWatchActor.Deleted(path) =>
        logger.info(s">>>>>>>>>> WatchClient - Deleted $path")
      case FileSystemWatchActor.Modified(path) =>
        logger.info(s">>>>>>>>>> WatchClient - Modified $path")
    }.failed.foreach( (ex: Throwable) => logger.error(s">>>>>>>>> error:\n$ex") )

    system.scheduler.scheduleOnce(1900.seconds) {
      system.terminate()
    }

    try {
      Await.ready(system.whenTerminated, 2000.seconds).onComplete {
        case Success(_) => logger.info("Done!")
        case Failure(ex) => logger.error("", ex)
      }
    } catch {
      case ex: Throwable => logger.error(s"WatchClient Await $ex")
    }


  }
}
