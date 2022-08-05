ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

val sttpVersion = "3.7.1"
val zioVersion  = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "scala-twitch",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core"                          % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe"                         % sttpVersion,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpVersion,
      "dev.zio"                       %% "zio-interop-cats"              % "22.0.0.0",
      "dev.zio"                       %% "zio-test"                      % zioVersion % "test",
      "dev.zio"                       %% "zio-test-sbt"                  % zioVersion % "test"
    ),
    scalacOptions += "-Wconf:cat=other-match-analysis:error"
  )
