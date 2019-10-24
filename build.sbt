scalafmtOnCompile := true

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.0-RC1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

Compile / run / fork := true

cancelable in Global := true