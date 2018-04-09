package examples.cache

import java.util.concurrent.atomic.AtomicLong

import actors._
import akka.actor.{ActorRef, Props, Terminated, Timers}

import scala.concurrent.duration._

object Cache {

  def props[K, V](cacheId: Int): Props = Props(classOf[Cache[K, V]], cacheId)

  // Timer
  private case object TickKey

  private case object Tick

  // Messages
  sealed trait Message[+K, +V]

  case class Add[K, V](requestId: Long, key: K, value: V) extends Message[K, V]

  case class Added(requestId: Long) extends Message[Nothing, Nothing]

  case class Get[K](requestId: Long, key: K) extends Message[K, Nothing]

  case class Got[V](requestId: Long, value: Option[V]) extends Message[Nothing, V]

  def getRequestId: Long = {
    requestIdCounter.getAndIncrement()
  }

  private lazy val requestIdCounter: AtomicLong = new AtomicLong(0L)

}

class Cache[K, V](cacheId: Int) extends Supervisor with Timers {

  import Cache._

  def this() = this(-1)

  // the nodes that the Cache knows about
  private final val nodes: Array[Option[ActorRef]] = Array.fill(10) {
    None
  }


  override def preStart(): Unit = {
    super.preStart()
    // start timer to decrement TTL
    timers.startPeriodicTimer(TickKey, Tick, 1.second)
    log.info(s"Cache $cacheId started")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.info(s"Cache $cacheId stopped")
  }

  private def decrementTTL(): Unit = for {Some(node) <- nodes} node ! StorageNode.DecrementTTL


  override def receive: Receive = {

    case Tick => decrementTTL()

    case Terminated(child) =>
      log.warning(s"$child died")
      nodes.update(nodes.indexOf(Some(child)), None)
      log.debug(nodes.mkString("<", " , ", ">"))

    case Add(requestId, key, value) =>
      log.info(s"Request $requestId received on cache $cacheId from ${sender()} - Add $key -> $value")
      //val hash = MessageDigest.getInstance("MD5").digest(key.asInstanceOf[Array[Byte]]) // MD5 is 128 bits
      //log.info(s"md5: ${Base64.getEncoder.encodeToString(hash) } ")
      val index = math.abs(key.##) % nodes.length
      val nodeOpt = nodes(index)
      nodeOpt match {
        case Some(node) =>
          node ! StorageNode.SN_Add(self, requestId, key, value)
        case None =>
          val newActor = context.actorOf(StorageNode.props(index), s"StorageNode$index")
          context watch newActor
          nodes(index) = Some(newActor)
          newActor ! StorageNode.SN_Add(self, requestId, key, value)
      }
      sender() ! Added(requestId)

    case Get(requestId, key) =>
      log.info(s"Request $requestId received on cache $cacheId from ${sender()} - Get $key")
      val client = sender()
      val index = math.abs(key.##) % nodes.length
      nodes(index) match {
        case Some(storageNode) =>
          log.info(s"Found storageNode $index for key $key")
          storageNode ! StorageNode.SN_Get(self, requestId, key) // TODO: still a small possibility that storageNode is dead but we haven't received the Terminated message
        case None =>
          log.info(s"Replying to client - requestId: $requestId - Got None")
          client ! Got(requestId, None)
      }

    case StorageNode.SN_Got(client, requestId, value) =>
      log.info(s"Replying to client - requestId $requestId - Got $value")
      client ! Got(requestId, value)
  }
}
