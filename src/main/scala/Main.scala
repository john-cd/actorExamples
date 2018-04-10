

object Main {


  def main(args: Array[String]): Unit = {

    // commandLineService.parse(args) match {
    // case Some(CommandLineConfig(arg1, arg2)) => doWork(...)
    // case None => System.exit(1) // Bad arguments. Error message has been displayed
    // }

    // OR

    doWork()
  }

  private def doWork(): Unit = {
    // examples.cache.CacheClient.run()
    // examples.hash.HashClient.run()
    examples.filewatch.WatchClient.run()
  }
}
