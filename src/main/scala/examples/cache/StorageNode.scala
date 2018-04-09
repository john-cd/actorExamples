package examples.cache

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}


object StorageNode {

  def props[K, V](id: Long): Props = Props(classOf[StorageNode[K, V]], id)

  final case object DecrementTTL

  sealed trait Message[+K1, +V1]

  case class SN_Add[K2, V2](cache: ActorRef, requestId: Long, key: K2, value: V2) extends Message[K2, V2]

  case class SN_Added(client: ActorRef, requestId: Long) extends Message[Nothing, Nothing]

  case class SN_Get[K3](client: ActorRef, requestId: Long, key: K3) extends Message[K3, Nothing]

  case class SN_Got[V4](client: ActorRef, requestId: Long, value: Option[V4]) extends Message[Nothing, V4]

}


class StorageNode[K, V](id: Long) extends Actor
  with ActorLogging {

  import StorageNode._

  private val maxTTL = 10

  private case class Record(value: V, initialTtl: Int = maxTTL) {
    private var ttl = initialTtl

    def decrement(): Int = {
      ttl -= 1
      ttl
    }
  }

  private val store: collection.mutable.Map[K, Record] = collection.mutable.Map.empty

  override def preStart(): Unit = log.info(s"StorageNode $id started")

  override def postStop(): Unit = log.info(s"StorageNode $id stopped")

  private def decrementTTL(): Unit = {

    // decrement TTL and evict dead values
    store.retain((_, v) => v.decrement() > 0)

    // context.children.map(_ ! StorageNode.DecrementTTL)

    // node dies when all stored values expire and there are no children
    if (store.isEmpty && context.children.isEmpty) {
      log.info(s"StorageNode $id dies")
      self ! PoisonPill
    }
  }

  private def add(key: K, value: V): Unit = {
    store += key -> Record(value)
  }

  def receive: Receive = {

    case StorageNode.DecrementTTL => decrementTTL()

    // TODO:  https://www.cakesolutions.net/teamblogs/ways-to-pattern-match-generic-types-in-scala

    case SN_Add(cache, requestId, key: K, value: V) =>
      val client = sender()
      log.info(s"StorageNode Add request received from $client on node $id - requestID: $requestId Value: $value")
      add(key, value)
      cache ! StorageNode.SN_Added(client, requestId)

    case SN_Get(client, requestId, key: K) =>
      log.info(s"StorageNode Get request from client $client received on node $id - requestID: $requestId Key: $key")

      val value: Option[V] = store.get(key).map(r => r.value)
      sender() ! StorageNode.SN_Got(client, requestId, value)

    case _ => log.error(s"Unknown message received from ${sender()}")

  }
}

