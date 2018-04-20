name := "offer-service"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++= {
  val enumeratumVersion = "1.5.13"
  val circeVersion = "0.9.3"
  Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.8"
    ,"com.beachape" %% "enumeratum" % enumeratumVersion
    ,"com.typesafe.akka" %% "akka-http-testkit" % "10.0.8" % Test
  ) ++ Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras"
  ).map(_ % circeVersion)
}