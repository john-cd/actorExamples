package examples.cache

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import examples.cache.Cache.{Added, Got}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

//noinspection SpellCheckingInspection,SpellCheckingInspection
object CacheClient extends LazyLogging {

  def run(): Unit = {

    // create the system and actor
    val system = ActorSystem("CacheClientSystem")
    val index = 42
    val cache = system.actorOf(Cache.props(index), s"Cache$index")

    implicit val timeout: Timeout = Timeout(15.seconds) // for the ? pattern

    for {
      res1 <- (cache ? Cache.Add(Cache.getRequestId, "mykey", "myvalue")).mapTo[Added]
    }
      logger.info(s"Add -> result $res1")

    system.scheduler.scheduleOnce(3.seconds) {
      for {
        res2 <- (cache ? Cache.Get(Cache.getRequestId, "mykey")).mapTo[Got[String]] // enforces sequential exec
      }
        logger.info(s"existing key - Get -> result $res2")
      for {
        res3 <- (cache ? Cache.Get(Cache.getRequestId, "not existing key")).mapTo[Got[String]]
      }
        logger.info(s"non existing key - Get -> result $res3")
    }

    system.scheduler.scheduleOnce(11.seconds) {
      (cache ? Cache.Get(Cache.getRequestId, "mykey")).mapTo[Got[String]].foreach(res4 => logger.info(s"after eviction - Get -> result $res4"))
    }

    system.scheduler.scheduleOnce(20.seconds) {
      system.terminate()
    }

    Await.ready(system.whenTerminated, 50.seconds).onComplete {
      case Success(_) => logger.info("Done!")
      case Failure(ex) => logger.error("", ex)
    }
  }

}

