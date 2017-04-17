name := "AkkaHttpMinimal"

version := "1.0.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.5",
    "com.typesafe.akka" %% "akka-http-core" % "2.4.4",
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4",
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.4",
    "com.typesafe.akka" %% "akka-slf4j" % "2.4.4",
    "com.typesafe.akka" %% "akka-http-testkit" % "2.4.4",
    "com.monsanto.arch" %% "kamon-prometheus" % "0.2.0",
    "org.scalatest" %% "scalatest" % "2.2.5" % Test,
    "org.mockito" % "mockito-core" % "2.1.0" % Test
)
scalacOptions ++= Seq("-deprecation", "-feature")
mainClass in assembly := Some("Main")

// META-INF discarding
assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case "reference.conf"              => MergeStrategy.concat
    case x => MergeStrategy.first
    //val baseStrategy = (assemblyMergeStrategy in assembly).value
    //baseStrategy(x)
}

fork in run := true
fork in Test := true

lazy val printVersion = taskKey[Unit]("Returns the version of this artifact")
lazy val printName = taskKey[Unit]("Returns the name of this artifact")
printVersion := println(version.value)
printName := println(name.value)
