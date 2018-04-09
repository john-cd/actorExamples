package actors

// TO READ:
// https://developer.lightbend.com/guides/akka-distributed-workers-scala/back-end.html
// http://letitcrash.com/post/29044669086/balancing-workload-across-nodes-with-akka-2

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration._


trait ActorComponent {

  val actorService: ActorService

  //noinspection ScalaUnusedSymbol
  object ActorService {

    // Loan pattern for actor system
    private def using[A](f: ActorSystem => A): A = {
      println("Starting actor system.")
      val system: ActorSystem = ActorSystem("mainActorSystem")
      try {
        // do something with it
        f(system)
      } finally {
        println("Stopping actor system.")
        system.terminate()
        // system.shutdown()
        // system.awaitTermination()
        Await.ready(system.whenTerminated, Duration(15, SECONDS)) // blocking
      }
    }
  }


  abstract class ActorService {

    def run(): Unit

    // Implement like:
    // def run(): Unit = using(actorSystem =>
    //    val cache = system.actorOf(Cache.props(), "cacheActor")
    //
    // )
  }

}
