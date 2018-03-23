name := "Kafka-definitive-guide"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
//resolvers += "stephenjudkins-bintray" at "http://dl.bintray.com/stephenjudkins/maven"

val json4sVersion = "3.3.0"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-streams" % "1.0.1",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "junit" % "junit" % "4.12" % Test,
  "org.scalamock" %% "scalamock" % "4.0.0" % Test
)