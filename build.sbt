val zioVersion = "2.0.4"
val zioLogger = "2.1.5"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "com.krishna",
        version := "0.0.1",
        scalaVersion := "3.2.1",
        description := "",
      )
    ),
    name := "zio-inspirational-quote",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,

      "dev.zio" %% "zio-json" % "0.3.0",

      // https://zio.github.io/zio-http/
      "io.d11" %% "zhttp" % "2.0.0-RC11",

      // https://zio.github.io/zio-logging/
      "dev.zio" %% "zio-logging" % zioLogger,
    ),
  )
