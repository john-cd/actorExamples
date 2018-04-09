package examples.cache

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}


final case object DecrementTTL

sealed trait Message[+K, +V]

case class SN_Add[K, V](cache: ActorRef, requestId: Long, key: K, value: V) extends Message[K, V]

case class SN_Added(client: ActorRef, requestId: Long) extends Message[Nothing, Nothing]

case class SN_Get[K](client: ActorRef, requestId: Long, key: K) extends Message[K, Nothing]

case class SN_Got[V](client: ActorRef, requestId: Long, value: Option[V]) extends Message[Nothing, V]



object StorageNode {

  def props[K, V](id: Long): Props = Props(new StorageNode[K, V](id))

}

class StorageNode[K, V](id: Long) extends Actor with ActorLogging {

  private def this() = this(0L)

  private val maxTTL = 100

  private case class Record[V](value: V, initialTtl: Int = maxTTL) {
    private var ttl = initialTtl

    def decrement() = {
      ttl -= 1
      ttl
    }
  }

  private val store: collection.mutable.Map[K, Record[V]] = collection.mutable.Map.empty

  override def preStart(): Unit = log.info(s"StorageNode $id started")

  override def postStop(): Unit = log.info(s"StorageNode $id stopped")

  private def decrementTTL(): Unit = {

    // decrement TTL and evict dead values
    store.retain((k, v) => v.decrement() > 0)

    // context.children.map(_ ! StorageNode.DecrementTTL)

    // node dies when all stored values expire and there are no children
    if ((store.size == 0) && (context.children.size == 0)) {
      log.info(s"StorageNode $id dies")
      self ! PoisonPill
    }
  }

  def add(key: K, value: V): Unit = {
    store += key -> Record(value)
  }

  def receive: Receive = {

    case DecrementTTL => decrementTTL()


    // TODO:  https://www.cakesolutions.net/teamblogs/ways-to-pattern-match-generic-types-in-scala

    case SN_Add(cache, requestId, key: K, value: V) =>
      val client = sender()
      log.info(s"StorageNode Add request received from $client - requestID: $requestId Value: $value on node $id")
      add(key, value)
      cache ! SN_Added(client, requestId)

    case SN_Get(client, requestId, key) =>
      log.info(s"StorageNode Request received on node $id - requestID: $requestId Key: $key")
      sender() ! SN_Got(client, requestId, None)

    case _ => log.error(s"Unknown message received from ${sender()}")

  }
}

