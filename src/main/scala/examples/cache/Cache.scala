package examples.cache

import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated, Timers}

import scala.concurrent.duration._
import actors._


object Cache extends Cache[Nothing, Nothing] {

  def props[K, V](cacheId: Int): Props = Props(new Cache[K, V](cacheId))

  // Timer
  private case object TickKey

  private case object Tick

  // Messages
  sealed trait Message[+K, +V]

  case class Add[K, V](requestId: Long, key: K, value: V) extends Message[K,V]

  case class Added(requestId: Long) extends Message[Nothing, Nothing]

  case class Get[K](requestId: Long, key: K) extends Message[K, Nothing]

  case class Got[V](requestId: Long, value: Option[V]) extends Message[Nothing, V]

  def getRequestId(): Long = {
    requestIdCounter.getAndIncrement()
  }

  private val requestIdCounter: AtomicLong = new AtomicLong(0L)

}

class Cache[K, V](cacheId: Int) extends Supervisor with Timers {

  import Cache._

  def this() = this(-1)

  // the nodes that the Cache knows about
  private final val nodes: Array[Option[ActorRef]] = Array.fill(256) {
    None
  }

  // start timer to decrement TTL
  timers.startPeriodicTimer(TickKey, Tick, 1.second)

  override def preStart(): Unit = {
    super.preStart()
    log.info(s"Cache $cacheId started")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.info(s"Cache $cacheId stopped")
  }

  private def decrementTTL() = for {Some(node) <- nodes} node ! DecrementTTL


  override def receive: Receive = {

    case Tick => decrementTTL()

    case Terminated(child) =>
      log.warning(s"$child died")

    case Add(requestId, key, value) =>
      log.info(s"Request $requestId received on cache $cacheId from ${sender()} - Add $key -> $value")
      //val hash = MessageDigest.getInstance("MD5").digest(key.asInstanceOf[Array[Byte]]) // MD5 is 128 bits
      //log.info(s"md5: ${Base64.getEncoder.encodeToString(hash) } ")
      val index = key.## % nodes.length
      val nodeOpt = nodes(index)
      nodeOpt match {
        case Some(node) =>
          node ! SN_Add(self, requestId, key, value)
        case None =>
          val newActor = context.actorOf(StorageNode.props(index))
          nodes(index) = Some(newActor)
          newActor ! SN_Add(self, requestId, key, value)
      }
      sender() ! Added(requestId)

    case Get(requestId, key) =>
      log.info(s"Request $requestId received from ${sender()} - Get $key")
      val client = sender()
      val node = nodes(key.## % nodes.length)
      node match {
        case Some(storageNode) =>
          storageNode ! SN_Get(self, requestId, key)
        case None => client ! Got(requestId, None)
      }

    case SN_Got(client, requestId, value) =>
      client ! Got(requestId, value)
  }
}
