package examples.filewatch

import java.nio.file.Path

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}

import scala.concurrent.duration._
import collection.mutable.{HashMap, MultiMap, Set}

/**
  * Non-blocking actor facade for WatchService
  * Monitors changes to the file system
  */
object FileSystemWatchActor {

  def props(): Props = Props[FileSystemWatchActor]

  // timer messages
  private case object TickKey

  private case object Tick

  // messages
  final case class Register(start: Path, recurse: Boolean)

  sealed trait Message

  final case class Created(path: Path) extends Message

  final case class Modified(path: Path) extends Message

  final case class Deleted(path: Path) extends Message
}

class FileSystemWatchActor extends Actor
  with Timers
  with ActorLogging {

  import FileSystemWatchActor._

  private var wd: WatchDir = _
  private lazy val registry = new HashMap[Path, Set[ActorRef]] with MultiMap[Path, ActorRef]


  private def created(registeredPath: Path, path: Path): Unit = {
    log.info(s"-------------------- FileSystemWatchActor - created $path")
    log.debug(s"registeredPath: $registeredPath registry: ${registry.mkString}")
    registry.get(registeredPath).foreach( set => set.foreach( _ ! Created(path)))
  }

  private def deleted(registeredPath: Path, path: Path): Unit = {
    log.info(s"-------------------- FileSystemWatchActor - deleted $path")
    registry.get(registeredPath).foreach( set => set.foreach( _ ! Deleted(path) ))
  }

  private def modified(registeredPath: Path, path: Path): Unit = {
    log.info(s"-------------------- FileSystemWatchActor - modified $path")
    registry.get(registeredPath).foreach( set => set.foreach( _ ! Modified(path)))
  }

  override def preStart(): Unit = {
    super.preStart()
    wd = WatchDir(created, modified, deleted)
    timers.startSingleTimer(TickKey, Tick, 1.second)
  }

  override def postStop(): Unit = {
    super.postStop()
    timers.cancelAll()
    if (wd != null)
      wd.close()
  }

  def receive: Receive = {
    case Register(start, recurse) =>
      val _sender: ActorRef = sender()
      registry.addBinding(start, _sender)
      log.debug(s"FileSystemWatchAtor - Add ${_sender} to registry")
      if (wd != null)
        wd.register(start, recurse)
    case Tick =>
      //log.debug("tick!")
      if (wd != null)
        wd.pollAll()
      // re-arm the timer - better implementation than using periodic timer
      timers.startSingleTimer(TickKey, Tick, 5.second)
  }

}