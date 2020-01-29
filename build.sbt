
name := "akka-typed-tutorial"

scalaVersion := "2.13.1"

scalafmtOnCompile := true

val akkaVersion = "2.6.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.0",
    "org.scalacheck" %% "scalacheck" % "1.14.3",
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
    ).map(_ % Test)


Compile / run / fork := true

cancelable in Global := true
