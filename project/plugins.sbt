/*
https://github.com/spray/sbt-revolver

sbt-revolver defines three new commands (SBT tasks) in its own re configuration:
- reStart <args> --- <jvmArgs> starts your application in a forked JVM. The optionally specified (JVM) arguments are appended to the ones
configured via the reStartArgs/ reStart::javaOptions setting (see the "Configuration" section below). If the application is already running
 it is first stopped before being restarted.
- reStop stops application. This is done by simply force-killing the forked JVM. Note, that this means that shutdown hooks are not run (see #20).
- reStatus shows an informational message about the current running state of the application.
 */
//addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
