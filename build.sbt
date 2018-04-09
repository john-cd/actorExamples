name := "akka-actors-scala"

version := "0.1"
scalaVersion := "2.12.5"

// AKKA
lazy val akkaVersion = "2.5.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

// PARSERS
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

// LOGGING
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
)

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")


/* TODO

import sbt._
import Keys._

name := "d"

lazy val commonSettings = Seq(
  organization := "com-john-cd",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.5"
)

lazy val overall = (project in file("."))
  .settings(commonSettings)
  .aggregate(sub1)

lazy val common = (project in file("common"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.flywaydb" % "flyway-core" % "4.1.2")
  )

lazy val sub1 = (project in file("slick"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.2.0"
    )
  )
  .dependsOn(common)
  
*/  