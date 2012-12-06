name := "Quotes"

version := "0.1"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Typesafe Artifactory Repository" at "http://repo.typesafe.com/typesafe/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0.4",
  "com.typesafe.akka" % "akka-testkit" % "2.0.4",
  "org.scalatest" %% "scalatest" % "1.8" % "test"
)
