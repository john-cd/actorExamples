package examples.cache

import akka.actor._
import scala.concurrent._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


object CacheClient {

  def run(): Unit = {

    // create the system and actor
    val system = ActorSystem("CacheClientSystem")
    val cache = system.actorOf(Props(classOf[Cache[String, String]], 1))


    // (1) this is one way to "ask" another actor
    implicit val timeout = Timeout(15.seconds)
    val future = cache ? Cache.Add(1L, "key", "value")
    val result = Await.result(future, timeout.duration)
    println(result)

    val fut2 = cache ? Cache.Get(1L, "key")
    val result2 = Await.result(fut2, 15.seconds).asInstanceOf[Cache.Got[String]]
    println(s"Got requestId ${result2.requestId} value ${result2.value}")

    system.terminate()
  }

}
