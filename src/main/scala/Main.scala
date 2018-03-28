import scala.io.StdIn
import scala.util._

object Main {

  def main(args: Array[String]): Unit = {

    // commandLineService.parse(args) match {
      // case Some(CommandLineConfig(paths, recursive)) => doWork(paths, recursive)
      // case None => System.exit(1) // Bad arguments. Error message has been displayed
    // }
	
	doWork()
  }


  private def doWork(): Unit = {

    // logger.info("Starting actor system. Use CTRL+C to exit.")
    // actorService.run()

    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  }
}