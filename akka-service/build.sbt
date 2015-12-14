name := "akka-service"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Maven Repository" at "https://repo1.maven.org/maven2/"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.4.0"

mainClass in assembly := Some("com.example.Boot")